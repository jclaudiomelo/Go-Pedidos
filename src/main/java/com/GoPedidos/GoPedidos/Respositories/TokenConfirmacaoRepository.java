package com.GoPedidos.GoPedidos.Respositories;


import com.GoPedidos.GoPedidos.Models.TokenConfirmacao;
import com.GoPedidos.GoPedidos.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenConfirmacaoRepository extends JpaRepository<TokenConfirmacao, Long> {

	Optional<TokenConfirmacao> findByToken(String token);

	void deleteByUsuarioAndTipo(Usuario usuario, TokenConfirmacao.TipoToken tipoToken);

	void deleteByToken(String token);

	Optional<TokenConfirmacao> findByUsuarioAndTipo(Usuario usuario, TokenConfirmacao.TipoToken tipoToken);


//	Optional<TokenConfirmacao> findByTokenAndTipo(String token, TokenConfirmacao.TipoToken tipoToken);
//
//	void deleteByUsuarioAndTipo(Usuario usuario, TokenConfirmacao.TipoToken tipoToken);
}
