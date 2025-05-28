package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Cardapio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardapioRepository extends JpaRepository<Cardapio, Long> {

	boolean existsByNomeAndEmpresaId(String nome, Long empresaId);

	List<Cardapio> findByEmpresaId(Long empresaId);

}