package com.GoPedidos.GoPedidos.DTOS;

public record LoginDTO(
		String email,
		String senha,
		String captchaToken
) {
}
