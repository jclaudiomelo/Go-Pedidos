package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {


	Optional<Cliente> findByEmail(String email);

	List<Cliente> findByEmpresaId(Long empresaId);

}
