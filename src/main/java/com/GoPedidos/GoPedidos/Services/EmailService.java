package com.GoPedidos.GoPedidos.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	private void enviarEmailRecuperacao(String email, String link) {
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setTo(email);
		mensagem.setSubject("Recuperação de Senha - GoPedidos");
		mensagem.setText("Clique no link abaixo para redefinir sua senha:\n" + link);
		mensagem.setFrom("noreply@gopedidos.com");
		mailSender.send(mensagem);
	}

	public void enviarEmailDeConfirmacao(String email, String token) {
		String mensagem = "Clique no link abaixo para confirmar seu email:\n";
//		mensagem += "http://localhost:8080/usuario/confirmar?token=" + token;
		mensagem += "https://jcmtech.store/usuario/confirmar?token=" + token;
//		mensagem += "https://backend.jcmtech.store/usuario/confirmar?token=" + token;


		SimpleMailMessage emailMessage = new SimpleMailMessage();
		emailMessage.setTo(email);
		emailMessage.setSubject("Confirmação de Cadastro");
		emailMessage.setText(mensagem);
		emailMessage.setFrom("gopedidos@jcmtech.store");

		mailSender.send(emailMessage);
	}

}