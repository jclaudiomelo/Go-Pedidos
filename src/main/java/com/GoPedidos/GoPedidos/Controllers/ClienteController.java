package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.ClienteDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.ClienteService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;


	// 🔹 Criar cliente (Qualquer usuário autenticado)
	@PostMapping("/criar")
	public ResponseEntity<ClienteDTO> criarCliente(@RequestHeader("Authorization") String authorizationHeader,
												   @RequestBody ClienteDTO clienteDTO) {
		try {
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
			ClienteDTO novoCliente = clienteService.criarCliente(clienteDTO, usuarioAutenticado.empresaId());
			return ResponseEntity.ok(novoCliente);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// 🔹 Editar cliente (Qualquer usuário autenticado)
	@PutMapping("/{id}")
	public ResponseEntity<ClienteDTO> editarCliente(@RequestHeader("Authorization") String authorizationHeader,
													@PathVariable Long id,
													@RequestBody ClienteDTO clienteDTO) {
		try {
			validarAcessoToken.validarUsuario(authorizationHeader);
			ClienteDTO clienteAtualizado = clienteService.editarCliente(id, clienteDTO, authorizationHeader);
			return ResponseEntity.ok(clienteAtualizado);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// 🔹 Deletar cliente (Apenas ADMIN ou MASTER)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarCliente(@RequestHeader("Authorization") String authorizationHeader,
											   @PathVariable Long id) {
		try {
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
			// 🔄 Chama o serviço para deletar, verificando a empresa
			clienteService.deletarCliente(id, usuarioAutenticado.empresaId());
			return ResponseEntity.noContent().build();
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
}
