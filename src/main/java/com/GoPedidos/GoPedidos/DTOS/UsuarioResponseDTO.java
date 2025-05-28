package com.GoPedidos.GoPedidos.DTOS;

public record UsuarioResponseDTO(
		Long id,
		String nome,
		String email,
		String usuarioUrl,
		String cpf,
		String role,
		Boolean ativo
) {
}
