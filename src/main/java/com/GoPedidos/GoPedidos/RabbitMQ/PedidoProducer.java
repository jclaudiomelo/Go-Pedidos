package com.GoPedidos.GoPedidos.RabbitMQ;

import com.GoPedidos.GoPedidos.DTOS.PedidoDTO;
import com.GoPedidos.GoPedidos.DTOS.PedidoSimplificadoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class PedidoProducer {

	private final RabbitTemplate rabbitTemplate;

	public PedidoProducer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public PedidoSimplificadoDTO converterParaPedidoSimplificado(PedidoDTO pedidoDTO) {
		PedidoSimplificadoDTO simplificadoDTO = new PedidoSimplificadoDTO();
		simplificadoDTO.setPedidoId(pedidoDTO.pedidoId());
		simplificadoDTO.setClienteId(pedidoDTO.clienteId());
		simplificadoDTO.setGarcomId(pedidoDTO.garcomId());
		simplificadoDTO.setDataHoraPedido(pedidoDTO.dataHoraPedido());
		simplificadoDTO.setStatusPedidoId(pedidoDTO.statusPedidoId());
		simplificadoDTO.setItensPedido(pedidoDTO.itensPedido());
//		simplificadoDTO.setTotalPedido(pedidoDTO.totalPedido().doubleValue());
		simplificadoDTO.setMetodoPagamentoId(pedidoDTO.metodoPagamentoId());
		simplificadoDTO.setObservacoes(pedidoDTO.observacoes());
		simplificadoDTO.setMesaId(pedidoDTO.mesaId());
//		simplificadoDTO.setDescontoAplicado(pedidoDTO.descontoAplicado().doubleValue());
		return simplificadoDTO;
	}

	public void enviarPedidoCriado(PedidoDTO pedidoDTO) {
		PedidoSimplificadoDTO pedidoSimplificadoDTO = converterParaPedidoSimplificado(pedidoDTO);
		log.info("Enviando pedido simplificado para RabbitMQ: {}", pedidoSimplificadoDTO);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, pedidoSimplificadoDTO);
	}
}
