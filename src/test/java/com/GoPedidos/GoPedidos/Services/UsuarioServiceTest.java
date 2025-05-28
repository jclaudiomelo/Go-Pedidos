package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.EmpresaDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioResponseDTO;
import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.TokenConfirmacao;
import com.GoPedidos.GoPedidos.Models.Usuario;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.TokenConfirmacaoRepository;
import com.GoPedidos.GoPedidos.Respositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import static javax.management.Query.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;
	@Mock
	private EmpresaRepository empresaRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenConfirmacaoRepository tokenConfirmacaoRepository;
	@Mock
	private EmailService emailService;
	@Mock
	private JavaMailSender mailSender;
	@Mock
	private TokenService tokenService;

	@InjectMocks
	private UsuarioService usuarioService;

	@Test
	void criarUsuarioMaster_DeveCriarUsuarioComSucesso() {
		// Arrange
		EmpresaDTO empresaDTO = new EmpresaDTO(
				"12.345.678/0001-90",
				"Empresa X",
				"Rua Exemplo",
				123,
				"Apto 101",
				"12345-678",
				"Centro",
				"São Paulo",
				"SP",
				"contato@empresa.com",
				"(11) 99999-9999"
		);

		UsuarioDTO dto = new UsuarioDTO(
				"João Silva",
				"joao.silva@empresa.com",
				"123.456.789-00",
				"senha123",
				Role.MASTER,
				empresaDTO
		);

		when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);

		Empresa empresaSalva = new Empresa();
		empresaSalva.setId(1L); // você pode simular o ID da empresa

		when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaSalva);

		Usuario usuarioSalvo = new Usuario();
		usuarioSalvo.setId(1L);
		usuarioSalvo.setNome(dto.nome());
		usuarioSalvo.setEmail(dto.email());
		usuarioSalvo.setCpf(dto.cpf());
		usuarioSalvo.setSenha("senhaCriptografada");
		usuarioSalvo.setRole(Role.MASTER);
		usuarioSalvo.setEmpresa(empresaSalva);
		usuarioSalvo.setAtivo(false);

		when(passwordEncoder.encode(dto.senha())).thenReturn("senhaCriptografada");
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);
		when(tokenConfirmacaoRepository.save(any(TokenConfirmacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		UsuarioResponseDTO response = usuarioService.criarUsuarioMaster(dto);

		// Assert
		assertNotNull(response);
		assertEquals(usuarioSalvo.getId(), response.id());
		assertEquals(usuarioSalvo.getNome(), response.nome());
		assertEquals(usuarioSalvo.getEmail(), response.email());
		assertEquals(usuarioSalvo.getRole().name(), response.role());

		verify(emailService).enviarEmailDeConfirmacao(eq(dto.email()), anyString());
	}
}
