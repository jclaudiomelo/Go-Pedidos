package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.CriarColaboradorDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.DTOS.UsuarioDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioResponseDTO;
import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.Usuario;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.UsuarioRepository;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private CloudflareR2Service cloudflareR2Service;

	@Value("${cloudflare.r2.public-url}")
	private String publicUrl;

	@Value("${cloudflare.r2.bucket.produtos}")
	private String bucketProdutos;
	@Autowired
	private ValidarAcessoToken validarAcessoToken;


	@Transactional
	public UsuarioDTO criarColaborador(
			CriarColaboradorDTO criarColaboradorDTO,
			UsuarioAutenticado usuarioAutenticado,
			String authorizationHeader) {

		if (!usuarioAutenticado.role().equals("MASTER")) {
			throw new CustomException("Apenas usuários com role MASTER podem criar colaboradores.",
					HttpStatus.FORBIDDEN.value());
		}

		if (criarColaboradorDTO.role() == Role.MASTER) {
			throw new CustomException("Não é permitido criar um colaborador com o cargo MASTER.",
					HttpStatus.BAD_REQUEST.value());
		}

		if (usuarioRepository.existsByEmail(criarColaboradorDTO.email())) {
			throw new CustomException("Já existe um usuário com esse email.",
					HttpStatus.CONFLICT.value());
		}

		if (usuarioRepository.existsByCpf(criarColaboradorDTO.cpf())) {
			throw new CustomException("Já existe um CPF cadastrado.",
					HttpStatus.CONFLICT.value());
		}

		Empresa empresa = empresaRepository.findById(usuarioAutenticado.empresaId())
				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));

		Usuario novoUsuario = new Usuario();
		novoUsuario.setNome(criarColaboradorDTO.nome());
		novoUsuario.setEmail(criarColaboradorDTO.email());
		novoUsuario.setCpf(criarColaboradorDTO.cpf());
		novoUsuario.setSenha(passwordEncoder.encode(criarColaboradorDTO.senha()));
		novoUsuario.setRole(criarColaboradorDTO.role());
		novoUsuario.setEmpresa(empresa);

		Usuario salvo = usuarioRepository.save(novoUsuario);

		return new UsuarioDTO(
				salvo.getId(),
				salvo.getNome(),
				salvo.getEmail(),
				salvo.getCpf(),
				null,
				salvo.getRole(),
				salvo.getUsuarioUrl(),
				salvo.getAtivo(),
				null
		);
	}


	@Transactional
	public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO usuarioDTO, String token) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(token);

		Long usuarioAutenticadoId = decodedJWT.getClaim("id").asLong();
		Role usuarioAutenticadoRole = Role.valueOf(decodedJWT.getClaim("role").asString());
		Long empresaIdAutenticado = decodedJWT.getClaim("empresa_id").asLong();

		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));

		// 🔒 Verifica se pertence à mesma empresa
		if (!usuario.getEmpresa().getId().equals(empresaIdAutenticado)) {
			throw new CustomException("Usuário não pode atualizar usuários de outra empresa.", HttpStatus.FORBIDDEN.value());
		}

		// 🔒 Se não for MASTER e tentar atualizar outro usuário
		if (usuarioAutenticadoRole != Role.MASTER && !usuarioAutenticadoId.equals(id)) {
			throw new CustomException("Usuário não tem permissão para atualizar dados de outros usuários.", HttpStatus.FORBIDDEN.value());
		}

		// ✅ Validação de email duplicado
		if (usuarioDTO.email() != null && !usuarioDTO.email().equals(usuario.getEmail())) {
			if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
				throw new CustomException("Já existe um usuário cadastrado com este email.", HttpStatus.CONFLICT.value());
			}
			usuario.setEmail(usuarioDTO.email());
		}

		// ✅ Validação de CPF duplicado
		if (usuarioDTO.cpf() != null && !usuarioDTO.cpf().equals(usuario.getCpf())) {
			if (usuarioRepository.existsByCpf(usuarioDTO.cpf())) {
				throw new CustomException("Já existe um usuário cadastrado com este CPF.", HttpStatus.CONFLICT.value());
			}
			usuario.setCpf(usuarioDTO.cpf());
		}

		// Atualização de outros campos permitidos
		if (usuarioDTO.nome() != null) usuario.setNome(usuarioDTO.nome());
		if (usuarioDTO.senha() != null) usuario.setSenha(passwordEncoder.encode(usuarioDTO.senha()));
		if (usuarioDTO.ativo() != null) usuario.setAtivo(usuarioDTO.ativo());

		// ✅ Regra de atualização do role
		if (usuarioDTO.role() != null) {
			if (usuarioAutenticadoRole == Role.MASTER) {
				if (usuarioAutenticadoId.equals(id)) {
					throw new CustomException("O usuário MASTER não pode alterar seu próprio role.", HttpStatus.FORBIDDEN.value());
				}
				if (usuarioDTO.role() == Role.MASTER) {
					throw new CustomException("Não é permitido atribuir o role MASTER a outro usuário. Apenas um MASTER por empresa.", HttpStatus.FORBIDDEN.value());
				}
				usuario.setRole(usuarioDTO.role());
			} else {
				throw new CustomException("Você não tem permissão para alterar o role de usuários.", HttpStatus.FORBIDDEN.value());
			}
		}

		usuario = usuarioRepository.save(usuario);

		return new UsuarioDTO(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getCpf(),
				null,
				usuario.getRole(),
				usuario.getUsuarioUrl(),
				usuario.getAtivo(),
				usuarioDTO.empresa()
		);
	}


	@Transactional
	public List<UsuarioResponseDTO> listarUsuarios(String authorizationHeader) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(authorizationHeader);
		Long empresaIdAutenticado = decodedJWT.getClaim("empresa_id").asLong();
		List<Usuario> usuarios = usuarioRepository.findByEmpresaId(empresaIdAutenticado);
		return usuarios.stream()
				.map(usuario -> new UsuarioResponseDTO(
						usuario.getId(),
						usuario.getNome(),
						usuario.getEmail(),
						usuario.getUsuarioUrl(),
						usuario.getCpf(),
						usuario.getRole().name(),
						usuario.getAtivo()
				))
				.collect(Collectors.toList());
	}

	@Transactional
	public UsuarioDTO usuarioId(Long id, String authorizationHeader) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(authorizationHeader);
		Long empresaIdAutenticado = decodedJWT.getClaim("empresa_id").asLong();
		Role usuarioAutenticadoRole = Role.valueOf(decodedJWT.getClaim("role").asString());
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));
		// Restrição de acesso
		if (!usuario.getEmpresa().getId().equals(empresaIdAutenticado)) {
			throw new CustomException("Usuário não pode acessar dados de outra empresa.", HttpStatus.FORBIDDEN.value());
		}
		if (usuarioAutenticadoRole != Role.MASTER && !decodedJWT.getClaim("id").asLong().equals(id)) {
			throw new CustomException("Você não tem permissão para acessar este usuário.", HttpStatus.FORBIDDEN.value());
		}
		return new UsuarioDTO(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getCpf(),
				null,
				usuario.getRole(),
				usuario.getUsuarioUrl(),
				usuario.getAtivo(),
				null
		);
	}

	@Transactional
	public String uploadFotoUsuario(Long id, MultipartFile file, String token) throws IOException {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(token);
		Long usuarioAutenticadoId = decodedJWT.getClaim("id").asLong();
		Role role = Role.valueOf(decodedJWT.getClaim("role").asString());
		Long empresaId = decodedJWT.getClaim("empresa_id").asLong();

		// Verifica se o usuário existe
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));

		// Verifica se pertence à mesma empresa
		if (!usuario.getEmpresa().getId().equals(empresaId)) {
			throw new CustomException("Usuário não pode atualizar de outra empresa.", HttpStatus.FORBIDDEN.value());
		}

		// Se não for MASTER e tentar atualizar outro
		if (role != Role.MASTER && !usuarioAutenticadoId.equals(id)) {
			throw new CustomException("Você não tem permissão para atualizar outro usuário.", HttpStatus.FORBIDDEN.value());
		}

		// Valida o arquivo
		if (file.isEmpty()) {
			throw new CustomException("Arquivo não enviado", HttpStatus.BAD_REQUEST.value());
		}
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CustomException("O arquivo enviado não é uma imagem válida.", HttpStatus.BAD_REQUEST.value());
		}
		if (file.getSize() > 5 * 1024 * 1024) {
			throw new CustomException("O arquivo excede o tamanho máximo de 5MB.", HttpStatus.BAD_REQUEST.value());
		}

		// Gera nome e faz upload
		String fileName = "usuarios_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
		String oldFileName = usuario.getUsuarioUrl() != null ? usuario.getUsuarioUrl().replace(publicUrl + "/", "") : null;
		String imagemUrl = cloudflareR2Service.uploadFile(file, fileName, oldFileName, bucketProdutos);

		// Atualiza no banco
		usuario.setUsuarioUrl(imagemUrl);
		usuarioRepository.save(usuario);

		return imagemUrl;
	}


	@Transactional
	public String deletarUsuario(Long id, String authorizationHeader) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(authorizationHeader);
		Long usuarioAutenticadoId = decodedJWT.getClaim("id").asLong();
		Role usuarioAutenticadoRole = Role.valueOf(decodedJWT.getClaim("role").asString());
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new CustomException("Usuário não encontrado", HttpStatus.NOT_FOUND.value()));
		if (usuario.getRole() == Role.MASTER) {
			throw new CustomException("Usuários MASTER não podem ser deletados.", HttpStatus.FORBIDDEN.value());
		}
		if (usuarioAutenticadoRole != Role.MASTER && !usuarioAutenticadoId.equals(id)) {
			throw new CustomException("Usuário não tem permissão para deletar outro usuário.", HttpStatus.FORBIDDEN.value());
		}
		usuarioRepository.delete(usuario);
		return "Usuário deletado com sucesso.";
	}


}