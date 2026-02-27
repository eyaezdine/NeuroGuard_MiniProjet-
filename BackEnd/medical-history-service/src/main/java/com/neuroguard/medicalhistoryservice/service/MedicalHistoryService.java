package com.neuroguard.medicalhistoryservice.service;

import com.neuroguard.medicalhistoryservice.client.UserServiceClient;
import com.neuroguard.medicalhistoryservice.dto.FileDto;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryRequest;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryResponse;
import com.neuroguard.medicalhistoryservice.dto.UserDto;
import com.neuroguard.medicalhistoryservice.entity.MedicalHistory;
import com.neuroguard.medicalhistoryservice.entity.MedicalRecordFile;
import com.neuroguard.medicalhistoryservice.repository.MedicalHistoryRepository;
import com.neuroguard.medicalhistoryservice.repository.MedicalRecordFileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalHistoryService {

    private static final Logger log = LoggerFactory.getLogger(MedicalHistoryService.class);

    private final MedicalHistoryRepository historyRepository;
    private final MedicalRecordFileRepository fileRepository;
    private final UserServiceClient userServiceClient;

    private final String uploadDir = "uploads/medical-history/";

    // ------------------- Provider Operations -------------------
    public List<MedicalHistoryResponse> getAllMedicalHistoriesForProvider(String providerId) {
        List<MedicalHistory> histories = historyRepository.findByProviderId(providerId);
        return histories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ------------------- Caregiver Operations -------------------
    public List<MedicalHistoryResponse> getAllMedicalHistoriesForCaregiver(String caregiverId) {
        List<MedicalHistory> histories = historyRepository.findByCaregiverId(caregiverId);
        return histories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public MedicalHistoryResponse createMedicalHistory(MedicalHistoryRequest request, String providerId) {
        if (historyRepository.existsByPatientId(request.getPatientId())) {
            throw new RuntimeException("Medical history already exists for patient: " + request.getPatientId());
        }

        MedicalHistory history = mapRequestToEntity(request);

        if (!history.getProviderIds().contains(providerId)) {
            history.getProviderIds().add(providerId);
        }

        List<String> caregiverIds = new ArrayList<>();
        if (request.getCaregiverIds() != null && !request.getCaregiverIds().isEmpty()) {
            caregiverIds = request.getCaregiverIds();
        } else if (request.getCaregiverNames() != null && !request.getCaregiverNames().isEmpty()) {
            caregiverIds = resolveCaregiverNamesToIds(request.getCaregiverNames());
        }
        history.setCaregiverIds(caregiverIds);

        history = historyRepository.save(history);
        return mapToResponse(history);
    }

    @Transactional
    public MedicalHistoryResponse updateMedicalHistory(String patientId, MedicalHistoryRequest request, String providerId) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        if (!history.getProviderIds().contains(providerId)) {
            throw new RuntimeException("Provider not assigned to this patient");
        }

        updateEntityFromRequest(history, request);

        if (request.getCaregiverIds() != null) {
            history.setCaregiverIds(request.getCaregiverIds());
        } else if (request.getCaregiverNames() != null && !request.getCaregiverNames().isEmpty()) {
            history.setCaregiverIds(resolveCaregiverNamesToIds(request.getCaregiverNames()));
        }

        if (request.getProviderIds() != null) {
            for (String newProviderId : request.getProviderIds()) {
                if (!history.getProviderIds().contains(newProviderId)) {
                    history.getProviderIds().add(newProviderId);
                }
            }
        }
        if (!history.getProviderIds().contains(providerId)) {
            history.getProviderIds().add(providerId);
        }

        history = historyRepository.save(history);
        return mapToResponse(history);
    }

    @Transactional
    public void deleteMedicalHistory(String patientId, String providerId) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        if (!history.getProviderIds().contains(providerId)) {
            throw new RuntimeException("Provider not assigned to this patient");
        }

        for (MedicalRecordFile file : history.getFiles()) {
            deleteFileFromDisk(file.getFilePath());
        }
        historyRepository.delete(history);
    }

    public MedicalHistoryResponse getMedicalHistoryByPatientId(String patientId, String requesterId, String requesterRole) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        switch (requesterRole) {
            case "PATIENT":
                if (!history.getPatientId().equals(requesterId)) {
                    throw new RuntimeException("Access denied: You can only view your own medical history");
                }
                break;
            case "PROVIDER":
                if (!history.getProviderIds().contains(requesterId)) {
                    throw new RuntimeException("Access denied: Provider not assigned to this patient");
                }
                break;
            case "CAREGIVER":
                if (!history.getCaregiverIds().contains(requesterId)) {
                    throw new RuntimeException("Access denied: Caregiver not assigned to this patient");
                }
                break;
            default:
                throw new RuntimeException("Access denied");
        }

        return mapToResponse(history);
    }

    // ------------------- File Operations -------------------

    @Transactional
    public FileDto uploadFile(String patientId, MultipartFile file, String requesterId, String requesterRole) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        if (requesterRole.equals("PATIENT") && !history.getPatientId().equals(requesterId)) {
            throw new RuntimeException("Access denied: You can only upload files to your own medical history");
        } else if (requesterRole.equals("PROVIDER") && !history.getProviderIds().contains(requesterId)) {
            throw new RuntimeException("Access denied: Provider not assigned to this patient");
        } else if (!requesterRole.equals("PATIENT") && !requesterRole.equals("PROVIDER")) {
            throw new RuntimeException("Access denied: Only patients and providers can upload files");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + patientId + "/" + fileName;
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        MedicalRecordFile fileEntity = new MedicalRecordFile();
        fileEntity.setMedicalHistoryId(history.getId());
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setFilePath(filePath);
        fileEntity.setUploadedAt(LocalDateTime.now());

        fileEntity = fileRepository.save(fileEntity);
        return mapToFileDto(fileEntity);
    }

    public List<FileDto> getFiles(String patientId, String requesterId, String requesterRole) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        switch (requesterRole) {
            case "PATIENT":
                if (!history.getPatientId().equals(requesterId)) throw new RuntimeException("Access denied");
                break;
            case "PROVIDER":
                if (!history.getProviderIds().contains(requesterId)) throw new RuntimeException("Access denied");
                break;
            case "CAREGIVER":
                if (!history.getCaregiverIds().contains(requesterId)) throw new RuntimeException("Access denied");
                break;
            default:
                throw new RuntimeException("Access denied");
        }

        return history.getFiles().stream().map(this::mapToFileDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteFile(String patientId, Long fileId, String requesterId, String requesterRole) {
        MedicalHistory history = historyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Medical history not found for patient: " + patientId));

        switch (requesterRole) {
            case "PATIENT":
                if (!history.getPatientId().equals(requesterId)) {
                    throw new RuntimeException("Access denied: You can only delete files from your own medical history");
                }
                break;
            case "PROVIDER":
                if (!history.getProviderIds().contains(requesterId)) {
                    throw new RuntimeException("Access denied: Provider not assigned to this patient");
                }
                break;
            default:
                throw new RuntimeException("Access denied: Only patients and providers can delete files");
        }

        MedicalRecordFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        if (!file.getMedicalHistoryId().equals(history.getId())) {
            throw new RuntimeException("File does not belong to this medical history");
        }

        deleteFileFromDisk(file.getFilePath());
        fileRepository.delete(file);
    }

    // ------------------- Helper Methods -------------------

    private List<String> resolveCaregiverNamesToIds(List<String> caregiverNames) {
        if (caregiverNames == null || caregiverNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> ids = new ArrayList<>();
        for (String name : caregiverNames) {
            if (name == null || name.trim().isEmpty()) continue;
            try {
                UserDto user = userServiceClient.getUserByUsername(name);
                if (!"CAREGIVER".equals(user.getRole())) {
                    log.warn("User {} exists but is not a caregiver, role is: {}", name, user.getRole());
                    continue;
                }
                ids.add(user.getId());
            } catch (Exception e) {
                log.warn("Could not resolve caregiver name '{}': {}", name, e.getMessage());
            }
        }
        return ids;
    }

    private MedicalHistory mapRequestToEntity(MedicalHistoryRequest req) {
        MedicalHistory history = new MedicalHistory();
        history.setPatientId(req.getPatientId());
        updateEntityFromRequest(history, req);
        return history;
    }

    private void updateEntityFromRequest(MedicalHistory history, MedicalHistoryRequest req) {
        history.setDiagnosis(req.getDiagnosis());
        history.setDiagnosisDate(req.getDiagnosisDate());
        history.setProgressionStage(req.getProgressionStage());
        history.setGeneticRisk(req.getGeneticRisk());
        history.setFamilyHistory(req.getFamilyHistory());
        history.setEnvironmentalFactors(req.getEnvironmentalFactors());
        history.setComorbidities(req.getComorbidities());
        history.setMedicationAllergies(req.getMedicationAllergies());
        history.setEnvironmentalAllergies(req.getEnvironmentalAllergies());
        history.setFoodAllergies(req.getFoodAllergies());
        history.setSurgeries(req.getSurgeries() != null ? req.getSurgeries() : new ArrayList<>());
    }

    private MedicalHistoryResponse mapToResponse(MedicalHistory history) {
        MedicalHistoryResponse resp = new MedicalHistoryResponse();
        resp.setId(history.getId());
        resp.setPatientId(history.getPatientId());

        try {
            UserDto patient = userServiceClient.getUserById(history.getPatientId());
            resp.setPatientName(patient.getFirstName() + " " + patient.getLastName());
        } catch (Exception e) {
            resp.setPatientName("Unknown");
            log.error("Failed to fetch patient name for id: {}", history.getPatientId(), e);
        }

        List<String> providerNames = new ArrayList<>();
        for (String providerId : history.getProviderIds()) {
            try {
                UserDto provider = userServiceClient.getUserById(providerId);
                providerNames.add(provider.getFirstName() + " " + provider.getLastName());
            } catch (Exception e) {
                providerNames.add("Unknown");
                log.error("Failed to fetch provider name for id: {}", providerId, e);
            }
        }
        resp.setProviderNames(providerNames);

        List<String> caregiverNames = new ArrayList<>();
        for (String caregiverId : history.getCaregiverIds()) {
            try {
                UserDto caregiver = userServiceClient.getUserById(caregiverId);
                caregiverNames.add(caregiver.getFirstName() + " " + caregiver.getLastName());
            } catch (Exception e) {
                caregiverNames.add("Unknown");
                log.error("Failed to fetch caregiver name for id: {}", caregiverId, e);
            }
        }
        resp.setCaregiverNames(caregiverNames);

        resp.setDiagnosis(history.getDiagnosis());
        resp.setDiagnosisDate(history.getDiagnosisDate());
        resp.setProgressionStage(history.getProgressionStage());
        resp.setGeneticRisk(history.getGeneticRisk());
        resp.setFamilyHistory(history.getFamilyHistory());
        resp.setEnvironmentalFactors(history.getEnvironmentalFactors());
        resp.setComorbidities(history.getComorbidities());
        resp.setMedicationAllergies(history.getMedicationAllergies());
        resp.setEnvironmentalAllergies(history.getEnvironmentalAllergies());
        resp.setFoodAllergies(history.getFoodAllergies());
        resp.setSurgeries(history.getSurgeries());
        resp.setProviderIds(history.getProviderIds());
        resp.setCaregiverIds(history.getCaregiverIds());
        resp.setFiles(history.getFiles().stream().map(this::mapToFileDto).collect(Collectors.toList()));
        resp.setCreatedAt(history.getCreatedAt());
        resp.setUpdatedAt(history.getUpdatedAt());

        return resp;
    }

    private FileDto mapToFileDto(MedicalRecordFile file) {
        FileDto dto = new FileDto();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFileType(file.getFileType());
        dto.setFileUrl("/files/" + file.getId());
        dto.setUploadedAt(file.getUploadedAt());
        return dto;
    }

    private void deleteFileFromDisk(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }
}