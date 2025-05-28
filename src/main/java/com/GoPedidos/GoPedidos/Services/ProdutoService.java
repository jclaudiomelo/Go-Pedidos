package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.CategoriaProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.ProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Cardapio;
import com.GoPedidos.GoPedidos.Models.CategoriaProduto;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.Produto;
import com.GoPedidos.GoPedidos.Respositories.*;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private CategoriaProdutoRepository categoriaProdutoRepository;

	@Autowired
	private CardapioRepository cardapioRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;


	@Autowired
	private CloudflareR2Service cloudflareR2Service;

	@Autowired
	private ItemPedidoRepository itemPedidoRepository;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;

	@Value("${cloudflare.r2.bucket.categorias}")
	private String bucketCategorias;

	@Transactional
	public ProdutoDTO criarProduto(ProdutoDTO produtoDTO, Long empresaId, String authorizationHeader) {
		// Valida o acesso do usuário com base no token
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);

		// Verifica se o usuário tem permissão para criar produtos (role MASTER ou ADMIN)
		if (!(usuarioAutenticado.role().equals("MASTER") || usuarioAutenticado.role().equals("ADMIN"))) {
			throw new CustomException("Usuário não tem permissão para criar produtos.", HttpStatus.UNAUTHORIZED.value());
		}
		if (produtoRepository.existsByNome(produtoDTO.nome())) {
			throw new CustomException("Já existe uma produto com o mesmo nome .", HttpStatus.CONFLICT.value());
		}

		// Verifica se o usuário pertence à empresa informada no token
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}

		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new CustomException("Empresa não encontrada!", HttpStatus.NOT_FOUND.value()));

		Cardapio cardapio = null;
		if (produtoDTO.cardapioId() != null) {
			cardapio = cardapioRepository.findById(produtoDTO.cardapioId())
					.orElseThrow(() -> new CustomException("Cardápio não encontrado!", HttpStatus.NOT_FOUND.value()));
		}

		// CategoriaProduto não é mais obrigatória
		// Verifica se o produtoDTO contém uma categoria
		CategoriaProduto categoriaProduto = null;
		if (produtoDTO.categoriaProduto() != null && produtoDTO.categoriaProduto().categoriaId() != null) {
			categoriaProduto = categoriaProdutoRepository.findById(produtoDTO.categoriaProduto().categoriaId())
					.orElseThrow(() -> new CustomException("Categoria do produto não encontrada!", HttpStatus.NOT_FOUND.value()));
		}


		Produto novoProduto = new Produto();
		novoProduto.setNome(produtoDTO.nome());
		novoProduto.setDescricao(produtoDTO.descricao());
		novoProduto.setPreco(produtoDTO.preco());
		novoProduto.setCategoriaProduto(categoriaProduto);
		novoProduto.setVisivel(produtoDTO.visivel());
		novoProduto.setCardapio(cardapio);
		novoProduto.setImagemUrl(produtoDTO.imagemUrl());
		novoProduto.setEmpresa(empresa);
		Produto produtoSalvo = produtoRepository.save(novoProduto);
		return mapearProdutoParaDTO(produtoSalvo);
	}


	// Listar produtos da empresa
	@Transactional
	public List<ProdutoDTO> listarProdutos(Long empresaId, String authorizationHeader) {
		// Valida o acesso da empresa
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}

		List<Produto> produtos = produtoRepository.findByEmpresaId(empresaId);
		return produtos.stream()
				.map(this::mapearProdutoParaDTO)
				.collect(Collectors.toList());
	}

	// Buscar produto por ID
	@Transactional
	public ProdutoDTO buscarProdutoPorId(Long id, Long empresaId, String authorizationHeader) {
		// Valida o acesso da empresa
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}
		Produto produto = produtoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value()));
		if (!produto.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Produto não pertence à empresa do usuário!", HttpStatus.UNAUTHORIZED.value());
		}
		return mapearProdutoParaDTO(produto);
	}


	// Editar produto
	@Transactional
	public ProdutoDTO editarProduto(Long id, ProdutoDTO produtoDTO, Long empresaId, String authorizationHeader) {
		// Valida o acesso da empresa
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}

		Produto produto = produtoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value()));

		if (!produto.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Usuário não pode editar produtos de outra empresa.", HttpStatus.UNAUTHORIZED.value());
		}

		if (produtoDTO.nome() != null) produto.setNome(produtoDTO.nome());
		if (produtoDTO.descricao() != null) produto.setDescricao(produtoDTO.descricao());
		if (produtoDTO.preco() != null) produto.setPreco(produtoDTO.preco());
		produto.setVisivel(produtoDTO.visivel());
		if (produtoDTO.categoriaProduto() != null && produtoDTO.categoriaProduto().categoriaId() != null) {
			CategoriaProduto categoriaProduto = categoriaProdutoRepository.findById(produtoDTO.categoriaProduto().categoriaId())
					.orElseThrow(() -> new CustomException("Categoria do produto não encontrada!", HttpStatus.NOT_FOUND.value()));
			produto.setCategoriaProduto(categoriaProduto);
		}


//		if (produtoDTO.cardapioId() != null) {
//			Cardapio cardapio = cardapioRepository.findById(produtoDTO.cardapioId())
//					.orElseThrow(() -> new CustomException("Cardápio não encontrado!", HttpStatus.NOT_FOUND.value()));
//
//			produto.setCardapio(cardapio);
//		}
		Produto produtoAtualizado = produtoRepository.save(produto);
		return mapearProdutoParaDTO(produtoAtualizado);
	}

	@Transactional
	public void deletarProduto(Long id, Long empresaId, String authorizationHeader) {
		// Valida o acesso da empresa
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarUsuarioBase(authorizationHeader);
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}
		// Verifica se o produto existe
		Produto produto = produtoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value()));
		if (!produto.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Usuário não pode excluir produtos de outra empresa.", HttpStatus.UNAUTHORIZED.value());
		}
		try {
			produtoRepository.delete(produto);
		} catch (DataIntegrityViolationException e) {
			throw new CustomException("Este produto não pode ser excluído porque está vinculado a pedidos.", HttpStatus.BAD_REQUEST.value());
		} catch (Exception e) {
			throw new CustomException("Erro ao excluir o produto: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	// Atualizar imagem do produto
	@Transactional
	public void atualizarImagemProduto(Long produtoId, String imagemUrl) {
		Produto produto = produtoRepository.findById(produtoId)
				.orElseThrow(() -> new CustomException("Produto não encontrado!", HttpStatus.NOT_FOUND.value()));

		produto.setImagemUrl(imagemUrl);
		produtoRepository.save(produto);
	}

	// Metodo auxiliar para verificar se o usuário pertence à empresa
	@Transactional
	private void validarAcessoEmpresa(Long empresaId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	// Metodo auxiliar para converter Produto em ProdutoDTO
	private ProdutoDTO mapearProdutoParaDTO(Produto produto) {
		CategoriaProdutoDTO categoriaProdutoDTO = null;
		if (produto.getCategoriaProduto() != null) {
			CategoriaProduto categoriaProduto = produto.getCategoriaProduto();
			categoriaProdutoDTO = new CategoriaProdutoDTO(
					categoriaProduto.getCategoriaId(),
					categoriaProduto.getNomeCategoria(),
					categoriaProduto.getDescricaoCategoria(),
					categoriaProduto.getCategoriaUrlImg(),
					categoriaProduto.getEmpresa() != null ? categoriaProduto.getEmpresa().getId() : null,
					categoriaProduto.getVisivel()
			);
		}

		ProdutoDTO produtoDTO = new ProdutoDTO(
				produto.getProdutoId(),
				produto.getNome(),
				produto.getDescricao(),
				produto.getPreco(),
				categoriaProdutoDTO,
				produto.getVisivel(),
				produto.getCardapio() != null ? produto.getCardapio().getCardapio_id() : null,
				produto.getImagemUrl(),
				produto.getEmpresa().getId()
		);
		return produtoDTO;
	}

	@Transactional
	public List<ProdutoDTO> listarProdutosPorCategoria(Long categoriaId, Long empresaId) {
		// Recuperando produtos da categoria e empresa especificadas
		List<Produto> produtos = produtoRepository.findByCategoriaProduto_CategoriaIdAndEmpresa_Id(categoriaId, empresaId);
		// Logando os produtos encontrados
		if (produtos != null && !produtos.isEmpty()) {
			produtos.forEach(produto -> System.out.println(produto));
		} else {
		}
		// Se a lista for nula ou vazia, retorne uma lista vazia
		if (produtos == null || produtos.isEmpty()) {
			return new ArrayList<>();
		}
		// Convertendo para DTO e retornando
		List<ProdutoDTO> produtoDTOs = produtos.stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
		return produtoDTOs;
	}

	@Transactional
	private ProdutoDTO toDTO(Produto produto) {
		CategoriaProdutoDTO categoriaDTO = null;
		if (produto.getCategoriaProduto() != null) {
			categoriaDTO = new CategoriaProdutoDTO(
					produto.getCategoriaProduto().getCategoriaId(),
					produto.getCategoriaProduto().getNomeCategoria(),
					produto.getCategoriaProduto().getDescricaoCategoria(),
					produto.getCategoriaProduto().getCategoriaUrlImg(),
					(produto.getCategoriaProduto().getEmpresa() != null) ? produto.getCategoriaProduto().getEmpresa().getId() : null,
					produto.getCategoriaProduto().getVisivel()
			);
		}

		ProdutoDTO produtoDTO = new ProdutoDTO(
				produto.getProdutoId() != null ? produto.getProdutoId() : 0L, // Garante tipo `long`
				produto.getNome(),
				produto.getDescricao(),
				produto.getPreco(),
				categoriaDTO,
				(produto.getVisivel() != null) ? produto.getVisivel() : false, // Garante `boolean`
				produto.getEmpresa() != null ? produto.getEmpresa().getId() : null,
				produto.getImagemUrl(),
				produto.getCardapio() != null ? produto.getCardapio().getCardapio_id() : null // Agora passa o `cardapioId` corretamente
		);
		return produtoDTO;
	}


}