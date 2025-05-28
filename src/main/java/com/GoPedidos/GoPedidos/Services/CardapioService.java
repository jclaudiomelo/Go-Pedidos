package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.*;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Cardapio;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Respositories.CardapioRepository;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardapioService {

	@Autowired
	private CardapioRepository cardapioRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private TokenService tokenService;

	// 🔹 Criar cardápio
	@Transactional
	public CardapioDTO criarCardapio(CardapioDTO cardapioDTO, Long empresaId) {
		// Obtém o usuário autenticado a partir do SecurityContextHolder
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		// Verifica se o usuário tem a role de ADMIN ou MASTER (assumindo que a role está no JWT)
		if (!(usuarioAutenticado.role().equals("MASTER") || usuarioAutenticado.role().equals("ADMIN"))) {
			throw new CustomException("Usuário não tem permissão para criar produtos. Necessária a role ADMIN ou MASTER.", HttpStatus.UNAUTHORIZED.value());
		}
		// 🔍 Buscar no banco diretamente antes de salvar
		boolean existe = cardapioRepository.existsByNomeAndEmpresaId(cardapioDTO.nome(), empresaId);
		if (existe) {
			throw new CustomException("Já existe um cardápio com esse nome ", HttpStatus.CONFLICT.value());
		}

		// Criar e salvar o cardápio normalmente
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new CustomException("Empresa não encontrada!", HttpStatus.NOT_FOUND.value()));


		Cardapio novoCardapio = Cardapio.builder()
				.nome(cardapioDTO.nome())
				.empresa(empresa)
				.visivel(cardapioDTO.visivel())
				.dataCriacao(LocalDateTime.now())
				.dataAtualizacao(LocalDateTime.now())
				.build();


		Cardapio cardapioSalvo = cardapioRepository.save(novoCardapio);

		return new CardapioDTO(
				cardapioSalvo.getCardapio_id(),
				cardapioSalvo.getNome(),
				cardapioSalvo.getDataAtualizacao(),
				cardapioSalvo.isVisivel(),
				null,
				null
		);
	}


	// 🔹 Editar cardápio
	@Transactional
	public CardapioDTO editarCardapio(Long id, CardapioDTO cardapioDTO, String token) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(token);
		Long usuarioAutenticadoId = decodedJWT.getClaim("id").asLong();
		Long empresaIdAutenticado = decodedJWT.getClaim("empresa_id").asLong();

		// Verifica se o cardápio existe
		Cardapio cardapio = cardapioRepository.findById(id)
				.orElseThrow(() -> new CustomException("Cardápio não encontrado!", HttpStatus.NOT_FOUND.value()));

		// Verifica se o cardápio pertence à empresa do usuário autenticado
		if (!cardapio.getEmpresa().getId().equals(empresaIdAutenticado)) {
			throw new RuntimeException("Você não pode editar cardápios de outra empresa.");
		}

		// Atualiza os campos do cardápio
		if (cardapioDTO.nome() != null) cardapio.setNome(cardapioDTO.nome());
		if (cardapioDTO.visivel() != cardapio.isVisivel()) cardapio.setVisivel(cardapioDTO.visivel());
		cardapio.setDataAtualizacao(LocalDateTime.now());  // Atualiza a data de atualização

		// Salva as alterações
		Cardapio cardapioAtualizado = cardapioRepository.save(cardapio);

		// Retorna o DTO atualizado
		return new CardapioDTO(
				cardapioAtualizado.getCardapio_id(),
				cardapioAtualizado.getNome(),
				cardapioAtualizado.getDataAtualizacao(),
				cardapioAtualizado.isVisivel(),
				cardapioAtualizado.getEmpresa().getId(),
				null
		);
	}

	@Transactional(readOnly = true)
	public CardapioProdutosDTO buscarCardapioPorId(Long cardapioId, Long empresaId) {
		// Busca o cardápio pelo ID
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		// Verifica se o usuário pertence à empresa informada
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}

		Cardapio cardapio = cardapioRepository.findById(cardapioId)
				.orElseThrow(() -> new CustomException("cardapio não encontrado!", HttpStatus.NOT_FOUND.value()));

		// Verifica se o produto pertence à empresa do usuário
		if (!cardapio.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Produto não pertence à empresa do usuário!", HttpStatus.UNAUTHORIZED.value());
		}

		// Converte a entidade Cardapio para CardapioProdutosDTO
		return new CardapioProdutosDTO(
				cardapio.getCardapio_id(),
				cardapio.getNome(),
				cardapio.getDataAtualizacao(),
				cardapio.isVisivel(),
				cardapio.getEmpresa().getId(),
				cardapio.getProdutos().stream()
						.map(produto -> new ProdutoCardapioDTO(
								produto.getProdutoId(),
								produto.getNome(),
								produto.getPreco(),
								produto.getDescricao(),
								produto.getVisivel()))
						.collect(Collectors.toList())
		);
	}

	// 🔹 Deletar cardápio
	@Transactional
	public void deletarCardapio(Long id, Long empresaIdUsuario) {
		Cardapio cardapio = cardapioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cardápio não encontrado!"));

		// Verifica se o cardápio pertence à empresa do usuário autenticado
		if (!cardapio.getEmpresa().getId().equals(empresaIdUsuario)) {
			throw new CustomException("Você não pode excluir cardápios de outra empresa.", HttpStatus.BAD_REQUEST.value());
		}

		// Deleta o cardápio
		cardapioRepository.deleteById(id);
	}

	@Transactional
	public List<CardapioDTO> listarCardapios(Long empresaId) {
		// Obtém o usuário autenticado
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authentication.getPrincipal();

		// Verifica se o usuário pertence à empresa informada
		if (!usuarioAutenticado.empresaId().equals(empresaId)) {
			throw new CustomException("Usuário não pertence à empresa informada!", HttpStatus.UNAUTHORIZED.value());
		}

		// Busca todos os cardápios da empresa
		List<Cardapio> cardapios = cardapioRepository.findByEmpresaId(empresaId);

		// Converte a lista de Cardapio para CardapioDTO
		return cardapios.stream()
				.map(cardapio -> new CardapioDTO(
						cardapio.getCardapio_id(),
						cardapio.getNome(),
						cardapio.getDataAtualizacao(),
						cardapio.isVisivel(),
						cardapio.getEmpresa().getId(),
						cardapio.getProdutos().stream()
								.map(produto -> new ProdutoDTO(
										produto.getProdutoId(),
										produto.getNome(),
										produto.getDescricao(),
										produto.getPreco(),
										// Converte a categoriaProduto para CategoriaProdutoDTO
										new CategoriaProdutoDTO(
												produto.getCategoriaProduto().getCategoriaId(),
												produto.getCategoriaProduto().getNomeCategoria(),
												produto.getCategoriaProduto().getDescricaoCategoria(),
												produto.getCategoriaProduto().getCategoriaUrlImg(),
												produto.getCategoriaProduto().getEmpresa() != null ? produto.getCategoriaProduto().getEmpresa().getId() : null,
												produto.getVisivel()
										),
										produto.getVisivel(),
										produto.getCardapio() != null ? produto.getCardapio().getCardapio_id() : null,
										produto.getImagemUrl(),
										produto.getEmpresa().getId()
								))
								.collect(Collectors.toList())
				))
				.collect(Collectors.toList());
	}

}
