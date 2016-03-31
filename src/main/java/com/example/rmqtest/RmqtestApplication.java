package com.example.rmqtest;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableRabbit
@RestController
public class RmqtestApplication implements CommandLineRunner {

	private final String queueName = "rmq-test";

	@Autowired
	private AnnotationConfigApplicationContext context;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange("spring-boot-exchange");
	}

	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(queueName);
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	Receiver receiver() {
		return new Receiver();
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Override
	public void run(String... args) throws Exception {
		// Send continuously new messages
		while ( true ) {

			// execute in a try/catch to ensure that in case of a
			// connection error a recovery is possible
			try {
				System.out.println("Waiting five seconds...");
				Thread.sleep(5000);
				System.out.println("Sending message...");
				rabbitTemplate.convertAndSend(queueName, "Hello from RabbitMQ!");

				// if we have not received an answer within 10s we are going to throw
				// an InterruptedException
				receiver().getLatch().await(10000, TimeUnit.MILLISECONDS);
			} catch(RuntimeException e) {
				// Once something goes wrong e.g. IOException due to RabbitMQ being
				// down or due to a network issue (Connection/Timeout issue) we
				// should be handling the error gracefully.
				//
				// In the current setup (using Spring AMQP) we could have also
				// catched a AMQPException and an InterruptedException specifically
				System.err.println("Something went wrong. " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(RmqtestApplication.class, args);
	}
}
