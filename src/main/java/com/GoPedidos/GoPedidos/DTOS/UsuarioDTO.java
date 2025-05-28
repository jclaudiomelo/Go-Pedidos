package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.Enuns.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioDTO(

		@Schema(description = "Id usuário", example = "2")
		Long id,

		@Schema(description = "Nome do usuário", example = "João Silva")
		String nome,

		@Schema(description = "Email do usuário", example = "joao.silva@empresa.com")
		String email,

		@Schema(description = "CPF do usuário", example = "123.456.789-00")
		String cpf,

		@Schema(description = "Senha do usuário", example = "senha123")
		String senha,

		@Schema(description = "Cargo do usuário", example = "GERENTE")
		Role role,

		@Schema(description = "Foto", example = "foto")
		String usuarioUrl,

		@Schema(description = "colaborador Ativo", example = "Sim | Não")
		Boolean ativo,

		@Schema(description = "Empresa associada ao usuário")
		EmpresaDTO empresa


) {
}
