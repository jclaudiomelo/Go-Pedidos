package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.Enuns.MesaEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

public record MesaDTO(
		Long mesaId,
		String descricao,
		@JsonFormat(shape = JsonFormat.Shape.STRING)
		MesaEnum status, // Mantemos o tipo String para o status
		Long empresaId,
		Long numero
) {
}

