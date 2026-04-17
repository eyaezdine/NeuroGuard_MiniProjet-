package com.neuroguard.medicalhistoryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queues
    public static final String MEDICAL_HISTORY_CREATED_QUEUE = "medical-history.created";
    public static final String MEDICAL_HISTORY_UPDATED_QUEUE = "medical-history.updated";
    public static final String CAREPLAN_UPDATED_QUEUE = "careplan.updated.medical";

    // Exchange
    public static final String MEDICAL_HISTORY_EXCHANGE = "medical-history-exchange";
    public static final String CAREPLAN_EXCHANGE = "careplan-exchange";

    // Routing Keys
    public static final String MEDICAL_HISTORY_CREATED_KEY = "medical-history.created";
    public static final String MEDICAL_HISTORY_UPDATED_KEY = "medical-history.updated";
    public static final String CAREPLAN_UPDATED_KEY = "careplan.updated";

    // Medical History Queues
    @Bean
    public Queue medicalHistoryCreatedQueue() {
        return QueueBuilder.durable(MEDICAL_HISTORY_CREATED_QUEUE).build();
    }

    @Bean
    public Queue medicalHistoryUpdatedQueue() {
        return QueueBuilder.durable(MEDICAL_HISTORY_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue carePlanUpdatedQueue() {
        return QueueBuilder.durable(CAREPLAN_UPDATED_QUEUE).build();
    }

    // Medical History Exchange
    @Bean
    public DirectExchange medicalHistoryExchange() {
        return new DirectExchange(MEDICAL_HISTORY_EXCHANGE, true, false);
    }

    // Careplan Exchange (for listening)
    @Bean
    public DirectExchange carePlanExchange() {
        return new DirectExchange(CAREPLAN_EXCHANGE, true, false);
    }

    // Medical History Bindings
    @Bean
    public Binding medicalHistoryCreatedBinding(Queue medicalHistoryCreatedQueue, DirectExchange medicalHistoryExchange) {
        return BindingBuilder.bind(medicalHistoryCreatedQueue)
                .to(medicalHistoryExchange)
                .with(MEDICAL_HISTORY_CREATED_KEY);
    }

    @Bean
    public Binding medicalHistoryUpdatedBinding(Queue medicalHistoryUpdatedQueue, DirectExchange medicalHistoryExchange) {
        return BindingBuilder.bind(medicalHistoryUpdatedQueue)
                .to(medicalHistoryExchange)
                .with(MEDICAL_HISTORY_UPDATED_KEY);
    }

    // Careplan Binding (for receiving careplan events)
    @Bean
    public Binding carePlanUpdatedBinding(Queue carePlanUpdatedQueue, DirectExchange carePlanExchange) {
        return BindingBuilder.bind(carePlanUpdatedQueue)
                .to(carePlanExchange)
                .with(CAREPLAN_UPDATED_KEY);
    }
}
