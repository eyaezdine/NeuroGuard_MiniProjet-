package com.esprit.microservice.careplanservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarePlanEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long carePlanId;
    private Long patientId;
    private Long caregiverId;
    private String status;
    private String eventType; // CREATED, UPDATED, DELETED
    private LocalDateTime timestamp;
    private String description;
}
