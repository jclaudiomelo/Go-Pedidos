package com.GoPedidos.GoPedidos.DTOS;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MensagemResponseDTO {
	private String message;
	private int statusCode;

	public MensagemResponseDTO(String message, int statusCode) {
		this.message = message;
		this.statusCode = statusCode;
	}

}