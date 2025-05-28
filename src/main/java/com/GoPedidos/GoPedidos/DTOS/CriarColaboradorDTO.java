package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.Enuns.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record CriarColaboradorDTO(

		@Schema(description = "Nome do colaborador", example = "Paulo Silva")
		String nome,

		@Schema(description = "Email do colaborador", example = "paulo@empresa.com")
		String email,

		@Schema(description = "CPF do colaborador", example = "123.456.789-00")
		String cpf,

		@Schema(description = "Senha do colaborador", example = "senha123")
		String senha,

		@Schema(description = "Cargo do colaborador", example = "GARCOM")
		Role role,

		@Schema(description = "colaborador Ativo", example = "Sim | Não")
		Boolean ativo

) {}