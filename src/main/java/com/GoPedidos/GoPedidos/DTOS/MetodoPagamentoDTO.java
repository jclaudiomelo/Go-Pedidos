package com.GoPedidos.GoPedidos.DTOS;

public record MetodoPagamentoDTO(
		Long id,
		String descricao,
		boolean visivel,
		Long empresaId
) {
}
