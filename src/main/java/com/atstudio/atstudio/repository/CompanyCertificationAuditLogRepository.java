package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.CompanyCertificationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyCertificationAuditLogRepository
        extends JpaRepository<CompanyCertificationAuditLog, Long> {
}
