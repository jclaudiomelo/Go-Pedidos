package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.Models.Produto;

import java.time.LocalDateTime;
import java.util.List;

public record CardapioDTO(
		Long cardapio_id,
		String nome,
		LocalDateTime data_atualizacao,
		boolean visivel,
		Long empresaId,
		List<ProdutoDTO> produtos

) {
}
