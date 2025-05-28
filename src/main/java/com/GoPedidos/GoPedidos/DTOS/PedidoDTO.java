package com.GoPedidos.GoPedidos.DTOS;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public record PedidoDTO(
		Long pedidoId,
		Long clienteId,
		Long garcomId,
		LocalDateTime dataHoraPedido,
		Long statusPedidoId,  // ID do StatusPedido
		List<ItemPedidoDTO> itensPedido,
		BigDecimal totalPedido,
		Long metodoPagamentoId,
		String observacoes,
		Long mesaId,
		BigDecimal descontoAplicado,
		BigDecimal taxaServico,
		Boolean incluirTaxaServico
) {
	public PedidoDTO {
		Objects.requireNonNull(clienteId, "clienteId não pode ser nulo");
		Objects.requireNonNull(garcomId, "garcomId não pode ser nulo");
		Objects.requireNonNull(statusPedidoId, "statusPedidoId não pode ser nulo");
		Objects.requireNonNull(itensPedido, "itensPedido não pode ser nulo");
		Objects.requireNonNull(metodoPagamentoId, "metodoPagamentoId não pode ser nulo");

		if (itensPedido.isEmpty()) {
			throw new IllegalArgumentException("A lista de itensPedido não pode estar vazia");
		}
	}

	public String getDataHoraPedido() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		return dataHoraPedido != null ? dataHoraPedido.format(formatter) : null;
	}
}
//public record PedidoDTO(
//		Long pedidoId,
//		Long clienteId,
//		Long garcomId,
//		LocalDateTime dataHoraPedido,
//		String statusPedido,  // Usando o enum StatusPedido
//		List<ItemPedidoDTO> itensPedido,
//		BigDecimal totalPedido,
//		Long metodoPagamentoId,
//		String observacoes,
//		Long mesaId,
//		BigDecimal descontoAplicado
//) {
//}
