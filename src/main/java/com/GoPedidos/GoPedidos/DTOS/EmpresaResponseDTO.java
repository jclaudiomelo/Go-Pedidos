package com.GoPedidos.GoPedidos.DTOS;

//public record EmpresaResponseDTO(
//		Long id,
//		String nomeEmpresa,
//		String cnpj
//) {
//}
public record EmpresaResponseDTO(
		Long id,
		String nomeEmpresa,
		String cnpj,
		String logradouro,
		int numero,
		String complemento,
		String cep,
		String bairro,
		String municipio,
		String uf,
		String email,
		String telefone
) {
}
