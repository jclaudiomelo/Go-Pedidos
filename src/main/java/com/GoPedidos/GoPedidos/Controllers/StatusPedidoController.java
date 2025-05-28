package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.StatusPedidoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Services.StatusPedidoService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status-pedido")
public class StatusPedidoController {

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	@Autowired
	private StatusPedidoService statusPedidoService;

	@PostMapping("/criar")
	public ResponseEntity<StatusPedidoDTO> criarStatusPedido(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestBody StatusPedidoDTO statusPedidoDTO) {

		try {
			// Validando o usuário autenticado
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);

			// Criando o status de pedido
			StatusPedidoDTO novoStatusPedido = statusPedidoService.criarStatusPedido(statusPedidoDTO, usuarioAutenticado.empresaId());
			return ResponseEntity.status(HttpStatus.CREATED).body(novoStatusPedido);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	@GetMapping("/todos")
	public ResponseEntity<List<StatusPedidoDTO>> listarStatusPedidos(
			@RequestHeader("Authorization") String authorizationHeader) {

		try {
			// Validando o usuário autenticado
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);

			// Listando os status de pedidos da empresa do usuário
			List<StatusPedidoDTO> statusPedidos = statusPedidoService.listarStatusPedidos(usuarioAutenticado.empresaId());
			return ResponseEntity.ok(statusPedidos);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
}
