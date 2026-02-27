package com.neuroguard.consultationservice.repository;

import com.neuroguard.consultationservice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByProviderId(Long providerId);
    List<Consultation> findByPatientId(Long patientId);
    List<Consultation> findByCaregiverId(Long caregiverId);
    // Pour les caregivers sans champ caregiverId direct, on peut aussi rechercher par patientId et ensuite filtrer,
    // mais ici on utilise le champ caregiverId pour simplifier.
}