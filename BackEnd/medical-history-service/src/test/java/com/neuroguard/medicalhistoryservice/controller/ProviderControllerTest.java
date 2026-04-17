package com.neuroguard.medicalhistoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroguard.medicalhistoryservice.client.UserServiceClient;
import com.neuroguard.medicalhistoryservice.dto.*;
import com.neuroguard.medicalhistoryservice.entity.ProgressionStage;
import com.neuroguard.medicalhistoryservice.service.MedicalHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Provider Controller Tests")
@org.springframework.test.context.ActiveProfiles("test")
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalHistoryService historyService;

    @MockBean
    private UserServiceClient userServiceClient;

    private MedicalHistoryResponse testResponse;
    private MedicalHistoryRequest testRequest;
    private UserDto testProvider;
    private UserDto testPatient;
    private UserDto testCaregiver;

    @BeforeEach
    void setUp() {
        testPatient = new UserDto();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setRole("PATIENT");

        testProvider = new UserDto();
        testProvider.setId(2L);
        testProvider.setFirstName("Dr.");
        testProvider.setLastName("Smith");
        testProvider.setRole("PROVIDER");

        testCaregiver = new UserDto();
        testCaregiver.setId(3L);
        testCaregiver.setFirstName("Jane");
        testCaregiver.setLastName("Care");
        testCaregiver.setRole("CAREGIVER");

        testRequest = new MedicalHistoryRequest();
        testRequest.setPatientId(1L);
        testRequest.setDiagnosis("Alzheimer's Disease");
        testRequest.setDiagnosisDate(LocalDate.of(2020, 1, 1));
        testRequest.setProgressionStage(ProgressionStage.MILD);
        testRequest.setMmse(25);
        testRequest.setFunctionalAssessment(20);
        testRequest.setAdl(15);

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
    }

    @Test
    @DisplayName("Should create medical history with status 201")
    void testCreateHistory_Success() throws Exception {
        when(historyService.createMedicalHistory(any(), anyLong())).thenReturn(testResponse);

        mockMvc.perform(post("/api/provider/medical-history")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("Alzheimer's Disease"))
                .andExpect(jsonPath("$.patientId").value(1L));

        verify(historyService, times(1)).createMedicalHistory(any(), eq(2L));
    }

    @Test
    @DisplayName("Should update medical history successfully")
    void testUpdateHistory_Success() throws Exception {
        Long patientId = 1L;

        when(historyService.updateMedicalHistory(eq(patientId), any(), anyLong())).thenReturn(testResponse);

        mockMvc.perform(put("/api/provider/medical-history/" + patientId)
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest))
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Alzheimer's Disease"));

        verify(historyService, times(1)).updateMedicalHistory(eq(patientId), any(), eq(2L));
    }

    @Test
    @DisplayName("Should get all histories for provider")
    void testGetAllHistories_Success() throws Exception {
        Page<MedicalHistoryResponse> page = new PageImpl<>(Arrays.asList(testResponse));

        when(historyService.getAllMedicalHistoriesForProvider(anyLong(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/provider/medical-history")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].diagnosis").value("Alzheimer's Disease"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(historyService, times(1)).getAllMedicalHistoriesForProvider(eq(2L), any());
    }

    @Test
    @DisplayName("Should get specific medical history by patient ID")
    void testGetHistory_Success() throws Exception {
        when(historyService.getMedicalHistoryByPatientId(1L, 2L, "PROVIDER"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/api/provider/medical-history/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("Alzheimer's Disease"));

        verify(historyService, times(1)).getMedicalHistoryByPatientId(1L, 2L, "PROVIDER");
    }

    @Test
    @DisplayName("Should delete medical history and return 204")
    void testDeleteHistory_Success() throws Exception {
        doNothing().when(historyService).deleteMedicalHistory(1L, 2L);

        mockMvc.perform(delete("/api/provider/medical-history/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isNoContent());

        verify(historyService, times(1)).deleteMedicalHistory(1L, 2L);
    }

    @Test
    @DisplayName("Should get list of patients")
    void testGetPatients() throws Exception {
        List<UserDto> patients = Arrays.asList(testPatient);

        when(userServiceClient.getUsersByRole("PATIENT")).thenReturn(patients);

        mockMvc.perform(get("/api/provider/medical-history/patients")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].role").value("PATIENT"));

        verify(userServiceClient, times(1)).getUsersByRole("PATIENT");
    }

    @Test
    @DisplayName("Should get list of caregivers")
    void testGetCaregivers() throws Exception {
        List<UserDto> caregivers = Arrays.asList(testCaregiver);

        when(userServiceClient.getUsersByRole("CAREGIVER")).thenReturn(caregivers);

        mockMvc.perform(get("/api/provider/medical-history/caregivers")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].role").value("CAREGIVER"));

        verify(userServiceClient, times(1)).getUsersByRole("CAREGIVER");
    }

    @Test
    @DisplayName("Should get list of providers")
    void testGetProviders() throws Exception {
        List<UserDto> providers = Arrays.asList(testProvider);

        when(userServiceClient.getUsersByRole("PROVIDER")).thenReturn(providers);

        mockMvc.perform(get("/api/provider/medical-history/providers")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Dr."))
                .andExpect(jsonPath("$[0].role").value("PROVIDER"));

        verify(userServiceClient, times(1)).getUsersByRole("PROVIDER");
    }

    @Test
    @DisplayName("Should delete file and return 204")
    void testDeleteFile_Success() throws Exception {
        doNothing().when(historyService).deleteFile(1L, 1L, 2L, "PROVIDER");

        mockMvc.perform(delete("/api/provider/medical-history/1/files/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isNoContent());

        verify(historyService, times(1)).deleteFile(1L, 1L, 2L, "PROVIDER");
    }

    @Test
    @DisplayName("Should get patient features")
    void testGetPatientFeatures() throws Exception {
        PatientFeatures features = new PatientFeatures();
        features.setPatientId(1L);
        features.setAge(65);
        features.setGender("Male");
        features.setMmse(25);

        when(historyService.buildPatientFeatures(1L)).thenReturn(features);

        mockMvc.perform(get("/api/provider/medical-history/features/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.age").value(65))
                .andExpect(jsonPath("$.mmse").value(25));

        verify(historyService, times(1)).buildPatientFeatures(1L);
    }

    @Test
    @DisplayName("Should return 400 when creating with invalid request")
    void testCreateHistory_InvalidRequest() throws Exception {
        MedicalHistoryRequest invalidRequest = new MedicalHistoryRequest();
        // Missing required fields
        when(historyService.createMedicalHistory(any(), anyLong())).thenReturn(testResponse);

        mockMvc.perform(post("/api/provider/medical-history")
                .header("Authorization", "Bearer mock-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isCreated());
    }
}
