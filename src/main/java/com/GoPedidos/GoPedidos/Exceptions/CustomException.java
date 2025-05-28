package com.GoPedidos.GoPedidos.Exceptions;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	private final String message;
	private final int statusCode;

	public CustomException(String message, int statusCode) {
		super(message);
		this.message = message;
		this.statusCode = statusCode;
	}

}
