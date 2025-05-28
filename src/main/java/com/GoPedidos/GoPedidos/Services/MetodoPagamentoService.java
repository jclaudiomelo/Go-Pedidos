package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.MetodoPagamentoDTO;
import com.GoPedidos.GoPedidos.DTOS.UsuarioAutenticado;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.MetodoPagamento;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import com.GoPedidos.GoPedidos.Respositories.MetodoPagamentoRepository;
import com.GoPedidos.GoPedidos.Utils.ValidarAcessoToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetodoPagamentoService {

	@Autowired
	private MetodoPagamentoRepository metodoPagamentoRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private ValidarAcessoToken validarAcessoToken;

	@Transactional
	public MetodoPagamentoDTO criar(String authorizationHeader, MetodoPagamentoDTO dto) {
		UsuarioAutenticado usuario = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);

		Empresa empresa = empresaRepository.findById(usuario.empresaId())
				.orElseThrow(() -> new CustomException("Empresa não encontrada", HttpStatus.NOT_FOUND.value()));

		MetodoPagamento metodo = new MetodoPagamento();
		metodo.setDescricao(dto.descricao());
		metodo.setVisivel(dto.visivel());
		metodo.setEmpresa(empresa);

		MetodoPagamento salvo = metodoPagamentoRepository.save(metodo);

		return new MetodoPagamentoDTO(
				salvo.getId(),
				salvo.getDescricao(),
				salvo.isVisivel(),
				empresa.getId()
		);
	}

	@Transactional
	public List<MetodoPagamentoDTO> listar(String authorizationHeader) {
		UsuarioAutenticado usuario = validarAcessoToken.validarUsuario(authorizationHeader);

		List<MetodoPagamento> metodos = metodoPagamentoRepository.findByEmpresaId(usuario.empresaId());

		return metodos.stream()
				.map(m -> new MetodoPagamentoDTO(
						m.getId(),
						m.getDescricao(),
						m.isVisivel(),
						m.getEmpresa().getId()))
				.collect(Collectors.toList());
	}

	@Transactional
	public MetodoPagamentoDTO atualizar(String authorizationHeader, Long id, MetodoPagamentoDTO dto) {
		UsuarioAutenticado usuario = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);

		MetodoPagamento metodo = metodoPagamentoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Método de pagamento não encontrado", HttpStatus.NOT_FOUND.value()));

		if (!metodo.getEmpresa().getId().equals(usuario.empresaId())) {
			throw new CustomException("Você não tem permissão para editar este método.", HttpStatus.FORBIDDEN.value());
		}

		metodo.setDescricao(dto.descricao());
		metodo.setVisivel(dto.visivel());

		MetodoPagamento atualizado = metodoPagamentoRepository.save(metodo);

		return new MetodoPagamentoDTO(
				atualizado.getId(),
				atualizado.getDescricao(),
				atualizado.isVisivel(),
				atualizado.getEmpresa().getId()
		);
	}

	@Transactional
	public String deletar(String authorizationHeader, Long id) {
		UsuarioAutenticado usuario = validarAcessoToken.validarMasterOuAdmin(authorizationHeader);

		MetodoPagamento metodo = metodoPagamentoRepository.findById(id)
				.orElseThrow(() -> new CustomException("Método de pagamento não encontrado", HttpStatus.NOT_FOUND.value()));

		if (!metodo.getEmpresa().getId().equals(usuario.empresaId())) {
			throw new CustomException("Você não tem permissão para deletar este método.", HttpStatus.FORBIDDEN.value());
		}

		metodoPagamentoRepository.delete(metodo);
		return "Método de pagamento deletado com sucesso.";
	}
}
