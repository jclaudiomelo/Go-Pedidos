package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.DTOS.CategoriaProdutoDTO;

import java.math.BigDecimal;

public record ProdutoDTO(
		long produtoId,
		String nome,
		String descricao,
		BigDecimal preco,
		CategoriaProdutoDTO categoriaProduto,
		boolean visivel,
		Long cardapioId,
		String imagemUrl,
		Long empresaId
) { }
