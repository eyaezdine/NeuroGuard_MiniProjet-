package com.neuroguard.medicalhistoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long medicalRecordId;
    private Long patientId;
    private String recordType;
    private String eventType; // CREATED, UPDATED, DELETED
    private LocalDateTime timestamp;
    private String description;
    private String diagnosis;
    private String treatment;
}
