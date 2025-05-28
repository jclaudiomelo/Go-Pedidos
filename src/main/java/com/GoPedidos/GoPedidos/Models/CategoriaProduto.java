package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias_produto")
public class CategoriaProduto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "categoria_id")
	private Long categoriaId;

	@Column(name = "nome_categoria", nullable = false, length = 255)
	private String nomeCategoria;

	@Column(name = "descricao_categoria", columnDefinition = "TEXT")
	private String descricaoCategoria;

	// Relacionamento OneToMany com Produto
	@OneToMany(mappedBy = "categoriaProduto", fetch = FetchType.EAGER)
	private List<Produto> produtos;

	@Column(name = "categoria_url_img")
	private String categoriaUrlImg;



	@Column(name = "visivel")
	private Boolean visivel;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;
}
