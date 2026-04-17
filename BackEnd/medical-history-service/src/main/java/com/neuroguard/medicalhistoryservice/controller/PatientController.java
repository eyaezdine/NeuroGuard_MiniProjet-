package com.neuroguard.medicalhistoryservice.controller;

import com.neuroguard.medicalhistoryservice.dto.FileDto;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryRequest;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryResponse;
import com.neuroguard.medicalhistoryservice.service.MedicalHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patient/medical-history")
@RequiredArgsConstructor
public class PatientController {

    private final MedicalHistoryService historyService;

    // NOTE: Each patient has ONE medical history record
    // So these endpoints manage that single record

    @PostMapping
    public ResponseEntity<MedicalHistoryResponse> createHistory(@RequestBody MedicalHistoryRequest request,
                                                                HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        // Override patientId in request to ensure patient can only create for themselves
        request.setPatientId(patientId);
        MedicalHistoryResponse response = historyService.createMedicalHistory(request, patientId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<MedicalHistoryResponse> getHistory(HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("userRole");
        MedicalHistoryResponse response = historyService.getMedicalHistoryByPatientId(patientId, patientId, role);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<MedicalHistoryResponse> updateHistory(@RequestBody MedicalHistoryRequest request,
                                                                HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        // Use patientId to find the record to update
        MedicalHistoryResponse response = historyService.updateMedicalHistory(patientId, request, patientId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteHistory(HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        historyService.deleteMedicalHistory(patientId, patientId);
        return ResponseEntity.noContent().build();
    }

    // File Operations

    @PostMapping("/files")
    public ResponseEntity<FileDto> uploadFile(@RequestParam("file") MultipartFile file,
                                              HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("userRole");
        FileDto fileDto = historyService.uploadFile(patientId, file, patientId, role);
        return ResponseEntity.ok(fileDto);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileDto>> getFiles(HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("userRole");
        List<FileDto> files = historyService.getFiles(patientId, patientId, role);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId,
                                           HttpServletRequest httpRequest) {
        String patientId = (String) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("userRole");
        historyService.deleteFile(patientId, fileId, patientId, role);
        return ResponseEntity.noContent().build();
    }
}