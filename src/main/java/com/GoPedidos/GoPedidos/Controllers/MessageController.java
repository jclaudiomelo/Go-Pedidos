package com.GoPedidos.GoPedidos.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/msg")
public class MessageController {

	private final List<Message> messages = new ArrayList<>(); // Aqui você pode manter as mensagens em memória ou persistir em algum lugar

	@GetMapping("/mensagens")
	public ResponseEntity<?> getMessages() {
		return ResponseEntity.ok(Map.of("messages", messages));
	}

	// Você pode ter um método para registrar as mensagens no array
	public void addMessage(String content, String type) {
		Message message = new Message(content, type, LocalDateTime.now());
		messages.add(message);
	}

	public static class Message {
		private String content;
		private String type;
		private LocalDateTime timestamp;

		// Construtores, getters e setters

		public Message(String content, String type, LocalDateTime timestamp) {
			this.content = content;
			this.type = type;
			this.timestamp = timestamp;
		}

		public String getContent() {
			return content;
		}

		public String getType() {
			return type;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}
	}
}
