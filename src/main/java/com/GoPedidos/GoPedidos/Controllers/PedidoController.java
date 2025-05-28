package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.PedidoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.RabbitMQ.RabbitMQConfig;
import com.GoPedidos.GoPedidos.Services.PedidoService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	@Autowired
	private PedidoService pedidoService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	// Criar um pedido (Somente usuários autenticados da mesma empresa)
	@PostMapping("/criar")
	public ResponseEntity<PedidoDTO> criarPedido(@RequestHeader("Authorization") String authorizationHeader,
												 @RequestBody PedidoDTO pedidoDTO) {
		try {
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
			PedidoDTO novoPedido = pedidoService.criarPedido(pedidoDTO, usuarioAutenticado.empresaId());
			return ResponseEntity.ok(novoPedido);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// Listar pedidos (Filtra por empresa ou cliente)
	@GetMapping("/listar")
	public ResponseEntity<List<PedidoDTO>> listarPedidos(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			// 🔍 Validando o usuário autenticado
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);

			// 🔄 Obtendo os pedidos da empresa do usuário autenticado
			List<PedidoDTO> pedidos = pedidoService.listarPedidosPorEmpresa(usuarioAutenticado.empresaId());

			return ResponseEntity.ok(pedidos);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	@GetMapping("/{pedidoId}")
	public ResponseEntity<PedidoDTO> buscarPedidoPorId(
			@PathVariable Long pedidoId,
			@RequestHeader("Authorization") String authorizationHeader) {
		try {
			// Obtém o ID da empresa do usuário autenticado
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);

			// Busca o pedido
			PedidoDTO pedidoDTO = pedidoService.buscarPedidoPorId(pedidoId, usuarioAutenticado.empresaId());
			return ResponseEntity.ok(pedidoDTO);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}


}
