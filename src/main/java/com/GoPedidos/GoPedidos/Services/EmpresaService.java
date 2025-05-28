package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.DTOS.EmpresaDTO;
import com.GoPedidos.GoPedidos.DTOS.EmpresaResponseDTO;
import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Respositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

	@Autowired
	private EmpresaRepository empresaRepository;

	//	@Transactional
//	public EmpresaResponseDTO criarEmpresa(EmpresaDTO empresaDTO) {
//		if (empresaRepository.existsByCnpj(empresaDTO.cnpj())) {
//			throw new CustomException("Já existe uma empresa cadastrada com esse CNPJ.", HttpStatus.CONFLICT.value());
//		}
//
//		Empresa empresa = new Empresa();
//		empresa.setNomeEmpresa(empresaDTO.nomeEmpresa());
//		empresa.setCnpj(empresaDTO.cnpj());
//		empresa.setLogradouro(empresaDTO.logradouro());
//		empresa.setNumero(empresaDTO.numero());
//		empresa.setComplemento(empresaDTO.complemento());
//		empresa.setCep(empresaDTO.cep());
//		empresa.setBairro(empresaDTO.bairro());
//		empresa.setMunicipio(empresaDTO.municipio());
//		empresa.setUf(empresaDTO.uf());
//		empresa.setEmail(empresaDTO.email());
//		empresa.setTelefone(empresaDTO.telefone());
//
//		Empresa empresaSalva = empresaRepository.save(empresa);
//
//		return new EmpresaResponseDTO(empresaSalva.getId(), empresaSalva.getNomeEmpresa(), empresaSalva.getCnpj());
//	}
	@Transactional
	public EmpresaResponseDTO criarEmpresa(EmpresaDTO empresaDTO, String idEmpresa) {
		// Buscar a empresa pelo ID
		Empresa empresa = empresaRepository.findById(Long.valueOf(idEmpresa))
				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));

		// Preencher os dados da empresa
		empresa.setNomeEmpresa(empresaDTO.nomeEmpresa());
		empresa.setCnpj(empresaDTO.cnpj());
		empresa.setLogradouro(empresaDTO.logradouro());
		empresa.setNumero(empresaDTO.numero());
		empresa.setComplemento(empresaDTO.complemento());
		empresa.setCep(empresaDTO.cep());
		empresa.setBairro(empresaDTO.bairro());
		empresa.setMunicipio(empresaDTO.municipio());
		empresa.setUf(empresaDTO.uf());
		empresa.setEmail(empresaDTO.email());
		empresa.setTelefone(empresaDTO.telefone());

		// Salvar a empresa atualizada
		Empresa empresaSalva = empresaRepository.save(empresa);

		return new EmpresaResponseDTO(empresa.getId(),
				empresa.getNomeEmpresa(),
				empresa.getCnpj(),
				empresa.getLogradouro(),
				empresa.getNumero(),
				empresa.getComplemento(),
				empresa.getCep(),
				empresa.getBairro(),
				empresa.getMunicipio(),
				empresa.getUf(),
				empresa.getEmail(),
				empresa.getTelefone());
	}

	@Transactional
	public EmpresaResponseDTO atualizarEmpresa(Long id, EmpresaDTO empresaDTO) {
		// Buscar empresa no banco
		Empresa empresa = empresaRepository.findById(id)
				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));

		try {
			// Atualizar os campos fornecidos, exceto CNPJ
			if (empresaDTO.nomeEmpresa() != null) {
				empresa.setNomeEmpresa(empresaDTO.nomeEmpresa());
			}
			if (empresaDTO.cnpj() != null) {
				empresa.setCnpj(empresaDTO.cnpj());
			}
			if (empresaDTO.logradouro() != null) {
				empresa.setLogradouro(empresaDTO.logradouro());
			}
			if (empresaDTO.complemento() != null) {
				empresa.setComplemento(empresaDTO.complemento());
			}
			if (empresaDTO.cep() != null) {
				empresa.setCep(empresaDTO.cep());
			}
			if (empresaDTO.bairro() != null) {
				empresa.setBairro(empresaDTO.bairro());
			}
			if (empresaDTO.municipio() != null) {
				empresa.setMunicipio(empresaDTO.municipio());
			}
			if (empresaDTO.uf() != null) {
				empresa.setUf(empresaDTO.uf());
			}
			if (empresaDTO.email() != null) {
				empresa.setEmail(empresaDTO.email());
			}
			if (empresaDTO.telefone() != null) {
				empresa.setTelefone(empresaDTO.telefone());
			}
			empresa = empresaRepository.save(empresa);
			return new EmpresaResponseDTO(
					empresa.getId(),
					empresa.getNomeEmpresa(),
					empresa.getCnpj(),
					empresa.getLogradouro(),
					empresa.getNumero(),
					empresa.getComplemento(),
					empresa.getCep(),
					empresa.getBairro(),
					empresa.getMunicipio(),
					empresa.getUf(),
					empresa.getEmail(),
					empresa.getTelefone()
			);

		} catch (Exception e) {
			throw new CustomException("Não foi possivel Atualizar", HttpStatus.UNAUTHORIZED.value());
		}
	}

	public EmpresaResponseDTO buscarEmpresaPorId(Long empresaId) {
		Empresa empresa = empresaRepository.findById(empresaId)
				.orElseThrow(() -> new CustomException("Empresa não encontrada.", HttpStatus.NOT_FOUND.value()));

		try {
			return new EmpresaResponseDTO(empresa.getId(),
					empresa.getNomeEmpresa(),
					empresa.getCnpj(),
					empresa.getLogradouro(),
					empresa.getNumero(),
					empresa.getComplemento(),
					empresa.getCep(),
					empresa.getBairro(),
					empresa.getMunicipio(),
					empresa.getUf(),
					empresa.getEmail(),
					empresa.getTelefone());

		} catch (Exception e) {
			throw new CustomException("Token inválido ou expirado", HttpStatus.UNAUTHORIZED.value());
		}
	}

}
