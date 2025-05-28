package com.GoPedidos.GoPedidos.Utils;

import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.TokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidarAcessoToken {

	@Autowired
	private TokenService tokenService;

	// Valida e retorna os dados do usuário autenticado com base no token
	public UsuarioAutenticado validarUsuarioBase(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new CustomException("Token inválido ou ausente", HttpStatus.UNAUTHORIZED.value());
		}

		String token = authorizationHeader.substring(7);
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(token);

		Long usuarioId = decodedJWT.getClaim("id").asLong();
		String role = decodedJWT.getClaim("role").asString();  // Aqui pegamos o role
		Long empresaId = decodedJWT.getClaim("empresa_id").asLong();

		return new UsuarioAutenticado(usuarioId, empresaId, role);
	}


	// Verifica se é MASTER ou ADMIN
	public UsuarioAutenticado validarMasterOuAdmin(String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarUsuarioBase(authorizationHeader);

		if (!"MASTER".equals(usuarioAutenticado.role()) && !"ADMIN".equals(usuarioAutenticado.role())) {
			throw new CustomException("Apenas usuários MASTER ou ADMIN podem realizar essa ação", HttpStatus.FORBIDDEN.value());
		}

		return usuarioAutenticado;
	}

	// Valida qualquer usuário, mas sem permissão extra
	public UsuarioAutenticado validarUsuario(String authorizationHeader) {
		return validarUsuarioBase(authorizationHeader);
	}
}
