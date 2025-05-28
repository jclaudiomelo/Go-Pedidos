package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itemPedidoId;

	@ManyToOne
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@ManyToOne
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(nullable = false)
	private Integer quantidade;

	@Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
	private BigDecimal precoUnitario;

	@Column(name = "subtotal", precision = 10, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "observacoes", columnDefinition = "TEXT")
	private String observacoes;

	@Column(name = "visivel", nullable = false)
	private boolean visivel;

	@ManyToOne
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;


	@PrePersist
	@PreUpdate
	private void calcularSubtotal() {
		if (precoUnitario != null && quantidade != null) {
			this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
		}
	}
}
