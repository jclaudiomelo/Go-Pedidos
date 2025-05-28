package com.GoPedidos.GoPedidos.Configurações;

import com.GoPedidos.GoPedidos.DTOS.MensagemResponseDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.TokenService;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	@Autowired
	private TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);

				Map<String, Claim> claims = tokenService.validarTokenUsuario(token).getClaims();

				if (claims != null) {
					String email = claims.get("sub").asString();
					String role = claims.get("role").asString();
					Long id = claims.get("id").asLong();
					Long empresaId = claims.get("empresa_id").asLong(); // Garantindo que a empresa está incluída

					if (email != null && role != null && empresaId != null) {
						List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

						// Criar o objeto UsuarioAutenticado com os detalhes necessários
						UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado(id, empresaId, role);

						// Criando o objeto de autenticação
						UsernamePasswordAuthenticationToken authentication =
								new UsernamePasswordAuthenticationToken(usuarioAutenticado, null, authorities);

						// Configurar os detalhes do contexto de segurança
						authentication.setDetails(Map.of("email", email, "role", role, "empresa_id", empresaId));

						// Adiciona o objeto de autenticação no SecurityContext
						SecurityContextHolder.getContext().setAuthentication(authentication);
					} else {
						throw new CustomException("Token inválido: informações do usuário incompletas.", HttpStatus.BAD_REQUEST.value());
					}
				} else {
					throw new CustomException("Token inválido: sem claims.", HttpStatus.BAD_REQUEST.value());
				}
			}

			filterChain.doFilter(request, response);
		} catch (CustomException e) {
			// Configura a resposta para JSON
			response.setStatus(e.getStatusCode());
			response.setContentType("application/json");

			// Cria o objeto de resposta de erro
			MensagemResponseDTO errorResponse = new MensagemResponseDTO(e.getMessage(), e.getStatusCode());

			// Converte o objeto para JSON e escreve na resposta
			ObjectMapper objectMapper = new ObjectMapper();
			response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
		} catch (TokenExpiredException e) {
			// Tratamento específico para token expirado
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json");

			MensagemResponseDTO errorResponse = new MensagemResponseDTO("Token expirado.", HttpStatus.UNAUTHORIZED.value());
			ObjectMapper objectMapper = new ObjectMapper();
			response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
		} catch (Exception e) {
			// Captura outras exceções inesperadas
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setContentType("application/json");

			MensagemResponseDTO errorResponse = new MensagemResponseDTO("Erro interno no servidor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
			ObjectMapper objectMapper = new ObjectMapper();
			response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
		}
	}
}