package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.CategoriaProdutoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.CategoriaProduto;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Respositories.CategoriaProdutoRepository;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CategoriaProdutoService {

	@Autowired
	private CategoriaProdutoRepository categoriaProdutoRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private CloudflareR2Service cloudflareR2Service;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;

	@Value("${cloudflare.r2.bucket.categorias}")
	private String bucketCategorias;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;


	@Transactional
	public List<CategoriaProdutoDTO> listarCategorias(Long empresaId) {
		return categoriaProdutoRepository.findByEmpresaId(empresaId)
				.stream()
				.map(this::toDTO)
				.collect(Collectors.toList());
	}

	@Transactional
	public CategoriaProdutoDTO buscarCategoriaPorId(Long id, Long empresaId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		// Verifica se o usuário pertence à empresa informada
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}
		// Busca a categoria pelo ID
		CategoriaProduto categoriaProduto = categoriaProdutoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Categoria não encontrada!", HttpStatus.NOT_FOUND.value()));

		// Verifica se a categoria pertence à empresa do usuário
		if (!categoriaProduto.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Categoria não pertence à empresa do usuário!", HttpStatus.UNAUTHORIZED.value());
		}

		// Retorna o DTO com os dados da categoria
		return new CategoriaProdutoDTO(
				categoriaProduto.getCategoriaId(),
				categoriaProduto.getNomeCategoria(),
				categoriaProduto.getCategoriaUrlImg(),
				categoriaProduto.getDescricaoCategoria(),
				categoriaProduto.getEmpresa().getId(),
				categoriaProduto.getVisivel()
		);
	}

//	Transactional
//	public CategoriaProdutoDTO criarCategoria(CategoriaProdutoDTO categoriaDTO, Long empresaId) {
//		// 🔹 Buscar empresa pelo ID
//		Empresa empresa = empresaRepository.findById(empresaId)
//				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));
//
//		// 🔹 Criar nova categoria associada à empresa já persistida
//		CategoriaProduto categoria = new CategoriaProduto();
//		categoria.setNomeCategoria(categoriaDTO.nomeCategoria());
//		categoria.setDescricaoCategoria(categoriaDTO.descricaoCategoria());
//		categoria.setCategoriaUrlImg(categoriaDTO.categoriaUrlImg());
//		categoria.setEmpresa(empresa); // Associar empresa já existente
//
//		// Salva a categoria no banco de dados
//		categoria = categoriaProdutoRepository.save(categoria);
//		return toDTO(categoria);
//	}
	@Transactional
	public CategoriaProdutoDTO criarCategoria(CategoriaProdutoDTO categoriaDTO, Long empresaId) {
		// Verifica se já existe uma categoria com o mesmo nome para a mesma empresa
		if (categoriaProdutoRepository.existsByNomeCategoriaAndEmpresa_Id(categoriaDTO.nomeCategoria(), empresaId)) {
			throw new CustomException("Já existe uma categoria com o mesmo nome para esta empresa.", HttpStatus.CONFLICT.value());
		}
		// Busca a empresa pelo ID
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));
		// Cria a categoria e associa a empresa
		CategoriaProduto categoria = new CategoriaProduto();
		categoria.setNomeCategoria(categoriaDTO.nomeCategoria());
		categoria.setDescricaoCategoria(categoriaDTO.descricaoCategoria());
		categoria.setCategoriaUrlImg(categoriaDTO.categoriaUrlImg());
		categoria.setVisivel(categoriaDTO.visivel());
		categoria.setEmpresa(empresa);

		// Salva a categoria no banco de dados
		categoria = categoriaProdutoRepository.save(categoria);
		return new CategoriaProdutoDTO(
				categoria.getCategoriaId(),
				categoria.getNomeCategoria(),
				categoria.getDescricaoCategoria(),
				categoria.getCategoriaUrlImg(),empresaId,
				categoria.getVisivel());
	}

	@Transactional
	public CategoriaProdutoDTO editarCategoria(Long categoriaId, CategoriaProdutoDTO categoriaDTO, Long empresaId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		// Verifica se o usuário pertence à empresa informada
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}
		// Busca a categoria no banco de dados
		CategoriaProduto categoria = categoriaProdutoRepository.findById(categoriaId)
				.orElseThrow(() -> new CustomException("Categoria não encontrada!", HttpStatus.NOT_FOUND.value()));
		// Verifica se a categoria pertence à empresa do usuário autenticado
		if (!categoria.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Usuário não pode editar categorias de outra empresa.", HttpStatus.UNAUTHORIZED.value());
		}
		// Atualiza apenas os campos fornecidos no DTO (não sobrescreve com `null`)
		if (categoriaDTO.nomeCategoria() != null && !categoriaDTO.nomeCategoria().isEmpty()) {
			categoria.setNomeCategoria(categoriaDTO.nomeCategoria());
		}
		if (categoriaDTO.descricaoCategoria() != null && !categoriaDTO.descricaoCategoria().isEmpty()) {
			categoria.setDescricaoCategoria(categoriaDTO.descricaoCategoria());
		}
		if (categoriaDTO.categoriaUrlImg() != null && !categoriaDTO.categoriaUrlImg().isEmpty()) {
			categoria.setCategoriaUrlImg(categoriaDTO.categoriaUrlImg());
		}
		categoria.setVisivel(categoriaDTO.visivel());
		// Salva as alterações no banco de dados
		CategoriaProduto categoriaAtualizada = categoriaProdutoRepository.save(categoria);
		// Retorna o DTO atualizado
		return new CategoriaProdutoDTO(
				categoriaAtualizada.getCategoriaId(),
				categoriaAtualizada.getNomeCategoria(),
				categoriaAtualizada.getCategoriaUrlImg(),
				categoriaAtualizada.getDescricaoCategoria(),
				categoriaAtualizada.getEmpresa().getId(),
				categoriaAtualizada.getVisivel()
		);
	}


	@Transactional
	public void deletarCategoria(Long id, Long empresaId) {
		CategoriaProduto categoria = categoriaProdutoRepository.findByCategoriaIdAndEmpresaId(id, empresaId)
				.orElseThrow(() -> new CustomException("Categoria não encontrada para esta empresa", HttpStatus.NOT_FOUND.value()));

		categoriaProdutoRepository.delete(categoria);
	}

	private CategoriaProdutoDTO toDTO(CategoriaProduto categoria) {
		return new CategoriaProdutoDTO(
				categoria.getCategoriaId(),
				categoria.getNomeCategoria(),
				categoria.getDescricaoCategoria(),
				categoria.getCategoriaUrlImg(),
				categoria.getEmpresa().getId(),
				categoria.getVisivel()
		);
	}

	@Transactional
	public String uploadImagemCategoria(Long categoriaId, MultipartFile file, Long empresaId) {
		try {
			// Validação da categoria
			CategoriaProduto categoria = categoriaProdutoRepository.findById(categoriaId)
					.orElseThrow(() -> new CustomException("Categoria não encontrada", HttpStatus.NOT_FOUND.value()));

			if (!categoria.getEmpresa().getId().equals(empresaId)) {
				throw new CustomException("Acesso não autorizado à categoria", HttpStatus.FORBIDDEN.value());
			}

			// Geração do nome do arquivo
			String fileName = "categorias_" + categoriaId + "_" + System.currentTimeMillis() + "_" +
					StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

			// Obtém a imagem antiga para exclusão
			String oldFileName = null;
			if (categoria.getCategoriaUrlImg() != null) {
				oldFileName = categoria.getCategoriaUrlImg()
						.substring(categoria.getCategoriaUrlImg().lastIndexOf('/') + 1);
			}

			// Upload da nova imagem
			String imagemUrl = cloudflareR2Service.uploadFile(file, fileName, oldFileName, bucketProdutos);

			// Atualiza a categoria
			categoria.setCategoriaUrlImg(imagemUrl);
			categoriaProdutoRepository.save(categoria);

			return imagemUrl;

		} catch (IOException e) {
			throw new CustomException("Erro ao processar a imagem: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

//	@Transactional
//	public void atualizarImagemCategoria(Long categoriaId, String imagemUrl) {
//		CategoriaProduto categoria = categoriaProdutoRepository.findById(categoriaId)
//				.orElseThrow(() -> new CustomException("Categoria não encontrada.", HttpStatus.NOT_FOUND.value()));
//
//		// Atualiza a URL da imagem na categoria
//		categoria.setCategoriaUrlImg(imagemUrl);
//		categoriaProdutoRepository.save(categoria);
//	}


}