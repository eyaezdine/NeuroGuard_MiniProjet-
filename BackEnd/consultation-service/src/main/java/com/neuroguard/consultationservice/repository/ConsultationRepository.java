package com.neuroguard.consultationservice.repository;

import com.neuroguard.consultationservice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    // Les paramètres doivent être de type String pour correspondre aux champs de l'entité
    List<Consultation> findByProviderId(String providerId);
    List<Consultation> findByPatientId(String patientId);
    List<Consultation> findByCaregiverId(String caregiverId);
}