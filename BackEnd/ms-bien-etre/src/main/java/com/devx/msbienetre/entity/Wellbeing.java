package com.devx.msbienetre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wellbeing")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wellbeing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private LocalDate date;

    private String mood; // HAPPY, SAD, IRRITATED...

    private Double sleepHours;

    private Integer stressLevel; // 1-5

    private Integer memoryDifficulty; // 1-5

    private String appetite; // GOOD, NORMAL, LOW

    @Column(length = 1000)
    private String notes;

    private Boolean riskFlag;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        // Automatically set the createdAt timestamp
        this.createdAt = LocalDateTime.now();

        // If date is not provided, set to today
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
