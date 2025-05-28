package com.GoPedidos.GoPedidos.DTOS;

public record StatusPedidoDTO(
		Long id,
		String descricao,
		boolean visivel,
		Long empresaId
) {
}
