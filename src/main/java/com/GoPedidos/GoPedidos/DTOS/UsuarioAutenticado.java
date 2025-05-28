package com.GoPedidos.GoPedidos.DTOS;

public record UsuarioAutenticado(Long usuarioId,
								 Long empresaId,
								 String role) {
}
