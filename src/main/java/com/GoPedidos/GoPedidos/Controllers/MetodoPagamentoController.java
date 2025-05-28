package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.MetodoPagamentoDTO;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.MetodoPagamentoService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metodos-pagamento")
public class MetodoPagamentoController {

	@Autowired
	private MetodoPagamentoService metodoPagamentoService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	@PostMapping("/criar")
	public ResponseEntity<?> criar(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestBody MetodoPagamentoDTO dto) {
		try {
			MetodoPagamentoDTO criado = metodoPagamentoService.criar(authorizationHeader, dto);
			return ResponseEntity.status(HttpStatus.CREATED).body(criado);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage(), "statusCode", e.getStatusCode()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro interno", "statusCode", 500));
		}
	}

	@GetMapping("/listar")
	public ResponseEntity<?> listar(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			List<MetodoPagamentoDTO> lista = metodoPagamentoService.listar(authorizationHeader);
			return ResponseEntity.ok(lista);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage(), "statusCode", e.getStatusCode()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro interno", "statusCode", 500));
		}
	}

	@PutMapping("/atualizar/{id}")
	public ResponseEntity<?> atualizar(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id,
			@RequestBody MetodoPagamentoDTO dto) {
		try {
			MetodoPagamentoDTO atualizado = metodoPagamentoService.atualizar(authorizationHeader, id, dto);
			return ResponseEntity.ok(atualizado);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage(), "statusCode", e.getStatusCode()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro interno", "statusCode", 500));
		}
	}

	@DeleteMapping("/deletar/{id}")
	public ResponseEntity<?> deletar(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id) {
		try {
			String msg = metodoPagamentoService.deletar(authorizationHeader, id);
			return ResponseEntity.ok(Map.of("message", msg, "statusCode", 200));
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage(), "statusCode", e.getStatusCode()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro interno", "statusCode", 500));
		}
	}
}

