package com.neuroguard.consultationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queues
    public static final String CONSULTATION_CREATED_QUEUE = "consultation.created";
    public static final String CONSULTATION_COMPLETED_QUEUE = "consultation.completed";
    public static final String CAREPLAN_CREATED_QUEUE = "careplan.created.consultation";

    // Exchange
    public static final String CONSULTATION_EXCHANGE = "consultation-exchange";
    public static final String CAREPLAN_EXCHANGE = "careplan-exchange";

    // Routing Keys
    public static final String CONSULTATION_CREATED_KEY = "consultation.created";
    public static final String CONSULTATION_COMPLETED_KEY = "consultation.completed";
    public static final String CAREPLAN_CREATED_KEY = "careplan.created";

    // Consultation Queues
    @Bean
    public Queue consultationCreatedQueue() {
        return QueueBuilder.durable(CONSULTATION_CREATED_QUEUE).build();
    }

    @Bean
    public Queue consultationCompletedQueue() {
        return QueueBuilder.durable(CONSULTATION_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue carePlanCreatedQueue() {
        return QueueBuilder.durable(CAREPLAN_CREATED_QUEUE).build();
    }

    // Consultation Exchange
    @Bean
    public DirectExchange consultationExchange() {
        return new DirectExchange(CONSULTATION_EXCHANGE, true, false);
    }

    // Careplan Exchange (for listening to careplan events)
    @Bean
    public DirectExchange carePlanExchange() {
        return new DirectExchange(CAREPLAN_EXCHANGE, true, false);
    }

    // Consultation Bindings
    @Bean
    public Binding consultationCreatedBinding(Queue consultationCreatedQueue, DirectExchange consultationExchange) {
        return BindingBuilder.bind(consultationCreatedQueue)
                .to(consultationExchange)
                .with(CONSULTATION_CREATED_KEY);
    }

    @Bean
    public Binding consultationCompletedBinding(Queue consultationCompletedQueue, DirectExchange consultationExchange) {
        return BindingBuilder.bind(consultationCompletedQueue)
                .to(consultationExchange)
                .with(CONSULTATION_COMPLETED_KEY);
    }

    // Careplan Binding (for receiving careplan events)
    @Bean
    public Binding carePlanCreatedBinding(Queue carePlanCreatedQueue, DirectExchange carePlanExchange) {
        return BindingBuilder.bind(carePlanCreatedQueue)
                .to(carePlanExchange)
                .with(CAREPLAN_CREATED_KEY);
    }
}
