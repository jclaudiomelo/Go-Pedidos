package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.CriarColaboradorDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.DTOS.UsuarioDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioResponseDTO;
import com.GoPedidos.GoPedidos.Services.AuthService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthService authService;
	@Autowired
	private ValidarAcessoToken validarAcessoToken;


	@PostMapping("/criarColaborador")
	public ResponseEntity<UsuarioDTO> criarColaborador(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestBody CriarColaboradorDTO criarColaboradorDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		UsuarioDTO novoColaborador = authService.criarColaborador(criarColaboradorDTO, usuarioAutenticado,authorizationHeader);
		return ResponseEntity.status(HttpStatus.CREATED).body(novoColaborador);
	}


	@PutMapping("/atualizar/{id}")
	public ResponseEntity<?> atualizarUsuario(
			@PathVariable Long id,
			@RequestBody UsuarioDTO usuarioDTO,
			@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		UsuarioDTO usuarioAtualizado = authService.atualizarUsuario(id, usuarioDTO, authorizationHeader);
		return ResponseEntity.ok(usuarioAtualizado);
	}

	@PostMapping("/todos")
	public ResponseEntity<?> listarUsuarios(@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		List<UsuarioResponseDTO> usuarios = authService.listarUsuarios(authorizationHeader);
		return ResponseEntity.ok(usuarios);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> usuarioId(
			@PathVariable Long id,
			@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioDTO usuarioDTO = authService.usuarioId(id, authorizationHeader);
		return ResponseEntity.ok(usuarioDTO);
	}

	@PutMapping("/{id}/upload-foto")
	public ResponseEntity<String> uploadFotoUsuario(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id,
			@RequestParam("file") MultipartFile file) throws IOException {
		String imagemUrl = authService.uploadFotoUsuario(id, file, authorizationHeader);
		return ResponseEntity.ok(imagemUrl);
	}


	@DeleteMapping("/deletar/{id}")
	public ResponseEntity<?> deletarUsuario(
			@PathVariable Long id,
			@RequestHeader("Authorization") String authorizationHeader) {
		String mensagem = String.valueOf(authService.deletarUsuario(id, authorizationHeader));
		return ResponseEntity.ok(mensagem);
	}

}