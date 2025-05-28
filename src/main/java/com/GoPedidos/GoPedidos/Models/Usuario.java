package com.GoPedidos.GoPedidos.Models;

import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Respositories.RetornoTokenRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails, RetornoTokenRepository {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

//	@Column(unique = true)
//	private String cpf;

	@Column(unique = true)
//	@Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 números")
	private String cpf;

	@JsonIgnore
	@Column(nullable = false)
	private String senha;

	@Column(name = "usuario_url")
	private String usuarioUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@ManyToOne
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@Column(nullable = false)
	private Boolean ativo = false;

	@Column(nullable = false)
	private Boolean googleLogin = false;

	public boolean isCpfValido() {
		return cpf != null && cpf.matches("\\d{11}");
	}


	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> "ROLE_" + this.role.name());
	}

	public String getUsername() {
		return email;
	}

	public String getPassword() {
		return senha;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

}
