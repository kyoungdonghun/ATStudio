package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationState;
import com.atstudio.atstudio.service.storage.StorageMutationType;
import com.atstudio.atstudio.service.storage.StorageRoot;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class StorageMutationContractTest {

    private static final Path FRESH_SCHEMA = Path.of("src/main/resources/schema.sql");

    @Test
    void entityAndEnumsMatchApprovedJournalContract() {
        assertThat(StorageDomain.values()).containsExactly(
                StorageDomain.TRACK,
                StorageDomain.PLAYLIST,
                StorageDomain.ALBUM,
                StorageDomain.COMPANY_CERTIFICATION,
                StorageDomain.NOTICE,
                StorageDomain.QUESTION);
        assertThat(StorageMutationType.values()).containsExactly(
                StorageMutationType.CREATE,
                StorageMutationType.REPLACE,
                StorageMutationType.DELETE);
        assertThat(StorageRoot.values()).containsExactly(StorageRoot.PUBLIC, StorageRoot.PRIVATE);
        assertThat(StorageMutationState.values()).containsExactly(
                StorageMutationState.PREPARED,
                StorageMutationState.COMMITTED,
                StorageMutationState.ROLLBACK_CLEANUP,
                StorageMutationState.AFTER_COMMIT_DELETE,
                StorageMutationState.RETRY,
                StorageMutationState.DONE,
                StorageMutationState.FAILED);

        Table table = StorageMutation.class.getAnnotation(Table.class);
        Map<String, Index> indexes = Arrays.stream(table.indexes())
                .collect(Collectors.toMap(Index::name, Function.identity()));
        assertThat(indexes.get("idx_storage_mutations_recovery").columnList())
                .isEqualTo("state,next_attempt_at,id");
        assertThat(indexes.get("idx_storage_mutations_operation_id").columnList())
                .isEqualTo("operation_id");
        assertThat(Arrays.stream(StorageMutation.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName))
                .doesNotContain("originalFilename", "content", "exception", "pii");
    }

    @Test
    void freshSchemaDefinesJournalWithoutLosingWi004PaymentContract() throws IOException {
        String schema = Files.readString(FRESH_SCHEMA);
        String journal = normalizeSql(tableDefinition(schema, "storage_mutations"));

        assertThat(journal).contains(
                "operation_id CHAR(36) NOT NULL",
                "mutation_type ENUM ('CREATE', 'REPLACE', 'DELETE') NOT NULL",
                "storage_root ENUM ('PUBLIC', 'PRIVATE') NOT NULL",
                "state ENUM ('PREPARED', 'COMMITTED', 'ROLLBACK_CLEANUP', 'AFTER_COMMIT_DELETE', 'RETRY', 'DONE', 'FAILED') NOT NULL",
                "KEY idx_storage_mutations_recovery (state, next_attempt_at, id)",
                "KEY idx_storage_mutations_operation_id (operation_id)",
                "CONSTRAINT chk_storage_mutations_keys CHECK");
        assertThat(schema).contains(
                "uq_payment_orders_command_key",
                "uq_subscription_payments_provider_transaction",
                "payment_operation_audit_logs");
    }

    private static String tableDefinition(String sql, String tableName) {
        Pattern pattern = Pattern.compile(
                "CREATE TABLE " + Pattern.quote(tableName)
                        + "\\s*\\((.*?)\\) ENGINE = InnoDB",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        assertThat(matcher.find()).as("table %s exists", tableName).isTrue();
        return matcher.group(1);
    }

    private static String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
