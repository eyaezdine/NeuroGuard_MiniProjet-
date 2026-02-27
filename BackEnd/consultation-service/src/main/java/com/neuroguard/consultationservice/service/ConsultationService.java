package com.neuroguard.consultationservice.service;

import com.neuroguard.consultationservice.dto.ApiResponse;
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
    public ConsultationResponse createConsultation(ConsultationRequest request, String providerId) {
        // Vérifier que le patient existe
        UserDto patient = fetchUserById(request.getPatientId(), "Patient");
        if (!"PATIENT".equals(patient.getRole())) {
            throw new IllegalArgumentException("L'utilisateur spécifié n'est pas un patient valide");
        }

        // Vérifier le caregiver si fourni
        if (request.getCaregiverId() != null) {
            UserDto caregiver = fetchUserById(request.getCaregiverId(), "Soignant");
            if (!"CAREGIVER".equals(caregiver.getRole())) {
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

    private UserDto fetchUserById(String userId, String userType) {
        try {
            ApiResponse<UserDto> response = userServiceClient.getUserById(userId);
            if (!response.isSuccess() || response.getData() == null) {
                throw new ResourceNotFoundException(userType + " avec l'ID " + userId + " non trouvé");
            }
            return response.getData();
        } catch (FeignException.NotFound ex) {
            logger.error("{} not found with id: {}", userType, userId);
            throw new ResourceNotFoundException(userType + " avec l'ID " + userId + " non trouvé");
        } catch (FeignException ex) {
            logger.error("Error calling user-service for {}: {}", userType, ex.getMessage());
            throw new IllegalStateException("Impossible de vérifier les données. Le service utilisateur est indisponible");
        }
    }

    @Transactional
    public ConsultationResponse updateConsultation(Long id, ConsultationRequest request, String userId, String role) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        if (!"PROVIDER".equals(role) || !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette consultation");
        }

        consultation.setTitle(request.getTitle());
        consultation.setDescription(request.getDescription());
        consultation.setStartTime(request.getStartTime());
        consultation.setEndTime(request.getEndTime());
        consultation.setType(request.getType());
        consultation.setPatientId(request.getPatientId());
        consultation.setCaregiverId(request.getCaregiverId());
        consultation.setUpdatedAt(LocalDateTime.now());

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
    public void deleteConsultation(Long id, String userId, String role) {
        Consultation consultation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        if (!"PROVIDER".equals(role) || !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette consultation");
        }

        repository.delete(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByProvider(String providerId) {
        return repository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByPatient(String patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultationsByCaregiver(String caregiverId) {
        return repository.findByCaregiverId(caregiverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getJoinLink(Long consultationId, String userId, String role) {
        Consultation consultation = repository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée"));

        if ("PROVIDER".equals(role) && !consultation.getProviderId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas le provider de cette consultation");
        }
        if ("PATIENT".equals(role) && !consultation.getPatientId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas le patient de cette consultation");
        }
        if ("CAREGIVER".equals(role)) {
            if (consultation.getCaregiverId() == null || !consultation.getCaregiverId().equals(userId)) {
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