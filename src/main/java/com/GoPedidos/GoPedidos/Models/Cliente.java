package com.GoPedidos.GoPedidos.Models;

import com.GoPedidos.GoPedidos.Enuns.StatusCliente;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long clienteId;

	@NotNull
	@Size(min = 3, max = 100)
	private String nome;

	@Email
	@Size(max = 100)
	private String email;

	@Size(min = 11, max = 11)
	private String cpf;

	@NotNull
	private String telefone;

	private String endereco;

	private LocalDate dataCadastro;

	@Enumerated(EnumType.STRING)
	private StatusCliente status;

	@ManyToOne
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;


}
