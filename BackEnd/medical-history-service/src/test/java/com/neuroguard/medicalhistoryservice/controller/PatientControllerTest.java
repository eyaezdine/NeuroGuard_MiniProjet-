package com.neuroguard.medicalhistoryservice.controller;

import com.neuroguard.medicalhistoryservice.dto.FileDto;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryResponse;
import com.neuroguard.medicalhistoryservice.entity.ProgressionStage;
import com.neuroguard.medicalhistoryservice.service.MedicalHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Patient Controller Tests")
@org.springframework.test.context.ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalHistoryService historyService;

    private MedicalHistoryResponse testResponse;
    private FileDto testFileDto;

    @BeforeEach
    void setUp() {
        testResponse = new MedicalHistoryResponse();
        testResponse.setId(1L);
        testResponse.setPatientId(1L);
        testResponse.setPatientName("John Doe");
        testResponse.setDiagnosis("Alzheimer's Disease");
        testResponse.setDiagnosisDate(LocalDate.of(2020, 1, 1));
        testResponse.setProgressionStage(ProgressionStage.MILD);
        testResponse.setProviderNames(Arrays.asList("Dr. Smith"));
        testResponse.setCaregiverNames(Arrays.asList("Jane Care"));
        testResponse.setProviderIds(Arrays.asList(2L));
        testResponse.setCaregiverIds(Arrays.asList(3L));
        testResponse.setFiles(new ArrayList<>());
        testResponse.setCreatedAt(LocalDateTime.now());
        testResponse.setUpdatedAt(LocalDateTime.now());
        testResponse.setMmse(25);
        testResponse.setFunctionalAssessment(20);
        testResponse.setAdl(15);

        testFileDto = new FileDto();
        testFileDto.setId(1L);
        testFileDto.setFileName("test.pdf");
        testFileDto.setFileType("application/pdf");
        testFileDto.setFileUrl("https://example.com/files/test.pdf");
        testFileDto.setUploadedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get patient's own medical history")
    void testGetMyHistory_Success() throws Exception {
        when(historyService.getMedicalHistoryByPatientId(1L, 1L, "PATIENT"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/api/patient/medical-history/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("Alzheimer's Disease"))
                .andExpect(jsonPath("$.mmse").value(25));

        verify(historyService, times(1)).getMedicalHistoryByPatientId(1L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should throw exception when accessing non-existent medical history")
    void testGetMyHistory_NotFound() throws Exception {
        when(historyService.getMedicalHistoryByPatientId(1L, 1L, "PATIENT"))
                .thenThrow(new RuntimeException("Medical history not found for patient: 1"));

        mockMvc.perform(get("/api/patient/medical-history/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isNotFound());  // 404: Not Found

        verify(historyService, times(1)).getMedicalHistoryByPatientId(1L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should upload file successfully")
    void testUploadFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test PDF Content".getBytes()
        );

        when(historyService.uploadFile(eq(1L), any(), eq(1L), eq("PATIENT")))
                .thenReturn(testFileDto);

        mockMvc.perform(multipart("/api/patient/medical-history/me/files")
                .file(file)
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"));

        verify(historyService, times(1)).uploadFile(eq(1L), any(), eq(1L), eq("PATIENT"));
    }

    @Test
    @DisplayName("Should throw exception when Azure Blob Storage is not configured")
    void testUploadFile_AzureNotConfigured() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test PDF Content".getBytes()
        );

        when(historyService.uploadFile(eq(1L), any(), eq(1L), eq("PATIENT")))
                .thenThrow(new RuntimeException("Azure Blob Storage is not configured"));

        mockMvc.perform(multipart("/api/patient/medical-history/me/files")
                .file(file)
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isInternalServerError());

        verify(historyService, times(1)).uploadFile(eq(1L), any(), eq(1L), eq("PATIENT"));
    }

    @Test
    @DisplayName("Should get all files for patient's medical history")
    void testGetMyFiles_Success() throws Exception {
        FileDto file2 = new FileDto();
        file2.setId(2L);
        file2.setFileName("scan.jpg");
        file2.setFileType("image/jpeg");

        when(historyService.getFiles(1L, 1L, "PATIENT"))
                .thenReturn(Arrays.asList(testFileDto, file2));

        mockMvc.perform(get("/api/patient/medical-history/me/files")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").value("test.pdf"))
                .andExpect(jsonPath("$[1].fileName").value("scan.jpg"));

        verify(historyService, times(1)).getFiles(1L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should return empty list when patient has no files")
    void testGetMyFiles_EmptyList() throws Exception {
        when(historyService.getFiles(1L, 1L, "PATIENT"))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/patient/medical-history/me/files")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(historyService, times(1)).getFiles(1L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should delete file successfully")
    void testDeleteFile_Success() throws Exception {
        doNothing().when(historyService).deleteFile(1L, 1L, 1L, "PATIENT");

        mockMvc.perform(delete("/api/patient/medical-history/me/files/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isNoContent());

        verify(historyService, times(1)).deleteFile(1L, 1L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent file")
    void testDeleteFile_NotFound() throws Exception {
        doThrow(new RuntimeException("File not found: 999"))
                .when(historyService).deleteFile(1L, 999L, 1L, "PATIENT");

        mockMvc.perform(delete("/api/patient/medical-history/me/files/999")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isNotFound());

        verify(historyService, times(1)).deleteFile(1L, 999L, 1L, "PATIENT");
    }

    @Test
    @DisplayName("Should throw exception when accessing files without permission")
    void testGetMyFiles_AccessDenied() throws Exception {
        when(historyService.getFiles(1L, 1L, "PATIENT"))
                .thenThrow(new RuntimeException("Access denied"));

        mockMvc.perform(get("/api/patient/medical-history/me/files")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should upload multiple files for same patient")
    void testUploadMultipleFiles_Success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test1.pdf",
                "application/pdf",
                "Test PDF Content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test2.pdf",
                "application/pdf",
                "Test PDF Content 2".getBytes()
        );

        FileDto dto1 = new FileDto();
        dto1.setId(1L);
        dto1.setFileName("test1.pdf");

        FileDto dto2 = new FileDto();
        dto2.setId(2L);
        dto2.setFileName("test2.pdf");

        // First upload
        when(historyService.uploadFile(eq(1L), any(), eq(1L), eq("PATIENT")))
                .thenReturn(dto1)
                .thenReturn(dto2);

        mockMvc.perform(multipart("/api/patient/medical-history/me/files")
                .file(file1)
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test1.pdf"));

        // Second upload
        mockMvc.perform(multipart("/api/patient/medical-history/me/files")
                .file(file2)
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test2.pdf"));

        verify(historyService, times(2)).uploadFile(eq(1L), any(), eq(1L), eq("PATIENT"));
    }

    @Test
    @DisplayName("Should handle large file upload")
    void testUploadFile_LargeFile() throws Exception {
        // Create a 5MB file
        byte[] content = new byte[5 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "largefile.pdf",
                "application/pdf",
                content
        );

        when(historyService.uploadFile(eq(1L), any(), eq(1L), eq("PATIENT")))
                .thenReturn(testFileDto);

        mockMvc.perform(multipart("/api/patient/medical-history/me/files")
                .file(file)
                .requestAttr("userId", 1L)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"));

        verify(historyService, times(1)).uploadFile(eq(1L), any(), eq(1L), eq("PATIENT"));
    }
}
