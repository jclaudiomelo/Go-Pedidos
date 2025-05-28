package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pedidoId;

	@ManyToOne
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;

	@ManyToOne
	@JoinColumn(name = "garcom_id", nullable = false)
	private Usuario garcom;

	@ManyToOne
	@JoinColumn(name = "status_pedido_id", nullable = false)
	private StatusPedido statusPedido;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ItemPedido> itensPedido;

	@Column(name = "total_pedido", precision = 10, scale = 2, nullable = false)
	private BigDecimal totalPedido;

	@ManyToOne
	@JoinColumn(name = "metodo_pagamento_id", nullable = false)
	private MetodoPagamento metodoPagamento;

	@Column(name = "observacoes", columnDefinition = "TEXT")
	private String observacoes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mesa_id", referencedColumnName = "mesa_id")
	private Mesa mesa;

	@Column(name = "desconto_aplicado", precision = 10, scale = 2)
	private BigDecimal descontoAplicado;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Column(name = "taxa_servico")
	private BigDecimal taxaServico;


	@Column(name = "data_hora_pedido", nullable = false, updatable = false)
	private LocalDateTime dataHoraPedido;
}
