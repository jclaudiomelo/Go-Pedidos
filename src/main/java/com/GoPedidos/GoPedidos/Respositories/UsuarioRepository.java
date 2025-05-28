package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Models.TokenConfirmacao;
import com.GoPedidos.GoPedidos.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByEmailIgnoreCase(String email); // Novo metodo

	Optional<Usuario> findByEmailAndEmpresaId(String email, Long empresaId);

	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findByEmailAndRole(String email, Role role);

	Optional<Usuario> findById(Long id);

	List<Usuario> findByEmpresaId(Long empresaId);

	boolean existsByEmail(String email);

	boolean existsByCpf(String cpf);
}