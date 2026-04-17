package com.neuroguard.medicalhistoryservice.listener;

import com.neuroguard.medicalhistoryservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CarePlanEventListener {

    @RabbitListener(queues = RabbitMQConfig.CAREPLAN_UPDATED_QUEUE)
    public void handleCarePlanUpdated(String message) {
        log.info("Received care plan updated event in medical history service: {}", message);
        // Sync medical records with updated care plan information
    }
}
