package com.GoPedidos.GoPedidos.DTOS;

public record ItemPedidoDTO(
		Long produtoId,
		Integer quantidade,
		String observacoes
) {
}
