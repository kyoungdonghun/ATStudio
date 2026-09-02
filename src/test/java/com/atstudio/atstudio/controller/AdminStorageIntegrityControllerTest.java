package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.storage.StorageIntegrityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("AdminStorageIntegrityController")
class AdminStorageIntegrityControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean StorageIntegrityService storageIntegrityService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void inspectionRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/storage-integrity"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void inspectionRejectsNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/admin/storage-integrity"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inspectionExposesAggregateAndOpaqueIssueEvidenceOnly() throws Exception {
        given(storageIntegrityService.inspect()).willReturn(
                new StorageIntegrityReportResponse(Instant.now(), 3, 2, 1, false, List.of()));

        mockMvc.perform(get("/api/admin/storage-integrity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkedReferenceCount").value(3))
                .andExpect(jsonPath("$.data.availableReferenceCount").value(2))
                .andExpect(jsonPath("$.data.missingReferenceCount").value(1))
                .andExpect(jsonPath("$.data.storageKey").doesNotExist())
                .andExpect(jsonPath("$.data.originalFilename").doesNotExist());
    }
}
