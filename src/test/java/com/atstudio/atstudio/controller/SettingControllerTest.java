package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.SiteSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("Setting controller contract tests")
class SettingControllerTest {

    private static final String KEY = "COMPANY_CERT_GUIDE";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SiteSettingService siteSettingService;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Public setting GET returns the canonical key and value")
    void getSetting_returnsCanonicalValue() throws Exception {
        given(siteSettingService.getValue(KEY, "")).willReturn("Canonical guide");

        mockMvc.perform(get("/api/settings/{key}", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.key").value(KEY))
                .andExpect(jsonPath("$.data.value").value("Canonical guide"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN setting PUT persists the submitted value")
    void updateSetting_adminRole_updatesValue() throws Exception {
        mockMvc.perform(put("/api/admin/settings/{key}", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"Published guide\"}"))
                .andExpect(status().isOk());

        verify(siteSettingService).setValue(KEY, "Published guide");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER setting PUT remains forbidden")
    void updateSetting_userRole_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/settings/{key}", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"Rejected guide\"}"))
                .andExpect(status().isForbidden());

        verify(siteSettingService, never()).setValue(KEY, "Rejected guide");
    }
}
