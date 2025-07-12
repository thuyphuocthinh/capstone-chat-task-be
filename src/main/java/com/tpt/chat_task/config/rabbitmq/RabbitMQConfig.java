package com.tpt.chat_task.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
@EnableRabbit
public class RabbitMQConfig implements RabbitListenerConfigurer {

    @Value("${spring.rabbitmq.login.queue}")
    private String loginQueue;

    @Value("${spring.rabbitmq.login.exchange}")
    private String loginExchange;

    @Value("${spring.rabbitmq.login.routing-key}")
    private String loginRoutingKey;

    @Value("${spring.rabbitmq.notification.queue}")
    private String notificationQueue;

    @Value("${spring.rabbitmq.notification.exchange}")
    private String notificationExchange;

    @Value("${spring.rabbitmq.notification.routing-key}")
    private String notificationRoutingKey;

    @Value("${spring.rabbitmq.conversations.members.add-queue}")
    private String conversationAddMemberQueue;

    @Value("${spring.rabbitmq.conversations.members.add-exchange}")
    private String conversationAddMemberExchange;

    @Value("${spring.rabbitmq.conversations.members.add-routing-key}")
    private String conversationAddMemberRoutingKey;

    @Value("${spring.rabbitmq.conversations.members.delete-queue}")
    private String conversationDeleteMemberQueue;

    @Value("${spring.rabbitmq.conversations.members.delete-exchange}")
    private String conversationDeleteMemberExchange;

    @Value("${spring.rabbitmq.conversations.members.delete-routing-key}")
    private String conversationDeleteMemberRoutingKey;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry() {
        return new RabbitListenerEndpointRegistry();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(consumerJackson2MessageConverter());
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(1);
        factory.setConsecutiveActiveTrigger(1);
        factory.setConsecutiveIdleTrigger(1);
        factory.setConnectionFactory(connectionFactory);
        registrar.setContainerFactory(factory);
        registrar.setEndpointRegistry(rabbitListenerEndpointRegistry());
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    public DirectExchange loginExchange() {
        return new DirectExchange(loginExchange);
    }

    @Bean
    public Queue loginQueue() {
        return new Queue(loginQueue, true);
    }

    @Bean
    public Binding loginBinding(Queue loginQueue, DirectExchange loginExchange) {
        return BindingBuilder.bind(loginQueue).to(loginExchange).with(loginRoutingKey);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(notificationExchange);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(notificationRoutingKey);
    }

    @Bean
    public DirectExchange conversationAddMemberExchange() {
        return new DirectExchange(conversationAddMemberExchange);
    }

    @Bean
    public Queue conversationAddMemberQueue() {
        return new Queue(conversationAddMemberQueue, true);
    }

    @Bean
    public Binding conversationAddMemberBinding(Queue conversationAddMemberQueue, DirectExchange conversationAddMemberExchange) {
        return BindingBuilder.bind(conversationAddMemberQueue).to(conversationAddMemberExchange).with(conversationAddMemberRoutingKey);
    }

    @Bean
    public DirectExchange conversationDeleteMemberExchange() {
        return new DirectExchange(conversationDeleteMemberExchange);
    }

    @Bean
    public Queue conversationDeleteMemberQueue() {
        return new Queue(conversationDeleteMemberQueue, true);
    }

    @Bean
    public Binding conversationDeleteMemberBinding(Queue conversationDeleteMemberQueue, DirectExchange conversationDeleteMemberExchange) {
        return BindingBuilder.bind(conversationDeleteMemberQueue).to(conversationDeleteMemberExchange).with(conversationDeleteMemberRoutingKey);
    }
}
