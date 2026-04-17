package com.neuroguard.consultationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long consultationId;
    private Long patientId;
    private Long doctorId;
    private String status;
    private String eventType; // CREATED, COMPLETED, CANCELLED
    private LocalDateTime timestamp;
    private String consultationType;
    private String notes;
}
