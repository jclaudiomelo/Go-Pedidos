package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Empresa;
import com.GoPedidos.GoPedidos.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

	Optional<Empresa> findById(Long id);


	boolean existsByCnpj(String cnpj);
}
