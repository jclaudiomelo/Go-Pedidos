package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.CardapioDTO;
import com.GoPedidos.GoPedidos.DTOS.CardapioProdutosDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Services.CardapioService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cardapios")
public class CardapioController {

	@Autowired
	private CardapioService cardapioService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;


	// 🔹 Criar cardápio
	@PostMapping("/criar")
	public ResponseEntity<CardapioDTO> criarCardapio(@RequestHeader("Authorization") String authorizationHeader,
													 @RequestBody CardapioDTO cardapioDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		CardapioDTO novoCardapio = cardapioService.criarCardapio(cardapioDTO, usuarioAutenticado.empresaId());
		return ResponseEntity.ok(novoCardapio);
	}

	// 🔹 Editar cardápio
	@PutMapping("/{id}")
	public ResponseEntity<CardapioDTO> editarCardapio(@RequestHeader("Authorization") String authorizationHeader,
													  @PathVariable Long id,
													  @RequestBody CardapioDTO cardapioDTO) {
		validarAcessoToken.validarUsuario(authorizationHeader);
		CardapioDTO cardapioAtualizado = cardapioService.editarCardapio(id, cardapioDTO, authorizationHeader);
		return ResponseEntity.ok(cardapioAtualizado);
	}

	// 🔹 Achar cardápio por id
	@GetMapping("/{cardapioId}")
	public ResponseEntity<CardapioProdutosDTO> buscarCardapioPorId(@PathVariable Long cardapioId,
																   @RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		CardapioProdutosDTO cardapioProdutosDTO = cardapioService.buscarCardapioPorId(cardapioId, usuarioAutenticado.empresaId());
		return ResponseEntity.ok(cardapioProdutosDTO);
	}

	// 🔹 Deletar cardápio
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarCardapio(@RequestHeader("Authorization") String authorizationHeader,
												@PathVariable Long id) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		cardapioService.deletarCardapio(id, usuarioAutenticado.empresaId());
		return ResponseEntity.noContent().build();
	}

	// 🔹 Listar todos os cardápios de uma empresa
	@GetMapping("/todos")
	public ResponseEntity<List<CardapioDTO>> listarCardapios(@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		List<CardapioDTO> cardapios = cardapioService.listarCardapios(usuarioAutenticado.empresaId());
		return ResponseEntity.ok(cardapios);
	}

}
