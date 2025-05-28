package com.GoPedidos.GoPedidos.RabbitMQ;

import com.GoPedidos.GoPedidos.DTOS.PedidoSimplificadoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PedidoConsumer {

	private final SimpMessagingTemplate messagingTemplate;

	public PedidoConsumer(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@RabbitListener(queues = RabbitMQConfig.PEDIDO_QUEUE)
	public void consumirMensagem(PedidoSimplificadoDTO pedidoSimplificadoDTO) {
		try {
			log.info("📩 Mensagem recebida: {}", pedidoSimplificadoDTO);

			// Simula processamento (ex: notificar a cozinha)
			log.info("🍔 Notificando cozinha sobre o pedido: {}", pedidoSimplificadoDTO.getPedidoId());

			// Lógica de processamento do pedido
			processarPedido(pedidoSimplificadoDTO);

			// Envia a mensagem para o frontend via WebSocket
			messagingTemplate.convertAndSend("/topic/pedidos", pedidoSimplificadoDTO);

		} catch (Exception e) {
			log.error("Erro ao processar pedido: {}", e.getMessage());
			throw new RuntimeException("Erro ao processar pedido", e);
		}
	}

	private void processarPedido(PedidoSimplificadoDTO pedidoSimplificadoDTO) {
		// Implementação da lógica de processamento do pedido
		log.info("Pedido processado com sucesso: {}", pedidoSimplificadoDTO);
	}
}

//@Slf4j
//@Component
//public class PedidoConsumer {
//
//	private final SimpMessagingTemplate messagingTemplate;
//
//	public PedidoConsumer(SimpMessagingTemplate messagingTemplate) {
//		this.messagingTemplate = messagingTemplate;
//	}
//
//	@RabbitListener(queues = RabbitMQConfig.PEDIDO_QUEUE)
//	public void consumirMensagem(PedidoDTO pedidoDTO) {
//		try {
//			log.info("📩 Pedido recebido: {}", pedidoDTO);
//
//			// Enviar para WebSocket
//			messagingTemplate.convertAndSend("/cozinha/pedidos", pedidoDTO);
//			log.info("📡 Pedido enviado para WebSocket!");
//
//		} catch (Exception e) {
//			log.error("Erro ao processar pedido: {}", e.getMessage());
//			throw new RuntimeException("Erro ao processar pedido", e);
//		}
//	}
//}