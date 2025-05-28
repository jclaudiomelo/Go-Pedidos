package com.GoPedidos.GoPedidos.RabbitMQ;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String PEDIDO_QUEUE = "pedido.queue";
	public static final String EXCHANGE_NAME = "pedido.exchange";
	public static final String ROUTING_KEY = "pedido.routingKey";
	public static final String DLX_NAME = "pedido.dlx";
	public static final String DLQ_NAME = "pedido.dlq";

	@Bean
	public Queue queue() {
		return QueueBuilder.durable(PEDIDO_QUEUE)
				.withArgument("x-dead-letter-exchange", DLX_NAME) // Configura DLX
				.withArgument("x-dead-letter-routing-key", DLQ_NAME) // Configura DLQ
				.build();
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter(); // Certifique-se de que o Jackson está configurado para converter corretamente para JSON
	}

	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(EXCHANGE_NAME);
	}

	@Bean
	public Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
	}

	@Bean
	public DirectExchange deadLetterExchange() {
		return new DirectExchange(DLX_NAME);
	}

	@Bean
	public Queue deadLetterQueue() {
		return QueueBuilder.durable(DLQ_NAME).build();
	}

	@Bean
	public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
		return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_NAME);
	}
}
