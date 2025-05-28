package com.GoPedidos.GoPedidos.DTOS;

import com.GoPedidos.GoPedidos.Enuns.StatusCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClienteDTO(
		@NotNull @Size(min = 3, max = 100)
		String nome,
		@Email @Size(max = 100)
		String email,
		@Size(min = 11, max = 11)
		String cpf,
		@NotNull
		String telefone,
		String endereco,
		LocalDate dataCadastro,
		StatusCliente status,
		Long empresaId
) {
}
