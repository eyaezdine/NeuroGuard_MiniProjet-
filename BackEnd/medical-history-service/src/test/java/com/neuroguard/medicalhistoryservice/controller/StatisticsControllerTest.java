package com.neuroguard.medicalhistoryservice.controller;

import com.neuroguard.medicalhistoryservice.dto.CaregiverStatisticsDTO;
import com.neuroguard.medicalhistoryservice.dto.PatientStatisticsDTO;
import com.neuroguard.medicalhistoryservice.dto.ProviderStatisticsDTO;
import com.neuroguard.medicalhistoryservice.service.CaregiverStatisticsService;
import com.neuroguard.medicalhistoryservice.service.PatientStatisticsService;
import com.neuroguard.medicalhistoryservice.service.ProviderStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Statistics Controller Tests")
@org.springframework.test.context.ActiveProfiles("test")
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaregiverStatisticsService caregiverStatisticsService;

    @MockBean
    private PatientStatisticsService patientStatisticsService;

    @MockBean
    private ProviderStatisticsService providerStatisticsService;

    private CaregiverStatisticsDTO caregiverStats;
    private PatientStatisticsDTO patientStats;
    private ProviderStatisticsDTO providerStats;

    @BeforeEach
    void setUp() {
        // Initialize caregiver statistics
        caregiverStats = new CaregiverStatisticsDTO();
        caregiverStats.setCaregiverId(3L);
        caregiverStats.setTotalAssignedPatients(5);

        // Initialize patient statistics
        patientStats = new PatientStatisticsDTO();
        patientStats.setPatientId(1L);
        patientStats.setHasMedicalHistory(true);
        patientStats.setTotalAlerts(5);

        // Initialize provider statistics
        providerStats = new ProviderStatisticsDTO();
        providerStats.setProviderId(2L);
        providerStats.setTotalPatients(10);
    }

    @Test
    @DisplayName("Should get caregiver statistics successfully")
    void testGetCaregiverStatistics_Success() throws Exception {
        Long caregiverId = 3L;

        when(caregiverStatisticsService.getCaregiverStatistics(caregiverId))
                .thenReturn(caregiverStats);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", caregiverId)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caregiverId").value(caregiverId))
                .andExpect(jsonPath("$.totalAssignedPatients").value(5));

        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(caregiverId);
    }

    @Test
    @DisplayName("Should return caregiver statistics with zero patients")
    void testGetCaregiverStatistics_ZeroPatients() throws Exception {
        Long caregiverId = 999L;
        caregiverStats.setTotalAssignedPatients(0);

        when(caregiverStatisticsService.getCaregiverStatistics(caregiverId))
                .thenReturn(caregiverStats);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", caregiverId)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssignedPatients").value(0));

        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(caregiverId);
    }

    @Test
    @DisplayName("Should get patient statistics successfully")
    void testGetPatientStatistics_Success() throws Exception {
        Long patientId = 1L;

        when(patientStatisticsService.getPatientStatistics(patientId))
                .thenReturn(patientStats);

        mockMvc.perform(get("/api/statistics/patient/{patientId}", patientId)
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", patientId)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.totalAlerts").value(5));

        verify(patientStatisticsService, times(1)).getPatientStatistics(patientId);
    }

    @Test
    @DisplayName("Should return null for patient with no medical history")
    void testGetPatientStatistics_NoHistory() throws Exception {
        Long patientId = 999L;

        when(patientStatisticsService.getPatientStatistics(patientId))
                .thenReturn(null);

        mockMvc.perform(get("/api/statistics/patient/{patientId}", patientId)
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", patientId)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().is(200));

        verify(patientStatisticsService, times(1)).getPatientStatistics(patientId);
    }

    @Test
    @DisplayName("Should get provider statistics successfully")
    void testGetProviderStatistics_Success() throws Exception {
        Long providerId = 2L;

        when(providerStatisticsService.getProviderStatistics(providerId))
                .thenReturn(providerStats);

        mockMvc.perform(get("/api/statistics/provider/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", providerId)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerId").value(providerId))
                .andExpect(jsonPath("$.totalPatients").value(10));

        verify(providerStatisticsService, times(1)).getProviderStatistics(providerId);
    }

    @Test
    @DisplayName("Should return zero patients for provider with no patients")
    void testGetProviderStatistics_ZeroPatients() throws Exception {
        Long providerId = 999L;
        providerStats.setTotalPatients(0);

        when(providerStatisticsService.getProviderStatistics(providerId))
                .thenReturn(providerStats);

        mockMvc.perform(get("/api/statistics/provider/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", providerId)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(0));

        verify(providerStatisticsService, times(1)).getProviderStatistics(providerId);
    }

    @Test
    @DisplayName("Should get all statistics for logged-in user")
    void testGetMyStatistics_CaregiverRole() throws Exception {
        Long userId = 3L;

        when(caregiverStatisticsService.getCaregiverStatistics(userId))
                .thenReturn(caregiverStats);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", userId)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssignedPatients").value(5));

        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(userId);
    }

    @Test
    @DisplayName("Should get provider statistics for logged-in provider")
    void testGetMyStatistics_ProviderRole() throws Exception {
        Long userId = 2L;

        when(providerStatisticsService.getProviderStatistics(userId))
                .thenReturn(providerStats);

        mockMvc.perform(get("/api/statistics/provider/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", userId)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(10));

        verify(providerStatisticsService, times(1)).getProviderStatistics(userId);
    }

    @Test
    @DisplayName("Should get patient statistics for logged-in patient")
    void testGetMyStatistics_PatientRole() throws Exception {
        Long userId = 1L;

        when(patientStatisticsService.getPatientStatistics(userId))
                .thenReturn(patientStats);

        mockMvc.perform(get("/api/statistics/patient/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", userId)
                .requestAttr("userRole", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").value(5));

        verify(patientStatisticsService, times(1)).getPatientStatistics(userId);
    }

    @Test
    @DisplayName("Should handle statistics for multiple caregivers")
    void testGetCaregiverStatistics_MultipleStats() throws Exception {
        // Test first caregiver
        when(caregiverStatisticsService.getCaregiverStatistics(3L))
                .thenReturn(caregiverStats);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssignedPatients").value(5));

        // Test second caregiver
        CaregiverStatisticsDTO stats2 = new CaregiverStatisticsDTO();
        stats2.setCaregiverId(4L);
        stats2.setTotalAssignedPatients(8);

        when(caregiverStatisticsService.getCaregiverStatistics(4L))
                .thenReturn(stats2);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 4L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssignedPatients").value(8));

        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(3L);
        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(4L);
    }

    @Test
    @DisplayName("Should return badrequest for invalid user id")
    void testGetStatistics_InvalidUserId() throws Exception {
        Long invalidId = -1L;

        when(caregiverStatisticsService.getCaregiverStatistics(invalidId))
                .thenReturn(null);

        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", invalidId)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk());

        verify(caregiverStatisticsService, times(1)).getCaregiverStatistics(invalidId);
    }

    @Test
    @DisplayName("Should calculate statistics accurately with large datasets")
    void testGetProviderStatistics_LargeDataset() throws Exception {
        ProviderStatisticsDTO largeStats = new ProviderStatisticsDTO();
        largeStats.setProviderId(2L);
        largeStats.setTotalPatients(1000);

        when(providerStatisticsService.getProviderStatistics(2L))
                .thenReturn(largeStats);

        mockMvc.perform(get("/api/statistics/provider/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 2L)
                .requestAttr("userRole", "PROVIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(1000));

        verify(providerStatisticsService, times(1)).getProviderStatistics(2L);
    }

    @Test
    @DisplayName("Should handle concurrent statistics requests")
    void testGetStatistics_ConcurrentRequests() throws Exception {
        when(caregiverStatisticsService.getCaregiverStatistics(3L))
                .thenReturn(caregiverStats);

        // First request
        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk());

        // Second request (simulating concurrent access)
        mockMvc.perform(get("/api/statistics/caregiver/me")
                .header("Authorization", "Bearer mock-jwt-token")
                .requestAttr("userId", 3L)
                .requestAttr("userRole", "CAREGIVER"))
                .andExpect(status().isOk());

        verify(caregiverStatisticsService, times(2)).getCaregiverStatistics(3L);
    }
}
