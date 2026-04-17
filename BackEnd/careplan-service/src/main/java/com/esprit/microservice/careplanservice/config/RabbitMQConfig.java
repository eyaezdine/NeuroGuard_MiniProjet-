package com.esprit.microservice.careplanservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queues
    public static final String CAREPLAN_CREATED_QUEUE = "careplan.created";
    public static final String CAREPLAN_UPDATED_QUEUE = "careplan.updated";
    public static final String CAREPLAN_DELETED_QUEUE = "careplan.deleted";
    public static final String CONSULTATION_COMPLETED_QUEUE = "consultation.completed";

    // Exchange
    public static final String CAREPLAN_EXCHANGE = "careplan-exchange";

    // Routing Keys
    public static final String CAREPLAN_CREATED_KEY = "careplan.created";
    public static final String CAREPLAN_UPDATED_KEY = "careplan.updated";
    public static final String CAREPLAN_DELETED_KEY = "careplan.deleted";
    public static final String CONSULTATION_COMPLETED_KEY = "consultation.completed";

    // Queues
    @Bean
    public Queue carePlanCreatedQueue() {
        return QueueBuilder.durable(CAREPLAN_CREATED_QUEUE).build();
    }

    @Bean
    public Queue carePlanUpdatedQueue() {
        return QueueBuilder.durable(CAREPLAN_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue carePlanDeletedQueue() {
        return QueueBuilder.durable(CAREPLAN_DELETED_QUEUE).build();
    }

    @Bean
    public Queue consultationCompletedQueue() {
        return QueueBuilder.durable(CONSULTATION_COMPLETED_QUEUE).build();
    }

    // Exchange
    @Bean
    public DirectExchange carePlanExchange() {
        return new DirectExchange(CAREPLAN_EXCHANGE, true, false);
    }

    // Bindings
    @Bean
    public Binding carePlanCreatedBinding(Queue carePlanCreatedQueue, DirectExchange carePlanExchange) {
        return BindingBuilder.bind(carePlanCreatedQueue)
                .to(carePlanExchange)
                .with(CAREPLAN_CREATED_KEY);
    }

    @Bean
    public Binding carePlanUpdatedBinding(Queue carePlanUpdatedQueue, DirectExchange carePlanExchange) {
        return BindingBuilder.bind(carePlanUpdatedQueue)
                .to(carePlanExchange)
                .with(CAREPLAN_UPDATED_KEY);
    }

    @Bean
    public Binding carePlanDeletedBinding(Queue carePlanDeletedQueue, DirectExchange carePlanExchange) {
        return BindingBuilder.bind(carePlanDeletedQueue)
                .to(carePlanExchange)
                .with(CAREPLAN_DELETED_KEY);
    }
}
