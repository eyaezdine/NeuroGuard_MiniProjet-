package com.neuroguard.medicalhistoryservice.controller;

import com.neuroguard.medicalhistoryservice.client.UserServiceClient;
import com.neuroguard.medicalhistoryservice.dto.MedicalHistoryResponse;
import com.neuroguard.medicalhistoryservice.dto.UserDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

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
@DisplayName("Caregiver Controller Tests")
@org.springframework.test.context.ActiveProfiles("test")
class CaregiverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalHistoryService historyService;

    @MockBean
    private UserServiceClient userServiceClient;

    private MedicalHistoryResponse testResponse;
    private UserDto testPatient;
    private UserDto testCaregiver;

    @BeforeEach
    void setUp() {
        testPatient = new UserDto();
        testPatient.setId(1L);
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setEmail("john@example.com");
        testPatient.setRole("PATIENT");
        testPatient.setUsername("johndoe");

        testCaregiver = new UserDto();
        testCaregiver.setId(3L);
        testCaregiver.setFirstName("Jane");
        testCaregiver.setLastName("Care");
        testCaregiver.setRole("CAREGIVER");

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
    }

    @Test
    @DisplayName("Should get medical history for assigned patient")
    void testGetHistory_Success() throws Exception {
        when(historyService.getMedicalHistoryByPatientId(1L, 3L, "CAREGIVER"))
                .thenReturn(testResponse);

        mockMvc.perform(get("/api/caregiver/medical-history/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1L))
                .andExpect(jsonPath("$.diagnosis").value("Alzheimer's Disease"));

        verify(historyService, times(1)).getMedicalHistoryByPatientId(1L, 3L, "CAREGIVER");
    }

    @Test
    @DisplayName("Should throw exception when accessing unauthorized patient")
    void testGetHistory_AccessDenied() throws Exception {
        when(historyService.getMedicalHistoryByPatientId(1L, 3L, "CAREGIVER"))
                .thenThrow(new RuntimeException("Access denied: Caregiver not assigned to this patient"));

        mockMvc.perform(get("/api/caregiver/medical-history/1")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isForbidden());  // 403: Access Forbidden

        verify(historyService, times(1)).getMedicalHistoryByPatientId(1L, 3L, "CAREGIVER");
    }

    @Test
    @DisplayName("Should get all assigned patients for caregiver")
    void testGetAssignedPatients_Success() throws Exception {
        Page<MedicalHistoryResponse> page = new PageImpl<>(Arrays.asList(testResponse));

        when(historyService.getAllMedicalHistoriesForCaregiver(eq(3L), any(Pageable.class)))
                .thenReturn(page);
        when(userServiceClient.getUserById(1L)).thenReturn(testPatient);

        mockMvc.perform(get("/api/caregiver/medical-history/patients")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].role").value("PATIENT"));

        verify(historyService, times(1)).getAllMedicalHistoriesForCaregiver(eq(3L), any());
        verify(userServiceClient, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("Should return empty list when caregiver has no assigned patients")
    void testGetAssignedPatients_EmptyList() throws Exception {
        Page<MedicalHistoryResponse> emptyPage = new PageImpl<>(new ArrayList<>());

        when(historyService.getAllMedicalHistoriesForCaregiver(eq(3L), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/caregiver/medical-history/patients")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(historyService, times(1)).getAllMedicalHistoriesForCaregiver(eq(3L), any());
    }

    @Test
    @DisplayName("Should handle user service failure gracefully")
    void testGetAssignedPatients_UserServiceFailure() throws Exception {
        Page<MedicalHistoryResponse> page = new PageImpl<>(Arrays.asList(testResponse));

        when(historyService.getAllMedicalHistoriesForCaregiver(eq(3L), any(Pageable.class)))
                .thenReturn(page);
        when(userServiceClient.getUserById(1L))
                .thenThrow(new RuntimeException("User service unavailable"));

        mockMvc.perform(get("/api/caregiver/medical-history/patients")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].role").value("PATIENT"))
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].username").value("N/A"));

        verify(userServiceClient, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("Should handle multiple assigned patients")
    void testGetAssignedPatients_MultiplePatients() throws Exception {
        MedicalHistoryResponse response2 = new MedicalHistoryResponse();
        response2.setPatientId(2L);
        response2.setPatientName("Jane Patient");

        Page<MedicalHistoryResponse> page = new PageImpl<>(Arrays.asList(testResponse, response2));

        when(historyService.getAllMedicalHistoriesForCaregiver(eq(3L), any(Pageable.class)))
                .thenReturn(page);
        when(userServiceClient.getUserById(1L)).thenReturn(testPatient);

        UserDto patient2 = new UserDto();
        patient2.setId(2L);
        patient2.setFirstName("Jane");
        patient2.setLastName("Patient");
        patient2.setRole("PATIENT");

        when(userServiceClient.getUserById(2L)).thenReturn(patient2);

        mockMvc.perform(get("/api/caregiver/medical-history/patients")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(historyService, times(1)).getAllMedicalHistoriesForCaregiver(eq(3L), any());
        verify(userServiceClient, times(1)).getUserById(1L);
        verify(userServiceClient, times(1)).getUserById(2L);
    }
}
