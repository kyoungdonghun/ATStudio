package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.AdminOperationAuditLog;
import com.atstudio.atstudio.entity.enums.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class ListeningReceiptTest extends ListeningDatabaseCatalogTest {
    String attempt = "12345678-1234-1234-1234-123456789abc";
    ListeningCatalogService.Update proposal() {
        return new ListeningCatalogService.Update(0, List.of(trackId), "Exact receipt reason", true, attempt);
    }
    ListeningCatalogService.Reconcile request(ListeningCatalogService.View before) {
        return new ListeningCatalogService.Reconcile(attempt, 0, before.trackIds(), before.digest(), List.of(trackId), "Exact receipt reason");
    }

    @Test void receiptExactCommitSurvivesLaterRevisionWithoutReplay() {
        var before = catalog.get(); var query = request(before);
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
        var committed = catalog.replace(proposal(), 7L);
        var receipt = catalog.reconcile(query, 7L);
        assertThat(receipt.status()).isEqualTo("COMMITTED");
        assertThat(receipt.receipt().actorId()).isEqualTo(7L);
        assertThat(receipt.receipt().after()).isEqualTo(committed);
        assertThat(receipt.receipt().before()).isEqualTo(before);
        assertThat(audits.count()).isEqualTo(1);
        catalog.replace(update(1, List.of()), 8L);
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("COMMITTED");
        assertThat(catalog.get().revision()).isEqualTo(2);
        assertThat(audits.count()).isEqualTo(2);
    }

    @Test void receiptWrongActorProposalReasonAndUuidCollisionFailClosed() {
        var before = catalog.get(); var query = request(before); catalog.replace(proposal(), 7L);
        assertThat(catalog.reconcile(query, 8L).status()).isEqualTo("UNKNOWN");
        assertThat(catalog.reconcile(new ListeningCatalogService.Reconcile(attempt, 0, List.of(), before.digest(), List.of(), "Exact receipt reason"), 7L).status()).isEqualTo("UNKNOWN");
        assertThat(catalog.reconcile(new ListeningCatalogService.Reconcile(attempt, 0, List.of(), before.digest(), List.of(trackId), "Other reason"), 7L).status()).isEqualTo("UNKNOWN");
        assertThatThrownBy(() -> catalog.replace(new ListeningCatalogService.Update(1, List.of(), "new", true, attempt), 8L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThat(catalog.get().revision()).isEqualTo(1); assertThat(audits.count()).isEqualTo(1);
    }

    @Test void receiptMissingAttemptCannotCreateAnUnrecoverableWrite() {
        assertThatThrownBy(() -> catalog.replace(new ListeningCatalogService.Update(0, List.of(trackId), "reason", true, null), 7L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThat(audits.count()).isZero(); assertThat(catalog.get().revision()).isZero();
    }

    @Test void receiptDuplicateOrTamperedAuditNeverConfirms() {
        var before = catalog.get(); var query = request(before); catalog.replace(proposal(), 7L);
        var original = audits.findAll().get(0);
        tx.executeWithoutResult(s -> audits.saveAndFlush(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.LISTENING_CATALOG_REPLACE).targetType(AdminOperationAuditTargetType.LISTENING_CATALOG)
                .targetId(original.getTargetId()).actorUserId(7L).outcome(AdminOperationAuditOutcome.SUCCEEDED)
                .reasonCode("EXPLICIT_CATALOG_APPROVAL").reasonNote(original.getReasonNote())
                .beforeState(original.getBeforeState()).afterState(original.getAfterState().replace(":", " : ")).build()));
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
        tx.executeWithoutResult(s -> { audits.deleteAllInBatch(); audits.saveAndFlush(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.LISTENING_CATALOG_REPLACE).targetType(AdminOperationAuditTargetType.LISTENING_CATALOG)
                .targetId(original.getTargetId()).actorUserId(7L).outcome(AdminOperationAuditOutcome.SUCCEEDED)
                .reasonCode("EXPLICIT_CATALOG_APPROVAL").reasonNote(original.getReasonNote())
                .beforeState(original.getBeforeState()).afterState("{\"attemptId\":\"" + attempt + "\",\"catalog\":{}} ").build()); });
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
    }

    @Test void receiptRolledBackOrSameRevisionChangedStateRemainsUnknown() {
        var before = catalog.get(); var query = request(before);
        assertThatThrownBy(() -> writer.replaceThenFail(proposal())).isInstanceOf(IllegalStateException.class);
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
        catalog.replace(proposal(), 7L);
        tx.executeWithoutResult(s -> settings.findBySettingKey(ReservedSettingPolicy.CATALOG_KEY).orElseThrow()
                .updateValue("{\"revision\":1,\"trackIds\":[]}"));
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
        tx.executeWithoutResult(s -> settings.findBySettingKey(ReservedSettingPolicy.CATALOG_KEY).orElseThrow()
                .updateValue("{\"revision\":0,\"trackIds\":[]}"));
        assertThat(catalog.reconcile(query, 7L).status()).isEqualTo("UNKNOWN");
    }
}
