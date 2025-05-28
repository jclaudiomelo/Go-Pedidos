package com.GoPedidos.GoPedidos.DTOS;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmpresaDTO(

		@Schema(description = "CNPJ da empresa", example = "12.345.678/0001-90", required = true)
		String cnpj,

		@Schema(description = "Nome da empresa", example = "Empresa X", required = true)
		String nomeEmpresa,

		@Schema(description = "Logradouro da empresa", example = "Rua Exemplo", required = true)
		String logradouro,

		@Schema(description = "Número do endereço da empresa", example = "123", required = true)
		int numero,

		@Schema(description = "Complemento do endereço da empresa", example = "Apto 101")
		String complemento,

		@Schema(description = "CEP da empresa", example = "12345-678", required = true)
		String cep,

		@Schema(description = "Bairro da empresa", example = "Centro", required = true)
		String bairro,

		@Schema(description = "Município da empresa", example = "São Paulo", required = true)
		String municipio,

		@Schema(description = "UF da empresa", example = "SP", required = true)
		String uf,

		@Schema(description = "Email da empresa", example = "contato@empresa.com", required = true)
		String email,

		@Schema(description = "Telefone da empresa", example = "(11) 99999-9999", required = true)
		String telefone
)

{
}
