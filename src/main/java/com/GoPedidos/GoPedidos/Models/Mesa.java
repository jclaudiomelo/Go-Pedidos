package com.GoPedidos.GoPedidos.Models;

import com.GoPedidos.GoPedidos.Enuns.MesaEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mesas")
public class Mesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)  // Geração automática da chave primária
	@Column(name = "mesa_id")
	private Long mesaId;

	@Column(name = "numero")
	private Long numero;

	@Column(name = "descricao")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private MesaEnum status; // Exemplo: "DISPONÍVEL", "OCUPADA", "RESERVADA"

	@ManyToOne
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;


}
