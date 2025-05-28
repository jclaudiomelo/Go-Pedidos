package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.MesaDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.MesaService;
import com.GoPedidos.GoPedidos.Services.TokenService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mesas")
public class MesaController {

	@Autowired
	private MesaService mesaService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	//  Listar mesas de uma empresa
	@GetMapping("/todas")
	public ResponseEntity<List<MesaDTO>> listarMesas(@RequestHeader("Authorization") String authorizationHeader) {
		try {
			List<MesaDTO> mesas = mesaService.listarMesas(authorizationHeader);
			return ResponseEntity.ok(mesas);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// Criar uma nova mesa
	@PostMapping("/criar")
	public ResponseEntity<MesaDTO> criarMesa(@RequestHeader("Authorization") String authorizationHeader,
											 @RequestBody MesaDTO mesaDTO) {
		MesaDTO mesaCriada = mesaService.criarMesa(authorizationHeader, mesaDTO);
		return ResponseEntity.ok(mesaCriada);
	}

	// Atualizar mesa existente
	@PutMapping("/{id}")
	public ResponseEntity<MesaDTO> atualizarMesa(@RequestHeader("Authorization") String authorizationHeader,
												 @PathVariable Long id,
												 @RequestBody MesaDTO mesaDTO) {
		MesaDTO mesaAtualizada = mesaService.atualizarMesa(authorizationHeader, id, mesaDTO);
		return ResponseEntity.ok(mesaAtualizada);
	}

	// Deletar uma mesa
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarMesa(@RequestHeader("Authorization") String authorizationHeader,
											@PathVariable Long id) {
		mesaService.deletarMesa(authorizationHeader, id);
		return ResponseEntity.noContent().build();
	}
//	@DeleteMapping("/{id}")
//	public ResponseEntity<CustomException> deletarMesa(@RequestHeader("Authorization") String authorizationHeader,
//													   @PathVariable Long id) {
//		mesaService.deletarMesa(authorizationHeader, id);
//		CustomException response = new CustomException("mesa deletada.", HttpStatus.OK.value());
//
//		return ResponseEntity.ok(response);
//	}


}
