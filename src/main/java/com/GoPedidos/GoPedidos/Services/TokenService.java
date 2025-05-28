package com.GoPedidos.GoPedidos.Services;


import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.TokenConfirmacao;
import com.GoPedidos.GoPedidos.Models.Usuario;
import com.GoPedidos.GoPedidos.Respositories.RetornoTokenRepository;
import com.GoPedidos.GoPedidos.Respositories.TokenConfirmacaoRepository;
import com.GoPedidos.GoPedidos.Respositories.UsuarioRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

	@Value("${jwt.secret}")
	private String secret;

	private RetornoTokenRepository retornoTokenRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private TokenConfirmacaoRepository tokenConfirmacaoRepository;

	@Autowired
	private JavaMailSender mailSender;


	// Gera o token JWT
	public String gerarToken(RetornoTokenRepository retornoTokenRepository) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.create()
					.withIssuer("auth-api")
					.withClaim("id", retornoTokenRepository.getId())
					.withClaim("role", retornoTokenRepository.getRole().name())
					.withClaim("nome", retornoTokenRepository.getNome())
					.withClaim("empresa_id", retornoTokenRepository.getEmpresa().getId()) // Garante que empresa_id está incluído
					.withSubject(retornoTokenRepository.getEmail())
					.withExpiresAt(gerarDataExpira())
					.sign(algorithm);
		} catch (JWTCreationException exception) {
			throw new CustomException("Erro ao gerar o token: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}


	// Valida o token JWT
	public DecodedJWT validarTokenUsuario(String token) {
		try {
			if (token.startsWith("Bearer ")) {
				token = token.substring(7); // Corrige a extração do token
			}

			Algorithm algorithm = Algorithm.HMAC256(secret);
			DecodedJWT decodedJWT = JWT.require(algorithm)
					.withIssuer("auth-api")
					.build()
					.verify(token);

			if (decodedJWT.getExpiresAt().before(new Date())) {
				throw new CustomException("O token expirou em " + decodedJWT.getExpiresAt(), HttpStatus.UNAUTHORIZED.value());
			}

			return decodedJWT;
		} catch (JWTVerificationException exception) {
			throw new CustomException("Token inválido ou mal formatado: " + exception.getMessage(), HttpStatus.UNAUTHORIZED.value());
		}
	}




	// Define a data de expiração do token
	private Instant gerarDataExpira() {
		return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
	}
}
