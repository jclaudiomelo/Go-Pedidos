package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.CategoriaProduto;
import com.GoPedidos.GoPedidos.Models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaProdutoRepository extends JpaRepository<CategoriaProduto, Long> {
	List<CategoriaProduto> findByEmpresaId(Long empresaId);

	boolean existsByNomeCategoriaAndEmpresa_Id(String nomeCategoria, Long empresaId);

	Optional<CategoriaProduto> findByCategoriaIdAndEmpresaId(Long categoriaId, Long empresaId);
}
