package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.ItemPedidoDTO;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.ItemPedido;
import com.GoPedidos.GoPedidos.Models.Produto;
import com.GoPedidos.GoPedidos.Respositories.ItemPedidoRepository;
import com.GoPedidos.GoPedidos.Respositories.PedidoRepository;
import com.GoPedidos.GoPedidos.Respositories.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemPedidoService {

	private final ItemPedidoRepository itemPedidoRepository;
	private final PedidoRepository pedidoRepository;
	private final ProdutoRepository produtoRepository;

	//	@Transactional
//	public ItemPedido criarItemPedido(ItemPedidoDTO itemPedidoDTO) {
//		// Obtém o usuário autenticado
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		Map<String, Object> detalhes = (Map<String, Object>) authentication.getDetails();
//		Long empresaUsuario = (Long) detalhes.get("empresa_id");
//
//		// 🔍 Busca o Pedido
//		Pedido pedido = pedidoRepository.findById(itemPedidoDTO.produtoId())
//				.orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
//
//		// ✅ Verifica se o Pedido pertence à empresa do usuário
//		if (!pedido.getEmpresa().getId().equals(empresaUsuario)) {
//			throw new RuntimeException("Pedido não pertence à empresa do usuário.");
//		}
//
//		// 🔍 Busca o Produto
//		Produto produto = produtoRepository.findById(itemPedidoDTO.produtoId())
//				.orElseThrow(() -> new RuntimeException("Produto não encontrado"));
//
//		// ✅ Verifica se o Produto pertence à mesma empresa
//		if (!produto.getEmpresa().getId().equals(empresaUsuario)) {
//			throw new RuntimeException("Produto não pertence à empresa do usuário.");
//		}
//
//		// ✅ Verifica se o Produto faz parte do mesmo Pedido
//		if (!pedido.getItensPedido().stream().anyMatch(item -> item.getProduto().getProdutoId().equals(produto.getProdutoId()))) {
//			throw new RuntimeException("Produto não faz parte deste pedido.");
//		}
//
//		// 🔄 Criando o ItemPedido
//		ItemPedido itemPedido = new ItemPedido();
//		itemPedido.setPedido(pedido);
//		itemPedido.setProduto(produto);
//		itemPedido.setQuantidade(itemPedidoDTO.quantidade());
//		itemPedido.setPrecoUnitario(produto.getPreco());
//		itemPedido.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemPedidoDTO.quantidade())));
//		itemPedido.setObservacoes(itemPedidoDTO.observacoes());
//		itemPedido.setVisivel(true);
//
//		return itemPedidoRepository.save(itemPedido);
//	}
	@Transactional
	public ItemPedido criarItemPedido(ItemPedidoDTO itemPedidoDTO) {
		// Obtém o usuário autenticado
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> detalhes = (Map<String, Object>) authentication.getDetails();
		Long empresaUsuario = (Long) detalhes.get("empresa_id");

		// 🔍 Busca o Produto
		Produto produto = produtoRepository.findById(itemPedidoDTO.produtoId())
				.orElseThrow(() -> {
					return new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value());
				});

		// ✅ Verifica se o Produto pertence à mesma empresa
		if (!produto.getEmpresa().getId().equals(empresaUsuario)) {
			throw new CustomException("Produto não pertence à empresa do usuário!", HttpStatus.FORBIDDEN.value());
		}

		// 🔄 Criando o ItemPedido
		ItemPedido itemPedido = new ItemPedido();
		itemPedido.setProduto(produto);
		itemPedido.setQuantidade(itemPedidoDTO.quantidade());
		itemPedido.setPrecoUnitario(produto.getPreco());
		itemPedido.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemPedidoDTO.quantidade())));
		itemPedido.setObservacoes(itemPedidoDTO.observacoes());
		itemPedido.setVisivel(true);

		return itemPedidoRepository.save(itemPedido);
	}

	@Transactional
	public ItemPedido editarItemPedido(Long id, ItemPedidoDTO itemPedidoDTO) {
		// Obtém o usuário autenticado
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> detalhes = (Map<String, Object>) authentication.getDetails();
		Long empresaUsuario = (Long) detalhes.get("empresa_id");

		// 🔍 Busca o ItemPedido
		ItemPedido itemPedido = itemPedidoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Item do pedido não encontrado"));

		// ✅ Verifica se o ItemPedido pertence à empresa do usuário
		if (!itemPedido.getPedido().getEmpresa().getId().equals(empresaUsuario)) {
			throw new RuntimeException("Item do pedido não pertence à empresa do usuário.");
		}

		// 🔄 Atualiza os dados
		if (itemPedidoDTO.quantidade() != null) {
			itemPedido.setQuantidade(itemPedidoDTO.quantidade());
			itemPedido.setSubtotal(itemPedido.getPrecoUnitario().multiply(BigDecimal.valueOf(itemPedidoDTO.quantidade())));
		}

		if (itemPedidoDTO.observacoes() != null) {
			itemPedido.setObservacoes(itemPedidoDTO.observacoes());
		}

		return itemPedidoRepository.save(itemPedido);
	}

	@Transactional
	public void deletarItemPedido(Long id) {
		// Obtém o usuário autenticado
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> detalhes = (Map<String, Object>) authentication.getDetails();
		Long empresaUsuario = (Long) detalhes.get("empresa_id");

		// 🔍 Busca o ItemPedido
		ItemPedido itemPedido = itemPedidoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Item do pedido não encontrado"));

		// ✅ Verifica se o ItemPedido pertence à empresa do usuário
		if (!itemPedido.getPedido().getEmpresa().getId().equals(empresaUsuario)) {
			throw new RuntimeException("Item do pedido não pertence à empresa do usuário.");
		}

		itemPedidoRepository.delete(itemPedido);
	}
}

