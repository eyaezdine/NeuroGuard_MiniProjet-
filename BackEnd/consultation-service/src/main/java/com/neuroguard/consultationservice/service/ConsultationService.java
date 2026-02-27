package com.neuroguard.consultationservice.service;

import com.neuroguard.consultationservice.dto.ConsultationRequest;
import com.neuroguard.consultationservice.dto.ConsultationResponse;
import com.neuroguard.consultationservice.dto.UserDto;
import com.neuroguard.consultationservice.entity.Consultation;
import com.neuroguard.consultationservice.entity.ConsultationStatus;
import com.neuroguard.consultationservice.entity.ConsultationType;
import com.neuroguard.consultationservice.exception.ResourceNotFoundException;
import com.neuroguard.consultationservice.exception.UnauthorizedException;
import com.neuroguard.consultationservice.repository.ConsultationRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultationService.class);

    private final ConsultationRepository repository;
    private final UserServiceClient userServiceClient;
    private final ZoomService zoomService;

    public ConsultationService(ConsultationRepository repository,
                               UserServiceClient userServiceClient,
                               ZoomService zoomService) {
        this.repository = repository;
        this.userServiceClient = userServiceClient;
        this.zoomService = zoomService;
    }

    @Transactional
    public ConsultationResponse createConsultation(ConsultationRequest request, Long providerId) {
        // Vérifier que le patient existe
        UserDto patient = null;
        try {
            patient = userServiceClient.getUserById(request.getPatientId());
        } catch (FeignException.NotFound ex) {
            logger.error("Patient not found with id: {}", request.getPatientId());
            throw new ResourceNotFoundException("Patient avec l'ID " + request.getPatientId() + " non trouvé");
        } catch (FeignException ex) {
            logger.error("Error calling user-service for patient: {}", ex.getMessage());
            throw new IllegalStateException("Impossible de vérifier les données du patient. Le service utilisateur est indisponible");
        }

        if (patient == null || !"PATIENT".equals(patient.getRole())) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un patient valide");
        }

        // Si un caregiver est fourni, vérifier qu'il existe et a le rôle CAREGIVER
        if (request.getCaregiverId() != null) {
            UserDto caregiver = null;
            try {
                caregiver = userServiceClient.getUserById(request.getCaregiverId());
            } catch (FeignException.NotFound ex) {
                logger.error("Caregiver not found with id: {}", request.getCaregiverId());
                throw new ResourceNotFoundException("Soignant avec l'ID " + request.getCaregiverId() + " non trouvé");
            } catch (FeignException ex) {
                logger.error("Error calling user-service for caregiver: {}", ex.getMessage());
                throw new IllegalStateException("Impossible de vérifier les données du soignant. Le service utilisateur est indisponible");
            }

            if (caregiver == null || !"CAREGIVER".equals(caregiver.getRole())) {
                throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un soignant valide");
            }
        }

        Consultation consultation = new Consultation();
        consultation.setTitle(request.getTitle());
        consultation.setDescription(request.getDescription());
        consultation.setStartTime(request.getStartTime());
        consultation.setEndTime(request.getEndTime());
        consultation.setType(request.getType());
        consultation.setProviderId(providerId);
        consultation.setPatientId(request.getPatientId());
        consultation.setCaregiverId(request.getCaregiverId());
        consultation.setStatus(ConsultationStatus.SCHEDULED);
        consultation.setCreatedAt(LocalDateTime.now());

        if (request.getType() == ConsultationType.ONLINE) {
            // Appel à ZoomService pour créer une réunion
            try {
                ZoomService.MeetingInfo meeting = zoomService.createMeeting(
                        request.getTitle(),
                        request.getStartTime(),
                        request.getEndTime() != null ?
                                java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() : 30
                );
                consultation.setMeetingLink(meeting.getJoinUrl());
                consultation.setMeetingId(meeting.getMeetingId());
            } catch (Exception ex) {
                logger.error("Error creating Zoom meeting: {}", ex.getMessage());
                throw new IllegalStateException("Impossible de créer la réunion Zoom");
            }
        }

        Consultation saved = repository.save(consultation);
        logger.info("Consultation created successfully with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ConsultationResponse updateConsultation(Long id, ConsultationRequest request, Long userId, String role) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        // Seul le provider créateur peut modifier
        if (!"PROVIDER".equals(role) || !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette consultation");
        }

        // Mise à jour des champs (sauf ceux qui ne doivent pas être modifiés après création)
        consultation.setTitle(request.getTitle());
        consultation.setDescription(request.getDescription());
        consultation.setStartTime(request.getStartTime());
        consultation.setEndTime(request.getEndTime());
        consultation.setType(request.getType());
        consultation.setPatientId(request.getPatientId());
        consultation.setCaregiverId(request.getCaregiverId());
        consultation.setUpdatedAt(LocalDateTime.now());

        // Si le type passe en ONLINE et qu'il n'y a pas encore de lien, en créer un
        if (request.getType() == ConsultationType.ONLINE && consultation.getMeetingLink() == null) {
            ZoomService.MeetingInfo meeting = zoomService.createMeeting(
                    request.getTitle(),
                    request.getStartTime(),
                    request.getEndTime() != null ?
                            java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() : 30
            );
            consultation.setMeetingLink(meeting.getJoinUrl());
            consultation.setMeetingId(meeting.getMeetingId());
        }

        Consultation updated = repository.save(consultation);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteConsultation(Long id, Long userId, String role) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        if (!"PROVIDER".equals(role) || !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette consultation");
        }

        // On peut implémenter une suppression logique (changement de statut) ou physique
        repository.delete(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByProvider(Long providerId) {
        return repository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByPatient(Long patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByCaregiver(Long caregiverId) {
        // Deux approches : soit on a un champ caregiverId dans la consultation, soit on récupère les patients associés
        // Ici on utilise le champ caregiverId pour simplifier.
        return repository.findByCaregiverId(caregiverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getJoinLink(Long consultationId, Long userId, String role) {
        Consultation consultation = repository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        // Vérification des droits selon le rôle
        if ("PROVIDER".equals(role) && !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas le provider de cette consultation");
        }
        if ("PATIENT".equals(role) && !consultation.getPatientId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas le patient de cette consultation");
        }
        if ("CAREGIVER".equals(role)) {
            // Vérifier que le caregiver est associé à cette consultation (via le champ caregiverId ou via patient)
            if (consultation.getCaregiverId() == null || !consultation.getCaregiverId().equals(userId)) {
                // Alternative : appeler user-service pour vérifier l'association caregiver-patient
                throw new UnauthorizedException("Vous n'êtes pas associé à cette consultation");
            }
        }

        if (consultation.getType() != ConsultationType.ONLINE) {
            throw new IllegalStateException("Cette consultation n'est pas en ligne");
        }

        return consultation.getMeetingLink();
    }

    private ConsultationResponse mapToResponse(Consultation c) {
        ConsultationResponse resp = new ConsultationResponse();
        resp.setId(c.getId());
        resp.setTitle(c.getTitle());
        resp.setDescription(c.getDescription());
        resp.setStartTime(c.getStartTime());
        resp.setEndTime(c.getEndTime());
        resp.setType(c.getType());
        resp.setStatus(c.getStatus());
        resp.setMeetingLink(c.getMeetingLink());
        resp.setProviderId(c.getProviderId());
        resp.setPatientId(c.getPatientId());
        resp.setCaregiverId(c.getCaregiverId());
        resp.setCreatedAt(c.getCreatedAt());
        return resp;
    }
}