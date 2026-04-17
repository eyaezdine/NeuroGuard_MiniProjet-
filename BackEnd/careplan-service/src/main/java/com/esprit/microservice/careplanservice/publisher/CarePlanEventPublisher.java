package com.esprit.microservice.careplanservice.publisher;

import com.esprit.microservice.careplanservice.config.RabbitMQConfig;
import com.esprit.microservice.careplanservice.event.CarePlanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarePlanEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCarePlanCreated(CarePlanEvent event) {
        log.info("Publishing CarePlan created event: {}", event.getCarePlanId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAREPLAN_EXCHANGE,
                RabbitMQConfig.CAREPLAN_CREATED_KEY,
                event
        );
    }

    public void publishCarePlanUpdated(CarePlanEvent event) {
        log.info("Publishing CarePlan updated event: {}", event.getCarePlanId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAREPLAN_EXCHANGE,
                RabbitMQConfig.CAREPLAN_UPDATED_KEY,
                event
        );
    }

    public void publishCarePlanDeleted(CarePlanEvent event) {
        log.info("Publishing CarePlan deleted event: {}", event.getCarePlanId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CAREPLAN_EXCHANGE,
                RabbitMQConfig.CAREPLAN_DELETED_KEY,
                event
        );
    }
}
