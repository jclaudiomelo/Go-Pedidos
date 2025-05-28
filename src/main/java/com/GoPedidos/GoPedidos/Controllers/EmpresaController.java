package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.EmpresaDTO;
import com.GoPedidos.GoPedidos.DTOS.EmpresaResponseDTO;
import com.GoPedidos.GoPedidos.Services.EmpresaService;
import com.GoPedidos.GoPedidos.Services.TokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

	@Autowired
	private EmpresaService empresaService;

	@Autowired
	private TokenService tokenService;

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarEmpresaPorToken(
			@RequestHeader("Authorization") String authorizationHeader) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(authorizationHeader.replace("Bearer ", ""));
		Long empresaId = decodedJWT.getClaim("empresa_id").asLong();
		EmpresaResponseDTO empresa = empresaService.buscarEmpresaPorId(empresaId);
		return ResponseEntity.ok(empresa);

	}

	@PutMapping("/{id}")
	public ResponseEntity<?> atualizarEmpresa(
			@PathVariable Long id,
			@RequestBody EmpresaDTO empresaDTO,
			@RequestHeader("Authorization") String authorizationHeader) {
		EmpresaResponseDTO empresaAtualizada = empresaService.atualizarEmpresa(id, empresaDTO);
		return ResponseEntity.ok(empresaAtualizada);
	}
}
