package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.ListeningCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks/admin/listening-catalog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ListeningCatalogController {
    private final ListeningCatalogService catalog;
    private final com.atstudio.atstudio.config.ListeningProperties properties;
    private final com.atstudio.atstudio.security.TrustedClientIdentityResolver identity;

    private void requireSource(jakarta.servlet.http.HttpServletRequest request) throws java.io.IOException {
        if (!com.atstudio.atstudio.security.ListeningAccessFilter.operatorSource(request, properties, identity)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<ListeningCatalogService.View>> get(jakarta.servlet.http.HttpServletRequest request) throws java.io.IOException {
        requireSource(request);
        return response(catalog.get());
    }

    @PutMapping
    public ResponseEntity<ResponseDTO<ListeningCatalogService.View>> replace(
            @RequestBody ListeningCatalogService.Update request,
            @AuthenticationPrincipal CustomUserDetails operator, jakarta.servlet.http.HttpServletRequest servletRequest) throws java.io.IOException {
        requireSource(servletRequest);
        return response(catalog.replace(request, operator.getId()));
    }

    @PostMapping("/reconcile")
    public ResponseEntity<ResponseDTO<ListeningCatalogService.Reconciliation>> reconcile(
            @RequestBody ListeningCatalogService.Reconcile request,
            @AuthenticationPrincipal CustomUserDetails operator,
            jakarta.servlet.http.HttpServletRequest servletRequest) throws java.io.IOException {
        requireSource(servletRequest);
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(ResponseDTO.<ListeningCatalogService.Reconciliation>withSingleData()
                        .data(catalog.reconcile(request, operator.getId())).build());
    }

    private ResponseEntity<ResponseDTO<ListeningCatalogService.View>> response(ListeningCatalogService.View value) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(ResponseDTO.<ListeningCatalogService.View>withSingleData().data(value).build());
    }
}
