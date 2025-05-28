package com.GoPedidos.GoPedidos.DTOS;

import java.time.LocalDateTime;
import java.util.List;

public record CardapioProdutosDTO(
		Long cardapio_id,
		String nome,
		LocalDateTime data_atualizacao,
		boolean visivel,
		Long empresaId,
		List<ProdutoCardapioDTO> produtos  // Lista de produtos
) {
}