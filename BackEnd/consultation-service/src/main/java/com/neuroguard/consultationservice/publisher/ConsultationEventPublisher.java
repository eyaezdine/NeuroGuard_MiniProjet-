package com.neuroguard.consultationservice.publisher;

import com.neuroguard.consultationservice.config.RabbitMQConfig;
import com.neuroguard.consultationservice.event.ConsultationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishConsultationCreated(ConsultationEvent event) {
        log.info("Publishing Consultation created event: {}", event.getConsultationId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONSULTATION_EXCHANGE,
                RabbitMQConfig.CONSULTATION_CREATED_KEY,
                event
        );
    }

    public void publishConsultationCompleted(ConsultationEvent event) {
        log.info("Publishing Consultation completed event: {}", event.getConsultationId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONSULTATION_EXCHANGE,
                RabbitMQConfig.CONSULTATION_COMPLETED_KEY,
                event
        );
    }
}
