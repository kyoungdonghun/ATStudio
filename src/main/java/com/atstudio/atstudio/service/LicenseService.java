package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.license.LicenseListItemResponse;
import com.atstudio.atstudio.dto.license.LicenseResponse;
import com.atstudio.atstudio.entity.License;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.LicenseRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;

    public ResponseDTO<LicenseListItemResponse> getMyLicenses(CustomUserDetails userDetails, int page, int size) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return buildLicensePage(licenseRepository.findAllByUser(user, toPageable(page, size)), page, size);
    }

    public ResponseDTO<LicenseListItemResponse> getUserLicenses(Long userId, int page, int size) {
        return buildLicensePage(licenseRepository.findAllByUser_Id(userId, toPageable(page, size)), page, size);
    }

    public LicenseResponse getMyLicense(Long licenseId, CustomUserDetails userDetails) {
        License license = licenseRepository.findByIdAndUser_Id(licenseId, userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return LicenseResponse.from(license);
    }

    public LicenseResponse getUserLicense(Long userId, Long licenseId) {
        License license = licenseRepository.findByIdAndUser_Id(licenseId, userId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return LicenseResponse.from(license);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Pageable toPageable(int page, int size) {
        return PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private ResponseDTO<LicenseListItemResponse> buildLicensePage(Page<License> licensePage, int page, int size) {
        List<LicenseListItemResponse> dataList = licensePage.getContent().stream()
                .map(LicenseListItemResponse::from)
                .toList();
        int total = (int) licensePage.getTotalElements();
        return ResponseDTO.<LicenseListItemResponse>builder()
                .message("Licenses retrieved")
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }
}
