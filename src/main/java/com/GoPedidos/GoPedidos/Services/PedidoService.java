package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.ItemPedidoDTO;
import com.GoPedidos.GoPedidos.DTOS.PedidoDTO;
import com.GoPedidos.GoPedidos.DTOS.PedidoSimplificadoDTO;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.*;
import com.GoPedidos.GoPedidos.RabbitMQ.PedidoProducer;
import com.GoPedidos.GoPedidos.Respositories.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log
@Service
public class PedidoService {


	@Autowired
	private PedidoProducer pedidoProducer;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private MesaRepository mesaRepository;

	@Autowired
	private StatusPedidoRepository statusPedidoRepository;

	@Autowired
	private MetodoPagamentoRepository metodoPagamentoRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	public void enviarPedidoParaWebSocket(PedidoSimplificadoDTO pedidoSimplificadoDTO) {
		// Enviar a mensagem para o canal "/cozinha" via WebSocket
		messagingTemplate.convertAndSend("/cozinha", pedidoSimplificadoDTO);
	}

	@Transactional
	public PedidoDTO criarPedido(PedidoDTO pedidoDTO, Long empresaId) {
		try {
			// Validação da lista de itens do pedido
			if (pedidoDTO.itensPedido() == null || pedidoDTO.itensPedido().isEmpty()) {
				throw new CustomException("A lista de itens do pedido não pode estar vazia!", HttpStatus.BAD_REQUEST.value());
			}

			// 🔍 Busca o status do pedido
			StatusPedido statusPedido = statusPedidoRepository.findById(pedidoDTO.statusPedidoId())
					.orElseThrow(() -> new CustomException("Status do pedido não encontrado!", HttpStatus.NOT_FOUND.value()));

			// 🔍 Busca o cliente
			Cliente cliente = clienteRepository.findById(pedidoDTO.clienteId())
					.orElseThrow(() -> new CustomException("Cliente não encontrado!", HttpStatus.NOT_FOUND.value()));

			// 🔍 Busca o garçom
			Usuario garcom = usuarioRepository.findById(pedidoDTO.garcomId())
					.orElseThrow(() -> new CustomException("Garçom não encontrado!", HttpStatus.NOT_FOUND.value()));

			// Verifica se o status pertence à empresa correta
			if (!statusPedido.getEmpresa().getId().equals(empresaId)) {
				throw new CustomException("Status do pedido não pertence à empresa!", HttpStatus.FORBIDDEN.value());
			}

			// 🔍 Busca a empresa
			Empresa empresa = empresaRepository.findById(empresaId)
					.orElseThrow(() -> new CustomException("Empresa não encontrada!", HttpStatus.NOT_FOUND.value()));

			// 🔍 Verifica se cliente e garçom pertencem à empresa correta
			if (!cliente.getEmpresa().getId().equals(empresaId) || !garcom.getEmpresa().getId().equals(empresaId)) {
				throw new CustomException("Usuário não pode criar pedidos para outra empresa!", HttpStatus.FORBIDDEN.value());
			}

			// 🔍 Busca o metodo de pagamento
			MetodoPagamento metodoPagamento = metodoPagamentoRepository.findById(pedidoDTO.metodoPagamentoId())
					.orElseThrow(() -> new CustomException("Método de pagamento não encontrado!", HttpStatus.NOT_FOUND.value()));

			// 🔍 Busca a mesa (se houver)
			Mesa mesa = null;
			if (pedidoDTO.mesaId() != null) {
				mesa = mesaRepository.findById(pedidoDTO.mesaId())
						.orElseThrow(() -> new CustomException("Mesa não encontrada!", HttpStatus.NOT_FOUND.value()));

				// Verifica se a mesa pertence à empresa correta
				if (!mesa.getEmpresa().getId().equals(empresaId)) {
					throw new CustomException("A mesa informada não pertence à sua empresa!", HttpStatus.FORBIDDEN.value());
				}
			}

			// 🔹 Criando os itens do pedido e calculando o total 🔹
			BigDecimal totalPedido = BigDecimal.ZERO;
			List<ItemPedido> itensPedido = new ArrayList<>();

			for (ItemPedidoDTO itemDTO : pedidoDTO.itensPedido()) {
				Produto produto = produtoRepository.findById(itemDTO.produtoId())
						.orElseThrow(() -> new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value()));

				// Validação do preço do produto
				if (produto.getPreco() == null) {
					throw new CustomException("Preço do produto não pode ser nulo!", HttpStatus.BAD_REQUEST.value());
				}

				// Validação da quantidade
				if (itemDTO.quantidade() <= 0) {
					throw new CustomException("Quantidade do produto deve ser maior que zero!", HttpStatus.BAD_REQUEST.value());
				}

				// Calcula o subtotal do item
				BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.quantidade()));

				// Soma ao total do pedido
				totalPedido = totalPedido.add(subtotal);

				// Criando o item do pedido
				ItemPedido item = ItemPedido.builder()
						.produto(produto)
						.empresa(empresa)
						.quantidade(itemDTO.quantidade())
						.precoUnitario(produto.getPreco())
						.subtotal(subtotal)
						.observacoes(itemDTO.observacoes())
						.build();

				itensPedido.add(item);
			}

			// 🔹 Aplica o desconto primeiro
			if (pedidoDTO.descontoAplicado() != null) {
				totalPedido = totalPedido.subtract(pedidoDTO.descontoAplicado());
			}

			// 🔹 Calcula a taxa de serviço (10% do total após o desconto) SE o cliente optar por incluí-la
			BigDecimal taxaServico = BigDecimal.ZERO;
			if (Boolean.TRUE.equals(pedidoDTO.incluirTaxaServico())) {
				double porcentagem = 10.0;
				taxaServico = totalPedido.multiply(BigDecimal.valueOf(porcentagem / 100));
				// ✅ Aplica DUAS casas decimais corretamente
//				totalPedido = totalPedido.setScale(2, RoundingMode.HALF_UP);
//				taxaServico = taxaServico.setScale(2, RoundingMode.HALF_UP);
			}

			// 🔹 Adiciona a taxa de serviço ao total final
			totalPedido = totalPedido.add(taxaServico);

			// 🔹 Garante que o total nunca seja negativo
			totalPedido = totalPedido.max(BigDecimal.ZERO);

			// 🔄 Criando o pedido
			Pedido novoPedido = Pedido.builder()
					.cliente(cliente)
					.garcom(garcom)
					.statusPedido(statusPedido)
					.itensPedido(itensPedido)
					.totalPedido(totalPedido)
					.metodoPagamento(metodoPagamento)
					.observacoes(pedidoDTO.observacoes())
					.mesa(mesa)
					.descontoAplicado(pedidoDTO.descontoAplicado())
					.empresa(empresa)
					.dataHoraPedido(LocalDateTime.now())
					.taxaServico(taxaServico)
					.build();

			// Associa os itens ao pedido
			for (ItemPedido item : itensPedido) {
				item.setPedido(novoPedido);
			}

			// 🚀 Salvando o pedido e os itens
			Pedido pedidoSalvo = pedidoRepository.save(novoPedido);

			// Envia a mensagem para o RabbitMQ fora da transação
			try {
				pedidoProducer.enviarPedidoCriado(pedidoDTO);
			} catch (Exception e) {
				throw new CustomException("Erro ao enviar pedido para o RabbitMQ!", HttpStatus.INTERNAL_SERVER_ERROR.value());
			}

			// 🔄 Retornando DTO com dados do pedido salvo
			return new PedidoDTO(
					pedidoSalvo.getPedidoId(),
					pedidoSalvo.getCliente().getClienteId(),
					pedidoSalvo.getGarcom().getId(),
					pedidoSalvo.getDataHoraPedido(),
					pedidoSalvo.getStatusPedido().getId(),
					pedidoSalvo.getItensPedido().stream()
							.map(item -> new ItemPedidoDTO(
									item.getProduto().getProdutoId(),
									item.getQuantidade(),
									item.getObservacoes()))
							.collect(Collectors.toList()),
					pedidoSalvo.getTotalPedido(),
					pedidoSalvo.getMetodoPagamento().getId(),
					pedidoSalvo.getObservacoes(),
					pedidoSalvo.getMesa() != null ? pedidoSalvo.getMesa().getMesaId() : null,
					pedidoSalvo.getDescontoAplicado(),
					pedidoSalvo.getTaxaServico(),
					pedidoDTO.incluirTaxaServico()
			);

		} catch (Exception e) {
			throw new CustomException("Erro ao criar pedido: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}


	@Transactional
	public List<PedidoDTO> listarPedidosPorEmpresa(Long empresaId) {
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new CustomException("Empresa não encontrada!", HttpStatus.NOT_FOUND.value()));

		List<Pedido> pedidos = pedidoRepository.findByEmpresa(empresa);

		return pedidos.stream().map(pedido -> new PedidoDTO(
				pedido.getPedidoId(),
				pedido.getCliente().getClienteId(),
				pedido.getGarcom().getId(),
				pedido.getDataHoraPedido(),
				pedido.getStatusPedido().getId(),
				pedido.getItensPedido().stream()
						.map(item -> new ItemPedidoDTO(
								item.getProduto().getProdutoId(),
								item.getQuantidade(),
								item.getObservacoes()))
						.toList(),
				pedido.getTotalPedido(),
				pedido.getMetodoPagamento().getId(),
				pedido.getObservacoes(),
				pedido.getMesa() != null ? pedido.getMesa().getMesaId() : null,
				pedido.getDescontoAplicado(),
				pedido.getTaxaServico(),
				null
		)).toList();
	}


	@Transactional(readOnly = true)
	public PedidoDTO buscarPedidoPorId(Long pedidoId, Long empresaId) {
		// Busca o pedido pelo ID
		Pedido pedido = pedidoRepository.findById(pedidoId)
				.orElseThrow(() -> new CustomException("Pedido não encontrado!", HttpStatus.NOT_FOUND.value()));

		// Verifica se o pedido pertence à empresa correta
		if (!pedido.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Pedido não pertence à empresa do usuário!", HttpStatus.FORBIDDEN.value());
		}


		// Converte a entidade Pedido para PedidoDTO
		return new PedidoDTO(
				pedido.getPedidoId(),
				pedido.getCliente().getClienteId(),
				pedido.getGarcom().getId(),
				pedido.getDataHoraPedido(),
				pedido.getStatusPedido().getId(),
				pedido.getItensPedido().stream()
						.map(item -> new ItemPedidoDTO(
								item.getProduto().getProdutoId(),
								item.getQuantidade(),
								item.getObservacoes()))
						.collect(Collectors.toList()),
				pedido.getTotalPedido(),
				pedido.getMetodoPagamento().getId(),
				pedido.getObservacoes(),
				pedido.getMesa() != null ? pedido.getMesa().getMesaId() : null,
				pedido.getDescontoAplicado(),
				pedido.getTaxaServico(),
				null
		);

	}
}
