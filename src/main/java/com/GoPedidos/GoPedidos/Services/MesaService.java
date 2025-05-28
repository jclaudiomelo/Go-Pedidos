package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.MesaDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Enuns.MesaEnum;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.Mesa;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.MesaRepository;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MesaService {

	@Autowired
	private MesaRepository mesaRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	// 🔹 Listar mesas de uma empresa
	@Transactional
	public List<MesaDTO> listarMesas(String authorizationHeader) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		List<Mesa> mesas = mesaRepository.findByEmpresaId(usuarioAutenticado.empresaId());
		return mesas.stream()
				.map(mesa -> new MesaDTO(
						mesa.getMesaId(),
						mesa.getDescricao(),
						mesa.getStatus(),
						mesa.getEmpresa().getId(),
						mesa.getNumero()
				))
				.collect(Collectors.toList());
	}


	// 🔹 Buscar mesa por ID
	@Transactional
	public MesaDTO criarMesa(String authorizationHeader, MesaDTO mesaDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		if (!usuarioAutenticado.role().equals("MASTER") && !usuarioAutenticado.role().equals("ADMIN")) {
			throw new CustomException("Usuário não tem permissão para atualizar produtos de outra empresa.", HttpStatus.UNAUTHORIZED.value());
		}
		// Verificar se já existe uma mesa com o mesmo nome para a empresa
		if (mesaRepository.existsByDescricaoAndEmpresaId(mesaDTO.descricao(), usuarioAutenticado.empresaId())) {
			throw new CustomException("Já existe uma mesa com esse nome.", HttpStatus.BAD_REQUEST.value());
		}
		// Criação da nova mesa
		Mesa novaMesa = new Mesa();
		novaMesa.setDescricao(mesaDTO.descricao());
		novaMesa.setNumero(mesaDTO.numero());
		novaMesa.setStatus(mesaDTO.status());

		Empresa empresa = empresaRepository.findById(usuarioAutenticado.empresaId())
				.orElseThrow(() -> new CustomException("Empresa não encontrada", HttpStatus.NOT_FOUND.value()));

		novaMesa.setEmpresa(empresa);
		Mesa mesaSalva = mesaRepository.save(novaMesa);

		return new MesaDTO(
				mesaSalva.getMesaId(),
				mesaSalva.getDescricao(),
				mesaSalva.getStatus(),
				mesaSalva.getEmpresa().getId(),
				mesaSalva.getNumero()
		);
	}


	@Transactional
	public MesaDTO atualizarMesa(String authorizationHeader, Long id, MesaDTO mesaDTO) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		// Verifica se a mesa existe
		Mesa mesaExistente = mesaRepository.findById(id)
				.orElseThrow(() -> new CustomException("Mesa não encontrada", HttpStatus.NOT_FOUND.value()));
		// Verificar se já existe uma mesa com o mesmo nome para a empresa, mas não a mesma mesa
		if (mesaRepository.existsByDescricaoAndEmpresaIdAndMesaIdNot(mesaDTO.descricao(), usuarioAutenticado.empresaId(), id)) {
			throw new CustomException("Já existe uma mesa com esse nome na sua empresa.", HttpStatus.BAD_REQUEST.value());
		}
		// Verifica se o usuário tem permissão para editar
		if (!usuarioAutenticado.role().equals("MASTER") && !usuarioAutenticado.role().equals("ADMIN")) {
			throw new CustomException("Usuário não tem permissão para atualizar produtos de outra empresa.", HttpStatus.UNAUTHORIZED.value());
		}
		// Atualiza a mesa
		if (mesaDTO.descricao() != null) mesaExistente.setDescricao(mesaDTO.descricao());
		if (mesaDTO.status() != null) mesaExistente.setStatus(mesaDTO.status());
		if (mesaDTO.numero() != null) mesaExistente.setNumero(mesaDTO.numero());

		Mesa mesaAtualizada = mesaRepository.save(mesaExistente);
		return new MesaDTO(
				mesaAtualizada.getMesaId(),
				mesaAtualizada.getDescricao(),
				mesaAtualizada.getStatus(),
				mesaAtualizada.getEmpresa().getId(),
				mesaAtualizada.getNumero()
		);
	}

	// 🔹 Deletar uma mesa
	@Transactional
	public void deletarMesa(String authorizationHeader, Long id) {
		UsuarioAutenticado usuarioAutenticado = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);
		// Verifica se a mesa existe
		Mesa mesa = mesaRepository.findById(id)
				.orElseThrow(() -> new CustomException("Mesa não encontrada", HttpStatus.NOT_FOUND.value()));
		// Verifica se o usuário tem permissão para deletar a mesa
		if (!mesa.getEmpresa().getId().equals(usuarioAutenticado.empresaId())) {
			throw new CustomException("Acesso negado. Você só pode excluir mesas da sua própria empresa.", HttpStatus.FORBIDDEN.value());
		}
		mesaRepository.deleteById(id);
	}
}

