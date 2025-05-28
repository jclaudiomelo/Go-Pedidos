package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.MetodoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {
	List<MetodoPagamento> findByEmpresaId(Long empresaId);
}
