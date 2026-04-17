package com.esprit.microservice.careplanservice.listener;

import com.esprit.microservice.careplanservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsultationEventListener {

    @RabbitListener(queues = RabbitMQConfig.CONSULTATION_COMPLETED_QUEUE)
    public void handleConsultationCompleted(String message) {
        log.info("Received consultation completed event: {}", message);
        // Update care plan status based on completed consultation
        // This is a reactive event handler that updates the care plan accordingly
    }
}
