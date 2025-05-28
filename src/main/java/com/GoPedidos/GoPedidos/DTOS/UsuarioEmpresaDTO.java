package com.GoPedidos.GoPedidos.DTOS;

import jakarta.validation.Valid;

public record UsuarioEmpresaDTO(
		@Valid UsuarioDTO usuarioDTO,
		@Valid EmpresaDTO empresaDTO
) {
}
