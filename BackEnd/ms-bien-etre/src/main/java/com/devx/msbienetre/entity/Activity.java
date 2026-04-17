package com.devx.msbienetre.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private LocalDate date;

    private String activityType; // WALKING, EXERCISE, PHYSIOTHERAPY, GARDENING, OTHER

    private Integer durationMinutes;

    private String intensity; // LOW, MODERATE, HIGH

    private String assistedBy;

    @Column(length = 1000)
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
