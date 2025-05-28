package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.ClienteDTO;
import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Models.Cliente;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Respositories.ClienteRepository;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenService tokenService;


	// 🔹 Criar cliente
	public ClienteDTO criarCliente(ClienteDTO clienteDTO, Long empresaId) {
		Cliente cliente = new Cliente();
		cliente.setNome(clienteDTO.nome());
		cliente.setEmail(clienteDTO.email());
		cliente.setCpf(clienteDTO.cpf());
		cliente.setTelefone(clienteDTO.telefone());
		cliente.setEndereco(clienteDTO.endereco());
		cliente.setDataCadastro(LocalDate.now());
		cliente.setStatus(clienteDTO.status());

		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new RuntimeException("Empresa não encontrada!"));
		cliente.setEmpresa(empresa);

		Cliente clienteSalvo = clienteRepository.save(cliente);

		return new ClienteDTO(
				clienteSalvo.getNome(),
				clienteSalvo.getEmail(),
				clienteSalvo.getCpf(),
				clienteSalvo.getTelefone(),
				clienteSalvo.getEndereco(),
				clienteSalvo.getDataCadastro(),
				clienteSalvo.getStatus(),
				null
		);
	}

	// 🔹 Editar cliente
	@Transactional
	public ClienteDTO editarCliente(Long id, ClienteDTO clienteDTO, String token) {
		DecodedJWT decodedJWT = tokenService.validarTokenUsuario(token);
		Long usuarioAutenticadoId = decodedJWT.getClaim("id").asLong();
		Role usuarioAutenticadoRole = Role.valueOf(decodedJWT.getClaim("role").asString());
		Long empresaIdAutenticado = decodedJWT.getClaim("empresa_id").asLong();

		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));

		// 🔍 Verifica se o cliente pertence à empresa do usuário autenticado
		if (!cliente.getEmpresa().getId().equals(empresaIdAutenticado)) {
			throw new RuntimeException("Usuário não pode atualizar clientes de outra empresa.");
		}

		// 🔍 Permite edição apenas para o próprio usuário ou MASTER
		if (usuarioAutenticadoRole != Role.MASTER && !usuarioAutenticadoId.equals(id)) {
			throw new RuntimeException("Usuário não tem permissão para atualizar dados de outros clientes.");
		}

		// 🔄 Atualiza apenas os campos não nulos
		if (clienteDTO.nome() != null) cliente.setNome(clienteDTO.nome());
		if (clienteDTO.email() != null) cliente.setEmail(clienteDTO.email());
		if (clienteDTO.telefone() != null) cliente.setTelefone(clienteDTO.telefone());
		if (clienteDTO.endereco() != null) cliente.setEndereco(clienteDTO.endereco());
		if (clienteDTO.status() != null) cliente.setStatus(clienteDTO.status());
		if (clienteDTO.cpf() != null) cliente.setCpf(clienteDTO.cpf());

		Cliente clienteAtualizado = clienteRepository.save(cliente);

		return new ClienteDTO(
				clienteAtualizado.getNome(),
				clienteAtualizado.getEmail(),
				clienteAtualizado.getCpf(),
				clienteAtualizado.getTelefone(),
				clienteAtualizado.getEndereco(),
				clienteAtualizado.getDataCadastro(),
				clienteAtualizado.getStatus(),
				null);
	}

	// 🔹 Deletar cliente
	public void deletarCliente(Long id, Long empresaIdUsuario) {
		Cliente cliente = clienteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));

		// 🔍 Verifica se o cliente pertence à empresa do usuário autenticado
		if (!cliente.getEmpresa().getId().equals(empresaIdUsuario)) {
			throw new RuntimeException("Acesso negado! Você só pode excluir clientes da sua própria empresa.");
		}

		clienteRepository.deleteById(id);
	}
}
