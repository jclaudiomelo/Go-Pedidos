package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.LoginDTO;
import com.GoPedidos.GoPedidos.DTOS.MensagemResponseDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioResponseDTO;
import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.TokenConfirmacao;
import com.GoPedidos.GoPedidos.Models.Usuario;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.TokenConfirmacaoRepository;
import com.GoPedidos.GoPedidos.Respositories.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

	private static final int MAX_ATTEMPTS = 5;
	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private EmpresaRepository empresaRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private TokenConfirmacaoRepository tokenConfirmacaoRepository;
	@Autowired
	private EmailService emailService;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private TokenConfirmacaoRepository tokenRepository;
	@Autowired
	private TokenService tokenService;

	private GoogleIdTokenVerifier verifier;


	@Transactional
	public UsuarioResponseDTO criarUsuarioMaster(UsuarioDTO usuarioDTO) {
		try {
			// Verificar se o email já está em uso
			if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
				throw new CustomException("Email já está em uso", HttpStatus.CONFLICT.value());
			}
			// Criar o usuário
			Usuario usuario = new Usuario();
			usuario.setRole(Role.MASTER); // Definir role automaticamente como MASTER
			usuario.setEmail(usuarioDTO.email());
			usuario.setSenha(passwordEncoder.encode(usuarioDTO.senha()));
			usuario.setNome(usuarioDTO.nome());
			usuario.setCpf(usuarioDTO.cpf());
			usuario.setAtivo(false);
			// Criar uma empresa vazia apenas com um ID
			Empresa empresa = new Empresa();
			empresa = empresaRepository.save(empresa);
			// Associar a empresa ao usuário
			usuario.setEmpresa(empresa);
			// Salvar o usuário no banco de dados
			usuario = usuarioRepository.save(usuario);
			// Gerar um token de confirmação para o usuário
			TokenConfirmacao token = new TokenConfirmacao();
			token.setToken(UUID.randomUUID().toString());
			token.setExpiracao(LocalDateTime.now().plusHours(1));
			token.setUsuario(usuario);
			token.setTipo(TokenConfirmacao.TipoToken.CONFIRMACAO_EMAIL);
			tokenConfirmacaoRepository.save(token);
			// Enviar email de confirmação com o token
			emailService.enviarEmailDeConfirmacao(usuario.getEmail(), token.getToken());
			return new UsuarioResponseDTO(
					usuario.getId(),
					usuario.getNome(),
					usuario.getCpf(),
					usuario.getUsuarioUrl(),
					usuario.getEmail(),
					usuario.getRole().name(),
					usuario.getAtivo())
			;
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			// Lançar uma CustomException em caso de erro inesperado
			throw new CustomException("Erro ao criar usuário master: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	@Transactional
	public String autenticar(LoginDTO loginDTO) {
		Usuario usuario = usuarioRepository.findByEmail(loginDTO.email()).orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));
		if (!passwordEncoder.matches(loginDTO.senha(), usuario.getSenha())) {
			throw new CustomException("Senha incorreta", HttpStatus.UNAUTHORIZED.value());
		}
		if (!usuario.getAtivo()) {
			throw new CustomException("Usuário não está ativo. Por favor, valide seu e-mail.", HttpStatus.FORBIDDEN.value());
		}
		return tokenService.gerarToken(usuario);
	}


	// Metodo para verificar e autenticar o ID Token do Google
	@Transactional
	public String autenticarComGoogle(String idTokenString) {
		try {
			// Verifica o ID Token com o GoogleIdTokenVerifier
			GoogleIdToken idToken = verifier.verify(idTokenString);

			if (idToken == null) {
				throw new CustomException("Token inválido ou expirado", HttpStatus.UNAUTHORIZED.value());
			}

			// Extrai os dados do ID Token
			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String nome = (String) payload.get("name");

			// Busca ou cria o usuário no banco com os dados do Google
			Usuario usuario = buscarOuCriarUsuarioGoogle(email, nome);

			// Gera um JWT para autenticação do usuário
			return tokenService.gerarToken(usuario);
		} catch (Exception e) {
			throw new CustomException("Erro ao autenticar com Google: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

//	public void solicitarRecuperacaoSenha(String email) {
//
//		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));
//
//		// Remover tokens antigos de recuperação (se existirem)
//		tokenConfirmacaoRepository.deleteByUsuarioAndTipo(usuario, TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);
//		// Criar novo token
//		String token = UUID.randomUUID().toString();
//		TokenConfirmacao tokenRecuperacao = new TokenConfirmacao();
//		tokenRecuperacao.setToken(token);
//		tokenRecuperacao.setExpiracao(LocalDateTime.now().plusHours(1)); // Token expira em 1 hora
//		tokenRecuperacao.setUsuario(usuario);
//		tokenRecuperacao.setTipo(TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);

	/// /		tokenConfirmacaoRepository.save(tokenRecuperacao);
//		try {
//			tokenConfirmacaoRepository.save(tokenRecuperacao);
//		} catch (Exception e) {
//			e.printStackTrace(); // MOSTRA A EXCEÇÃO NO CONSOLE
//			throw new CustomException("Erro ao salvar token no banco: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//		// Enviar e-mail com o link
//		String link = "https://jcmtech.store/redefinir-senha?token=" + token;
//		System.out.println(link);
//		enviarEmailRecuperacao(usuario.getEmail(), link);
//
//	}
//	@Transactional
//	public void solicitarRecuperacaoSenha(String email) {
//		Usuario usuario = usuarioRepository.findByEmail(email)
//				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));
//		// Remover tokens antigos
//		tokenConfirmacaoRepository.deleteByUsuarioAndTipo(usuario, TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);
//		// Criar novo token
//		String token = UUID.randomUUID().toString();
//		TokenConfirmacao tokenRecuperacao = new TokenConfirmacao();
//		tokenRecuperacao.setToken(token);
//		tokenRecuperacao.setExpiracao(LocalDateTime.now().plusHours(1));
//		tokenRecuperacao.setUsuario(usuario);
//		tokenRecuperacao.setTipo(TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);
//		try {
//			tokenConfirmacaoRepository.save(tokenRecuperacao);
//			// Enviar e-mail com o link
//			String link = "https://jcmtech.store/redefinir-senha?token=" + token;
//			System.out.println(link);
//			enviarEmailRecuperacao(usuario.getEmail(), link);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new CustomException("Erro ao processar recuperação de senha: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//	}
	public void solicitarRecuperacaoSenha(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));

		Optional<TokenConfirmacao> tokenExistenteOpt = tokenConfirmacaoRepository
				.findByUsuarioAndTipo(usuario, TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);

		if (tokenExistenteOpt.isPresent()) {
			TokenConfirmacao tokenExistente = tokenExistenteOpt.get();

			if (tokenExistente.getExpiracao().isAfter(LocalDateTime.now())) {
				// Token ainda válido
				throw new CustomException("Já existe um código de recuperação válido enviado. Verifique seu e-mail.", HttpStatus.CONFLICT.value());
			} else {
				// Token expirado - remover para gerar novo
				tokenConfirmacaoRepository.delete(tokenExistente);
			}
		}

		// Criar novo token
		String token = UUID.randomUUID().toString();
		TokenConfirmacao tokenRecuperacao = new TokenConfirmacao();
		tokenRecuperacao.setToken(token);
		tokenRecuperacao.setExpiracao(LocalDateTime.now().plusMinutes(1));
		tokenRecuperacao.setUsuario(usuario);
		tokenRecuperacao.setTipo(TokenConfirmacao.TipoToken.RECUPERACAO_SENHA);

		try {
			tokenConfirmacaoRepository.save(tokenRecuperacao);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("Erro ao salvar token no banco: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		// Enviar e-mail com o link
		String link = "https://jcmtech.store/redefinir-senha?token=" + token;
		System.out.println(link);
		enviarEmailRecuperacao(usuario.getEmail(), link);
	}


	@Transactional
	private void enviarEmailRecuperacao(String email, String link) {
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setTo(email);
		mensagem.setSubject("Recuperação de Senha - GoPedidos");
		mensagem.setText("Clique no link abaixo para redefinir sua senha:\n" + link);
		mensagem.setFrom("gopedidos@jcmtech.store");
		mailSender.send(mensagem);
	}


	@Transactional
	public ResponseEntity<MensagemResponseDTO> validarTokenEAtivarUsuario(String token) {
		TokenConfirmacao tokenConfirmacao = tokenConfirmacaoRepository.findByToken(token).orElseThrow(() -> new CustomException("Token inválido ou expirado", HttpStatus.BAD_REQUEST.value()));
		// Verifica se o token expirou
		if (tokenConfirmacao.getExpiracao().isBefore(LocalDateTime.now())) {
			System.out.println("Token expirado em: " + tokenConfirmacao.getExpiracao());
			throw new CustomException("Token expirado", HttpStatus.BAD_REQUEST.value());
		}
		// Ativa o usuário
		Usuario usuario = tokenConfirmacao.getUsuario();
		usuario.setAtivo(true);
		usuarioRepository.save(usuario);
		tokenConfirmacaoRepository.delete(tokenConfirmacao);
		// Retorna sucesso com a resposta em formato JSON
		MensagemResponseDTO response = new MensagemResponseDTO("E-mail confirmado com sucesso!", HttpStatus.OK.value());
		return ResponseEntity.ok(response);
	}


	@Transactional
	public void redefinirSenha(String token, String novaSenha) {
		// Verificar se o token existe e não expirou
		TokenConfirmacao tokenRecuperacao = tokenConfirmacaoRepository.findByToken(token).orElseThrow(() -> new CustomException("Token inválido ou expirado", HttpStatus.BAD_REQUEST.value()));

		if (tokenRecuperacao.getExpiracao().isBefore(LocalDateTime.now())) {
			throw new CustomException("Token expirado", HttpStatus.BAD_REQUEST.value());
		}
		// Validações básicas
		if (token == null || token.trim().isEmpty()) {
			throw new CustomException("O campo 'token' é obrigatório.", HttpStatus.BAD_REQUEST.value());
		}
		if (novaSenha == null || novaSenha.trim().isEmpty()) {
			throw new CustomException("O campo 'novaSenha' é obrigatório.", HttpStatus.BAD_REQUEST.value());
		}
		// Verificar se o token é do tipo RECUPERACAO_SENHA
		if (tokenRecuperacao.getTipo() != TokenConfirmacao.TipoToken.RECUPERACAO_SENHA) {
			throw new CustomException("Token inválido para esta operação", HttpStatus.BAD_REQUEST.value());
		}

		// Atualizar a senha do usuário
		Usuario usuario = tokenRecuperacao.getUsuario();
		usuario.setSenha(passwordEncoder.encode(novaSenha));
		usuarioRepository.save(usuario);

		// Remover o token de recuperação de senha após a redefinição
		tokenConfirmacaoRepository.delete(tokenRecuperacao);


	}

	@Transactional
	public Usuario buscarOuCriarUsuarioGoogle(String email, String nome) {
		// Tenta encontrar o usuário pelo email
		Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

		// Se o usuário já existir, retorna ele
		if (usuarioOpt.isPresent()) {
			Usuario usuarioExistente = usuarioOpt.get();
			// Caso o usuário já tenha feito login via Google anteriormente
			if (usuarioExistente.getAtivo()) {
				return usuarioExistente;
			} else {
				throw new CustomException("Usuário já registrado com outro método de autenticação", HttpStatus.CONFLICT.value());
			}
		}

		// Caso o usuário não exista, cria um novo usuário com os dados do Google
		Usuario novoUsuario = new Usuario();
		novoUsuario.setEmail(email);
		novoUsuario.setNome(nome);
		novoUsuario.setSenha(null); // Usuário do Google não precisa de senha
		novoUsuario.setGoogleLogin(true); // Marcar que é login via Google
		novoUsuario.setAtivo(true); // Ou definir conforme sua regra de negócios

		return usuarioRepository.save(novoUsuario); // Salva e retorna o novo usuário
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return null;
	}
}