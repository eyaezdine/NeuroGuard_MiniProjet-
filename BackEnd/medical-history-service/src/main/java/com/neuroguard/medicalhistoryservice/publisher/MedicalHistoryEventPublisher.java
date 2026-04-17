package com.neuroguard.medicalhistoryservice.publisher;

import com.neuroguard.medicalhistoryservice.config.RabbitMQConfig;
import com.neuroguard.medicalhistoryservice.event.MedicalHistoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalHistoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMedicalHistoryCreated(MedicalHistoryEvent event) {
        log.info("Publishing Medical History created event: {}", event.getMedicalRecordId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MEDICAL_HISTORY_EXCHANGE,
                RabbitMQConfig.MEDICAL_HISTORY_CREATED_KEY,
                event
        );
    }

    public void publishMedicalHistoryUpdated(MedicalHistoryEvent event) {
        log.info("Publishing Medical History updated event: {}", event.getMedicalRecordId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MEDICAL_HISTORY_EXCHANGE,
                RabbitMQConfig.MEDICAL_HISTORY_UPDATED_KEY,
                event
        );
    }
}
