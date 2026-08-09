package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditAction;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(
        name = "admin_operation_audit_logs",
        indexes = {
                @Index(
                        name = "idx_admin_operation_audit_logs_actor_created",
                        columnList = "actor_user_id,created_at"),
                @Index(
                        name = "idx_admin_operation_audit_logs_target",
                        columnList = "target_type,target_id"),
                @Index(
                        name = "idx_admin_operation_audit_logs_action_created",
                        columnList = "action,created_at")
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminOperationAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 60)
    private AdminOperationAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false, length = 60)
    private AdminOperationAuditTargetType targetType;

    @Column(name = "target_id", nullable = false, updatable = false)
    private Long targetId;

    // Snapshot identifier only. Deliberately not a user association or foreign key.
    @Column(name = "actor_user_id", updatable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    private AdminOperationAuditOutcome outcome;

    @Column(name = "before_state", updatable = false, columnDefinition = "TEXT")
    private String beforeState;

    @Column(name = "after_state", updatable = false, columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "reason_code", updatable = false, length = 100)
    private String reasonCode;

    @Column(name = "reason_note", updatable = false, length = 500)
    private String reasonNote;
}
