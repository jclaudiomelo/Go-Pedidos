package com.GoPedidos.GoPedidos.DTOS;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.List;
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "__type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PedidoSimplificadoDTO.class, name = "PedidoSimplificadoDTO")
})
public class PedidoSimplificadoDTO {

	private Long pedidoId;
	private Long clienteId;
	private Long garcomId;
	private LocalDateTime dataHoraPedido;
	private Long statusPedidoId;
	private List<ItemPedidoDTO> itensPedido;
	private Double totalPedido;
	private Long metodoPagamentoId;
	private String observacoes;
	private Long mesaId;
	private Double descontoAplicado;

	public Long getPedidoId() {
		return pedidoId;
	}

	public void setPedidoId(Long pedidoId) {
		this.pedidoId = pedidoId;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}

	public Long getGarcomId() {
		return garcomId;
	}

	public void setGarcomId(Long garcomId) {
		this.garcomId = garcomId;
	}

	public LocalDateTime getDataHoraPedido() {
		return dataHoraPedido;
	}

	public void setDataHoraPedido(LocalDateTime dataHoraPedido) {
		this.dataHoraPedido = dataHoraPedido;
	}

	public Long getStatusPedidoId() {
		return statusPedidoId;
	}

	public void setStatusPedidoId(Long statusPedidoId) {
		this.statusPedidoId = statusPedidoId;
	}

	public List<ItemPedidoDTO> getItensPedido() {
		return itensPedido;
	}

	public void setItensPedido(List<ItemPedidoDTO> itensPedido) {
		this.itensPedido = itensPedido;
	}

	public Double getTotalPedido() {
		return totalPedido;
	}

	public void setTotalPedido(Double totalPedido) {
		this.totalPedido = totalPedido;
	}

	public Long getMetodoPagamentoId() {
		return metodoPagamentoId;
	}

	public void setMetodoPagamentoId(Long metodoPagamentoId) {
		this.metodoPagamentoId = metodoPagamentoId;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public Long getMesaId() {
		return mesaId;
	}

	public void setMesaId(Long mesaId) {
		this.mesaId = mesaId;
	}

	public Double getDescontoAplicado() {
		return descontoAplicado;
	}

	public void setDescontoAplicado(Double descontoAplicado) {
		this.descontoAplicado = descontoAplicado;
	}
// Getters and setters
}
