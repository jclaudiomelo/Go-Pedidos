package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Cardapio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cardapio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cardapio_id")
	private Long cardapio_id;


	@Column(name = "nome_cardapio", nullable = false)
	private String nome;

	@OneToMany(mappedBy = "cardapio", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Produto> produtos;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao = LocalDateTime.now();


	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao = LocalDateTime.now();

	@Column(name = "visivel")
	private boolean visivel;

	@ManyToOne
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;


}
