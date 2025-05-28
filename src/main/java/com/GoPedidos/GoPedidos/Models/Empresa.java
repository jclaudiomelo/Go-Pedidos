package com.GoPedidos.GoPedidos.Models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Empresa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column()
//	@Column(nullable = false)
	private String nomeEmpresa;

	@Column( unique = true, length = 18)
	private String cnpj;

	@Column()
	private String logradouro;

	@Column()
	private int numero;

	@Column
	private String complemento;

	@Column(length = 9)
	private String cep;

	@Column()
	private String bairro;

	@Column()
	private String municipio;

	@Column(length = 2)
	private String uf;

	@Column()
//	@Column(nullable = false, unique = true)
	private String email;

	@Column()
	private String telefone;

	@OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Usuario> usuarios = new ArrayList<>();

}
