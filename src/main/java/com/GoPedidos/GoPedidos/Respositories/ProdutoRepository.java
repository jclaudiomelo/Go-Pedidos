package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
	boolean existsByNome(String nome);

	List<Produto> findByEmpresaId(Long empresaId);
	List<Produto> findByCategoriaProduto_CategoriaIdAndEmpresa_Id(Long categoriaId, Long empresaId);
}

