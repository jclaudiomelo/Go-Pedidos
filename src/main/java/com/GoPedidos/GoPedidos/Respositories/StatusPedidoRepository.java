package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusPedidoRepository extends JpaRepository<StatusPedido, Long> {
	Optional<StatusPedido> findByDescricao(String descricao);


	List<StatusPedido> findByEmpresaId(Long empresaId);
}
