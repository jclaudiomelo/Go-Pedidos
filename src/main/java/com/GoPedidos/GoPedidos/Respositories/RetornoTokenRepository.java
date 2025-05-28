package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Enuns.Role;
import com.GoPedidos.GoPedidos.Models.Empresa;


public interface RetornoTokenRepository {
	Long getId();

	String getEmail();

	Role getRole();

	String getNome();

	Empresa getEmpresa();

}
