package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.AdminOperationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOperationAuditLogRepository extends JpaRepository<AdminOperationAuditLog, Long> {
}
