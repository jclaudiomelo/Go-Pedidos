package com.GoPedidos.GoPedidos.Services;

import com.GoPedidos.GoPedidos.Exceptions.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class RecaptchaService {

	private static final String SECRET_KEY = "6LfD_u0qAAAAABxQnEvr6S973Te0EHxhp1IH-fkH";
	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";


	public boolean verifyToken(String token) {
		try {
			// Codifica o token para uso na URL
			String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

			// Monta o corpo da requisição
			String requestBody = "secret=" + SECRET_KEY + "&response=" + encodedToken;

			// Cria a requisição HTTP
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(VERIFY_URL))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			// Envia a requisição e obtém a resposta
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// Processa a resposta
			if (response.statusCode() == 200) {
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

				// Verifica se o reCAPTCHA foi validado com sucesso
				return Boolean.TRUE.equals(responseMap.get("success"));
			} else {
				throw new CustomException("Erro ao validar CAPTCHA.", HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		} catch (IOException | InterruptedException e) {
			throw new CustomException("Erro ao validar CAPTCHA.", HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
//	public boolean verifyToken(String token) throws IOException, InterruptedException {
//		// Codifica o token para uso na URL
//		String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
//
//		// Monta o corpo da requisição
//		String requestBody = "secret=" + SECRET_KEY + "&response=" + encodedToken;
//
//		// Cria a requisição HTTP
//		HttpRequest request = HttpRequest.newBuilder()
//				.uri(URI.create(VERIFY_URL))
//				.header("Content-Type", "application/x-www-form-urlencoded")
//				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
//				.build();
//
//		// Envia a requisição e obtém a resposta
//		HttpClient client = HttpClient.newHttpClient();
//		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//		// Processa a resposta
//		if (response.statusCode() == 200) {
//			ObjectMapper objectMapper = new ObjectMapper();
//			Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
//
//			// Verifica se o reCAPTCHA foi validado com sucesso
//			return Boolean.TRUE.equals(responseMap.get("success"));
//		} else {
//			throw new IOException("Erro ao validar reCAPTCHA: " + response.statusCode());
//		}
//	}
//	public boolean verifyToken(String token) throws IOException, InterruptedException {
//		// Simula a validação do reCAPTCHA como verdadeira (sempre retorna true)
//		return true;
//	}
}