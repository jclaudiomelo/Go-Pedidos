package com.GoPedidos.GoPedidos.DTOS;

import java.math.BigDecimal;

public record ProdutoCardapioDTO(
		Long produtoId,
		String nome,
		BigDecimal preco,
		String descricao,
		boolean visivel
) {
}