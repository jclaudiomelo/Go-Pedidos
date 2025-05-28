package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.CategoriaProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.ProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Services.CategoriaProdutoService;
import com.GoPedidos.GoPedidos.Services.CloudflareR2Service;
import com.GoPedidos.GoPedidos.Services.ProdutoService;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categorias-produto")
public class CategoriaProdutoController {

	@Autowired
	private CategoriaProdutoService categoriaProdutoService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private CloudflareR2Service cloudflareR2Service;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;

	@Value("${cloudflare.r2.bucket.categorias}")
	private String bucketCategorias;


	// 🔹 Criar Categoria
	@PostMapping("/criar")
	public ResponseEntity<CategoriaProdutoDTO> criarCategoria(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestBody CategoriaProdutoDTO categoriaDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		Long empresaId = usuarioAutenticado.empresaId();
		CategoriaProdutoDTO novaCategoria = categoriaProdutoService.criarCategoria(categoriaDTO, empresaId);
		return ResponseEntity.ok(novaCategoria);
	}


	// 🔹 Listar Categorias
	@GetMapping("/todos")
	public ResponseEntity<List<CategoriaProdutoDTO>> listarCategorias(
			@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		List<CategoriaProdutoDTO> categorias = categoriaProdutoService.listarCategorias(usuarioAutenticado.empresaId());
		return ResponseEntity.ok(categorias);
	}


	// 🔹 Listar Produtos por Categoria
	@GetMapping("/produtos/categoria/{categoriaId}")
	public ResponseEntity<List<ProdutoDTO>> listarProdutosPorCategoria(
			@PathVariable("categoriaId") Long categoriaId,
			@RequestHeader("Authorization") String authorizationHeader) {

		try {
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
			List<ProdutoDTO> produtos = produtoService.listarProdutosPorCategoria(categoriaId, usuarioAutenticado.empresaId());
			return ResponseEntity.ok(produtos);
		} catch (CustomException e) {
			return ResponseEntity.status(e.getStatusCode()).body(Collections.emptyList());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
		}
	}

	// 🔹 Buscar Categoria por ID
	@GetMapping("/{id}")
	public ResponseEntity<CategoriaProdutoDTO> buscarCategoriaPorId(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		CategoriaProdutoDTO categoria = categoriaProdutoService.buscarCategoriaPorId(id, usuarioAutenticado.empresaId());
		return ResponseEntity.ok(categoria);
	}

	// 🔹 Editar Categoria
	@PutMapping("/{id}")
	public ResponseEntity<CategoriaProdutoDTO> editarCategoria(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id,
			@RequestBody CategoriaProdutoDTO categoriaDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		CategoriaProdutoDTO categoriaAtualizada = categoriaProdutoService.editarCategoria(
				id,
				categoriaDTO,
				usuarioAutenticado.empresaId());
		return ResponseEntity.ok(categoriaAtualizada);
	}

	// 🔹 Deletar Categoria
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarCategoria(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		categoriaProdutoService.deletarCategoria(id, usuarioAutenticado.empresaId());
		return ResponseEntity.noContent().build();
	}


	@PutMapping("/{id}/imagem")
	public ResponseEntity<Map<String, Object>> uploadImagemCategoria(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id,
			@RequestParam("file") MultipartFile file) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		String imagemUrl = categoriaProdutoService.uploadImagemCategoria(id, file, usuarioAutenticado.empresaId());
		Map<String, Object> response = new HashMap<>();
		response.put("message", imagemUrl);
		response.put("statusCode", HttpStatus.CREATED.value());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}