package com.neuroguard.consultationservice.listener;

import com.neuroguard.consultationservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CarePlanEventListener {

    @RabbitListener(queues = RabbitMQConfig.CAREPLAN_CREATED_QUEUE)
    public void handleCarePlanCreated(String message) {
        log.info("Received care plan created event in consultation service: {}", message);
        // Trigger notification or schedule consultation when a new care plan is created
    }
}
