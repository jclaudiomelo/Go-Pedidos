package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.StatusPedidoDTO;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.StatusPedido;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.StatusPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusPedidoService {

	@Autowired
	private StatusPedidoRepository statusPedidoRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	public StatusPedidoDTO criarStatusPedido(StatusPedidoDTO statusPedidoDTO, Long empresaId) {
		// Verificando se a empresa existe
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

		// Criando o novo Status de Pedido
		StatusPedido statusPedido = new StatusPedido();
		statusPedido.setDescricao(statusPedidoDTO.descricao());
		statusPedido.setVisivel(statusPedidoDTO.visivel());
		statusPedido.setEmpresa(empresa);

		// Salvando no banco de dados
		StatusPedido statusPedidoSalvo = statusPedidoRepository.save(statusPedido);

		// Retornando o DTO
		return new StatusPedidoDTO(
				statusPedidoSalvo.getId(),
				statusPedidoSalvo.getDescricao(),
				statusPedidoSalvo.isVisivel(),
				statusPedidoSalvo.getEmpresa().getId()
		);
	}

	public List<StatusPedidoDTO> listarStatusPedidos(Long empresaId) {
		List<StatusPedido> statusPedidos = statusPedidoRepository.findByEmpresaId(empresaId);
		return statusPedidos.stream()
				.map(status -> new StatusPedidoDTO(
						status.getId(),
						status.getDescricao(),
						status.isVisivel(),
						status.getEmpresa().getId()
				))
				.collect(Collectors.toList());
	}
}
