package com.GoPedidos.GoPedidos.DTOS;

public record CategoriaProdutoDTO(
		Long categoriaId,
		String nomeCategoria,
		String descricaoCategoria,
		String categoriaUrlImg,
		Long empresaId,
		Boolean visivel
) { }
