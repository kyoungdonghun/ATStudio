package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.admin.DashboardStatsResponse;
import com.atstudio.atstudio.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminStatsService.getDashboardStats();
        return ResponseEntity.ok(ResponseDTO.<DashboardStatsResponse>withSingleData()
                .message("Dashboard stats retrieved")
                .data(stats)
                .build());
    }
}
