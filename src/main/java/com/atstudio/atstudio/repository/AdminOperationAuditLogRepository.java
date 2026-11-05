package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.AdminOperationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOperationAuditLogRepository extends JpaRepository<AdminOperationAuditLog, Long> {
    @org.springframework.data.jpa.repository.QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.query.timeout", value = "3000"))
    @org.springframework.data.jpa.repository.Query("select a from AdminOperationAuditLog a where "
            + "a.action = com.atstudio.atstudio.entity.enums.AdminOperationAuditAction.LISTENING_CATALOG_REPLACE "
            + "and a.targetType = com.atstudio.atstudio.entity.enums.AdminOperationAuditTargetType.LISTENING_CATALOG "
            + "and a.targetId = :settingId and a.afterState like :pattern order by a.id desc")
    java.util.List<AdminOperationAuditLog> findCatalogAttempts(
            @org.springframework.data.repository.query.Param("settingId") Long settingId,
            @org.springframework.data.repository.query.Param("pattern") String pattern,
            org.springframework.data.domain.Pageable page);
}

