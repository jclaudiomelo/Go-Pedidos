package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.*;
import com.GoPedidos.GoPedidos.Respositories.TokenConfirmacaoRepository;
import com.GoPedidos.GoPedidos.Services.AuthService;
import com.GoPedidos.GoPedidos.Services.RecaptchaService;
import com.GoPedidos.GoPedidos.Services.TokenService;
import com.GoPedidos.GoPedidos.Services.UsuarioService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;
	@Autowired
	private AuthService authService;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private ValidarAcessoToken validarAcessoToken;
	@Autowired
	private RecaptchaService recaptchaService;
	@Getter
	@Value("${google.id}")
	private String clientId;
	@Autowired
	private TokenConfirmacaoRepository tokenConfirmacaoRepository;


	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
		String token = usuarioService.autenticar(loginDTO);
		return ResponseEntity.ok(Map.of("token", token));
	}

	@CrossOrigin(origins = "*")
	@PostMapping("/recaptcha")
	public Map<String, Boolean> validateRecaptcha(@RequestBody Map<String, String> request) throws IOException, InterruptedException {
		String token = request.get("token");
		boolean isValid = recaptchaService.verifyToken(token);
		return Map.of("success", isValid);
	}

	@PostMapping("/criar-conta")
	public ResponseEntity<?> criarUsuario(@RequestBody @Valid UsuarioDTO usuarioDTO) {
		UsuarioResponseDTO usuarioCriado = usuarioService.criarUsuarioMaster(usuarioDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
	}

	@GetMapping("/confirmar")
	public ResponseEntity<MensagemResponseDTO> confirmarEmail(@RequestParam String token) {
		return usuarioService.validarTokenEAtivarUsuario(token);
	}

	//	@PostMapping("/solicitar-redefinicao-senha")
//	public ResponseEntity<?> solicitarRedefinicaoSenha(@RequestBody Map<String, String> request) {
//		String email = request.get("email");
//		usuarioService.solicitarRecuperacaoSenha(email);
//		MensagemResponseDTO resposta = new MensagemResponseDTO("Email de recuperação enviado.", HttpStatus.OK.value());
//		return ResponseEntity.ok(resposta);
//	}
	@PostMapping("/solicitar-redefinicao-senha")
	public ResponseEntity<MensagemResponseDTO> solicitarRedefinicaoSenha(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		usuarioService.solicitarRecuperacaoSenha(email);
		MensagemResponseDTO resposta = new MensagemResponseDTO("Email de recuperação enviado.", HttpStatus.OK.value());
		return ResponseEntity.ok(resposta);
	}


	@CrossOrigin(origins = "*") // Ou especifique o domínio do frontend
	@PostMapping("/redefinir-senha")
	public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
		usuarioService.redefinirSenha(request.token(), request.senha());
		MensagemResponseDTO resposta = new MensagemResponseDTO("Senha Alterada com Sucesso", HttpStatus.OK.value());
		return ResponseEntity.ok(resposta);
	}

	@PostMapping("/google")
	public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
		String idTokenString = request.get("idTokenString");
		String token = usuarioService.autenticarComGoogle(idTokenString);
		return ResponseEntity.ok(Map.of("token", token));
	}
}

