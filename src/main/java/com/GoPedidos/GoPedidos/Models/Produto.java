package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produtos")
public class Produto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "produto_id")
	private Long produtoId;

	@Column(name = "nome_produto", nullable = false, length = 255)
	private String nome;

	@Column(name = "descricao_produto", columnDefinition = "TEXT")
	private String descricao;

	@Column(name = "preco_produto", nullable = false, precision = 10, scale = 2)
	private BigDecimal preco;

	// Relacionamento ManyToOne com CategoriaProduto
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "categoria_id", referencedColumnName = "categoria_id")
	private CategoriaProduto categoriaProduto;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "cardapio_id", referencedColumnName = "cardapio_id")
	private Cardapio cardapio;

	@Column(name = "visivel")
	private Boolean visivel;

	@Column(name = "imagem_url")
	private String imagemUrl;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;
}
