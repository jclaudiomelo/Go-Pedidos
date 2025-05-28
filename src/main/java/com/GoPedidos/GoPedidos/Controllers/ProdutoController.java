package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.ProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
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
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	@Autowired
	private CloudflareR2Service cloudflareR2Service;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;

	// 🔹 Criar Produto
	@PostMapping("/criar")
	public ResponseEntity<ProdutoDTO> criarProduto(@RequestHeader("Authorization") String authorizationHeader,
												   @RequestBody ProdutoDTO produtoDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		ProdutoDTO novoProduto = produtoService.criarProduto(produtoDTO, usuarioAutenticado.empresaId(), authorizationHeader);
		return ResponseEntity.ok(novoProduto);
	}

	// 🔹 Listar Produtos
	@GetMapping("/todos")
	public ResponseEntity<List<ProdutoDTO>> listarProdutos(@RequestHeader("Authorization") String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		List<ProdutoDTO> produtos = produtoService.listarProdutos(usuarioAutenticado.empresaId(), authorizationHeader);
		return ResponseEntity.ok(produtos);
	}

	// 🔹 Editar Produto
	@PutMapping("/{id}")
	public ResponseEntity<ProdutoDTO> editarProduto(@RequestHeader("Authorization") String authorizationHeader,
													@PathVariable Long id,
													@RequestBody ProdutoDTO produtoDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		ProdutoDTO produtoAtualizado = produtoService.editarProduto(id, produtoDTO, usuarioAutenticado.empresaId(), authorizationHeader);
		return ResponseEntity.ok(produtoAtualizado);

	}

	@GetMapping("/{id}")
	public ResponseEntity<ProdutoDTO> buscarProdutoPorId(@RequestHeader("Authorization") String authorizationHeader,
														 @PathVariable Long id) {
		// Valida o usuário autenticado e obtém o empresaId
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
		// Busca o produto
		ProdutoDTO produtoDTO = produtoService.buscarProdutoPorId(id, usuarioAutenticado.empresaId(), authorizationHeader);
		return ResponseEntity.ok(produtoDTO);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarProduto(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id
	) {
		// Valida o acesso do usuário (verifica se ele é MASTER ou ADMIN)
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);

		// Chama o serviço para deletar o produto
		produtoService.deletarProduto(id, usuarioAutenticado.empresaId(), authorizationHeader);

		// Retorna resposta com status 200 OK após a exclusão bem-sucedida
		return ResponseEntity.ok().build();
	}



	// 🔹 Upload de Imagem
	@PutMapping("/{id}/imagem")
	public ResponseEntity<String> uploadImagemProduto(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable Long id,
			@RequestParam("file") MultipartFile file) {
		try {
			// Valida o usuário autenticado
			UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuario(authorizationHeader);
			// Verifica se o usuário tem permissão para a empresa do produto
			ProdutoDTO produto = produtoService.buscarProdutoPorId(id, usuarioAutenticado.empresaId(), authorizationHeader);
			if (!produto.empresaId().equals(usuarioAutenticado.empresaId())) {
				throw new CustomException("Usuário não tem permissão para atualizar produtos de outra empresa.", HttpStatus.UNAUTHORIZED.value());
			}
			// Validação do arquivo de imagem
			if (file.isEmpty()) {
				throw new CustomException("Arquivo de imagem não encontrado.", HttpStatus.BAD_REQUEST.value());
			}
			// Verifica o tipo do arquivo (por exemplo, apenas imagens)
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				throw new CustomException("O arquivo enviado não é uma imagem válida.", HttpStatus.BAD_REQUEST.value());
			}
			// Verifica o tamanho do arquivo (exemplo: máximo 5MB)
			long maxSize = 5 * 1024 * 1024; // 5MB
			if (file.getSize() > maxSize) {
				throw new CustomException("O arquivo de imagem é muito grande. O tamanho máximo permitido é 5MB.", HttpStatus.BAD_REQUEST.value());
			}
			// Gera um nome único para o arquivo
			String fileName = "produtos_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
			// Obtém o nome da imagem antiga (se existir)
			String oldFileName = produto.imagemUrl() != null ? produto.imagemUrl().replace(publicUrl + "/", "") : null;
			// Faz o upload da nova imagem para o Cloudflare R2
			String imagemUrl = cloudflareR2Service.uploadFile(file, fileName, oldFileName, bucketProdutos);
			// Atualiza a URL da imagem no banco de dados
			produtoService.atualizarImagemProduto(id, imagemUrl);
			// Retorna a URL pública da imagem
//			return ResponseEntity.ok(imagemUrl);
			throw new CustomException(imagemUrl, HttpStatus.CREATED.value());
			// Retorna a URL da imagem
//			return ResponseEntity.status(HttpStatus.CREATED).body(imagemUrl);
		} catch (IOException e) {
			throw new CustomException("Erro ao processar o arquivo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (CustomException e) {
			throw e; // Re-lança exceções personalizadas com código adequado
		} catch (Exception e) {
			// Tratamento genérico de erros
			throw new CustomException("Erro inesperado ao fazer upload da imagem: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}