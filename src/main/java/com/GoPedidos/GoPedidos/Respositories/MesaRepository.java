package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
	List<Mesa> findByEmpresaId(Long empresaId);
	// Verificar se já existe uma mesa com o mesmo nome e empresa
	boolean existsByDescricaoAndEmpresaId(String descricao, Long empresaId);
	boolean existsByDescricaoAndEmpresaIdAndMesaIdNot(String descricao, Long empresaId, Long mesaId);


}
