package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Cliente;
import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByEmpresa(Empresa empresa);

	Optional<Pedido> findById(Long pedidoId);

	List<Pedido> findByCliente(Cliente cliente);


	// Você pode adicionar outras consultas personalizadas conforme necessário
}
