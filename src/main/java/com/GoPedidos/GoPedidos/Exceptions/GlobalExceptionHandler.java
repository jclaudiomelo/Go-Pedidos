package com.GoPedidos.GoPedidos.Exceptions;

import com.GoPedidos.GoPedidos.DTOS.MensagemResponseDTO;
import org.hibernate.Internal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<MensagemResponseDTO> handleCustomException(CustomException ex) {
		MensagemResponseDTO errorResponse = new MensagemResponseDTO(ex.getMessage(), ex.getStatusCode());
		return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getStatusCode()));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<MensagemResponseDTO> handleRuntimeException(RuntimeException ex) {
		MensagemResponseDTO errorResponse = new MensagemResponseDTO("Erro interno no servidor.", HttpStatus.INTERNAL_SERVER_ERROR.value());
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}

//Informational Responses (100–199):
//
//		HttpStatus.CONTINUE (100): Continue.
//
//		HttpStatus.SWITCHING_PROTOCOLS (101): Switching Protocols.
//
//Successful Responses (200–299):
//
//		HttpStatus.OK (200): OK.
//
//		HttpStatus.CREATED (201): Created.
//
//		HttpStatus.ACCEPTED (202): Accepted.
//
//		HttpStatus.NO_CONTENT (204): No Content.
//
//Redirection Messages (300–399):
//
//		HttpStatus.MOVED_PERMANENTLY (301): Moved Permanently.
//
//		HttpStatus.FOUND (302): Found.
//
//		HttpStatus.SEE_OTHER (303): See Other.
//
//		HttpStatus.NOT_MODIFIED (304): Not Modified.
//
//		HttpStatus.TEMPORARY_REDIRECT (307): Temporary Redirect.
//
//Client Error Responses (400–499):
//
//		HttpStatus.BAD_REQUEST (400): Bad Request.
//
//		HttpStatus.UNAUTHORIZED (401): Unauthorized.
//
//		HttpStatus.FORBIDDEN (403): Forbidden.
//
//		HttpStatus.NOT_FOUND (404): Not Found.
//
//		HttpStatus.METHOD_NOT_ALLOWED (405): Method Not Allowed.
//
//		HttpStatus.CONFLICT (409): Conflict.
//
//		HttpStatus.GONE (410): Gone.
//
//		HttpStatus.UNSUPPORTED_MEDIA_TYPE (415): Unsupported Media Type.
//
//		HttpStatus.TOO_MANY_REQUESTS (429): Too Many Requests.
//
//Server Error Responses (500–599):
//
//		HttpStatus.INTERNAL_SERVER_ERROR (500): Internal Server Error.
//
//		HttpStatus.NOT_IMPLEMENTED (501): Not Implemented.
//
//		HttpStatus.BAD_GATEWAY (502): Bad Gateway.
//
//		HttpStatus.SERVICE_UNAVAILABLE (503): Service Unavailable.
//
//HttpStatus.GATEWAY_TIMEOUT