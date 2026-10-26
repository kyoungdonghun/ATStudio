import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** WI063 in-memory JDBC contract checks, not MySQL locking/concurrency evidence. */
public final class RuntimeFeatureProvisionerHarness {
    private static int tests;
    private RuntimeFeatureProvisionerHarness() {}

    static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("HARNESS_CONTRACT");
        }
    }

    static Object proxy(Class<?> type, java.lang.reflect.InvocationHandler handler) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    static ResultSet rows(List<List<String>> values) {
        int[] row = {-1};
        return (ResultSet) proxy(ResultSet.class, (object, method, args) -> switch (method.getName()) {
            case "next" -> ++row[0] < values.size();
            case "getString" -> values.get(row[0]).get((int) args[0] - 1);
            case "getLong" -> Long.parseLong(values.get(row[0]).get((int) args[0] - 1));
            case "close" -> null;
            default -> throw new AssertionError("UNEXPECTED_RESULT_CALL");
        });
    }

    static ResultSet scalar(String value) {
        return rows(List.of(List.of(value)));
    }

    static final class Fake {
        boolean readOnly;
        boolean autoCommit = true;
        int isolation;
        int updates;
        int audits;
        int commits;
        int rollbacks;
        int updatedRows = 1;
        int auditRows = 1;
        long generated = 9;
        boolean failCommit;
        boolean failRollback;
        boolean failAudit;
        boolean pendingUser;
        boolean pendingAudit;
        boolean committedUser;
        boolean committedAudit;
        int auditMatch = 1;
        boolean metadataDrift;
        int metadataRows = 1;
        String targetRole = "USER";
        String deleted = "0";
        String verified = "1";
        String loggedOut = "1";
        int targetRows = 1;
        List<List<String>> admins = List.of();
        final List<String> queries = new ArrayList<>();
        final Map<String, String> overrides = new HashMap<>();
        final Connection connection = (Connection) proxy(Connection.class, (object, method, args) -> {
            return switch (method.getName()) {
                case "setReadOnly" -> { readOnly = (boolean) args[0]; yield null; }
                case "isReadOnly" -> readOnly;
                case "setAutoCommit" -> { autoCommit = (boolean) args[0]; yield null; }
                case "getAutoCommit" -> autoCommit;
                case "setTransactionIsolation" -> { isolation = (int) args[0]; yield null; }
                case "getTransactionIsolation" -> isolation;
                case "prepareStatement" -> statement((String) args[0]);
                case "commit" -> {
                    commits++;
                    committedUser = pendingUser;
                    committedAudit = pendingAudit;
                    if (failCommit) { throw new SQLException("PRIVATE_COMMIT_DIAGNOSTIC"); }
                    yield null;
                }
                case "rollback" -> {
                    rollbacks++;
                    pendingUser = false;
                    pendingAudit = false;
                    if (failRollback) { throw new SQLException("PRIVATE_ROLLBACK_DIAGNOSTIC"); }
                    yield null;
                }
                default -> throw new AssertionError("UNEXPECTED_CONNECTION_CALL");
            };
        });

        PreparedStatement statement(String sql) {
            queries.add(sql);
            Map<Integer, Object> values = new HashMap<>();
            return (PreparedStatement) proxy(PreparedStatement.class, (object, method, args) -> {
                return switch (method.getName()) {
                    case "setQueryTimeout" -> { check((int) args[0] == 3); yield null; }
                    case "setMaxRows" -> { check((int) args[0] == 257); yield null; }
                    case "setObject" -> { values.put((int) args[0], args[1]); yield null; }
                    case "executeQuery" -> query(sql, values);
                    case "executeUpdate" -> {
                        check(!readOnly && !autoCommit && isolation == Connection.TRANSACTION_SERIALIZABLE);
                        if (sql.equals(RuntimeFeatureProvisioner.UPDATE)) {
                            check(values.equals(Map.of(1, 7L, 2, "f".repeat(64))));
                            updates++;
                            pendingUser = true;
                            yield updatedRows;
                        }
                        check(sql.equals(RuntimeFeatureProvisioner.AUDIT));
                        check(values.equals(Map.of(1, 7L, 2, RuntimeFeatureProvisioner.BEFORE,
                                3, RuntimeFeatureProvisioner.AFTER, 4, "FIRST_ADMIN_BOOTSTRAP:" + "a".repeat(32))));
                        audits++;
                        if (failAudit) { throw new SQLException("PRIVATE_AUDIT_DIAGNOSTIC"); }
                        pendingAudit = true;
                        yield auditRows;
                    }
                    case "execute" -> { check(sql.equals("SET SESSION innodb_lock_wait_timeout=3")); yield false; }
                    case "getGeneratedKeys" -> scalar(Long.toString(generated));
                    case "close" -> null;
                    default -> throw new AssertionError("UNEXPECTED_STATEMENT_CALL");
                };
            });
        }

        ResultSet query(String sql, Map<Integer, Object> values) throws SQLException {
            if (overrides.containsKey(sql)) {
                if (overrides.get(sql).equals("THROW")) {
                    throw new SQLException("PRIVATE_QUERY_DIAGNOSTIC");
                }
                return scalar(overrides.get(sql));
            }
            int metadata = RuntimeFeatureProvisioner.METADATA.indexOf(sql);
            if (metadata >= 0) {
                List<List<String>> result = new ArrayList<>();
                for (int i = 0; i < metadataRows; i++) {
                    result.add(metadata == 0 ? List.of(metadataDrift ? "changed" : "users", "InnoDB", "utf8mb4_unicode_ci")
                            : Arrays.asList("users", "id", "bigint", "NO", null, "auto_increment", null));
                }
                return rows(result);
            }
            if (sql.equals(RuntimeFeatureProvisioner.ADMIN_LOCK)
                    || sql.equals("SELECT id FROM users WHERE role='ADMIN' ORDER BY id")) {
                return rows(admins);
            }
            if (sql.startsWith(RuntimeFeatureProvisioner.TARGET)) {
                check(values.equals(Map.of(1, 7L, 2, "f".repeat(64))));
                List<List<String>> result = new ArrayList<>();
                for (int i = 0; i < targetRows; i++) {
                    result.add(List.of(targetRole, deleted, verified, loggedOut));
                }
                return rows(result);
            }
            if (sql.equals(RuntimeFeatureProvisioner.AUDIT_MATCH)) {
                check(values.get(1).equals(9L) && values.get(2).equals(7L)
                        && values.get(3).equals(RuntimeFeatureProvisioner.BEFORE)
                        && values.get(4).equals(RuntimeFeatureProvisioner.AFTER)
                        && values.get(5).equals("FIRST_ADMIN_BOOTSTRAP:" + "a".repeat(32)));
                return scalar(Integer.toString(auditMatch));
            }
            if (sql.startsWith("SELECT COUNT(*) FROM information_schema.TABLES")) { return scalar("2"); }
            if (sql.startsWith("SELECT COUNT(*) FROM information_schema.TRIGGERS")) { return scalar("0"); }
            return switch (sql) {
                case "SELECT CURRENT_USER()" -> scalar("atstudio_app@127.0.0.1");
                case "SELECT DATABASE()" -> scalar("atstudio_prod");
                case "SELECT SHA2(@@server_uuid,256)" -> scalar("d".repeat(64));
                case "SELECT @@session.transaction_isolation" -> scalar("SERIALIZABLE");
                case "SELECT @@session.transaction_read_only" -> scalar(readOnly ? "1" : "0");
                case "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306" -> scalar("1");
                default -> throw new AssertionError("UNEXPECTED_SQL");
            };
        }

        RuntimeFeatureProvisioner.Input input(long auditId) throws Exception {
            return new RuntimeFeatureProvisioner.Input("X".repeat(32), "d".repeat(64),
                    RuntimeFeatureProvisioner.metadataHash(connection), 7L, "f".repeat(64),
                    "a".repeat(32), auditId);
        }
    }

    static void scenario(String expected, String mode, long auditId,
                         java.util.function.Consumer<Fake> configure) throws Exception {
        Fake fake = new Fake();
        RuntimeFeatureProvisioner.Input input = fake.input(auditId);
        fake.queries.clear();
        configure.accept(fake);
        RuntimeFeatureProvisioner.Outcome result = RuntimeFeatureProvisioner.transact(fake.connection, input, mode);
        check(expected.equals(result.status()));
        if (expected.equals("APPLIED")) {
            check(fake.updates == 1 && fake.audits == 1 && fake.commits == 1 && fake.rollbacks == 0);
            check(fake.committedUser && fake.committedAudit && result.auditId() == 9);
            check(fake.queries.indexOf(RuntimeFeatureProvisioner.ADMIN_LOCK)
                    < fake.queries.indexOf(RuntimeFeatureProvisioner.TARGET + " FOR UPDATE"));
            check(fake.queries.indexOf(RuntimeFeatureProvisioner.TARGET + " FOR UPDATE")
                    < fake.queries.indexOf(RuntimeFeatureProvisioner.UPDATE));
        } else if (expected.equals("ALREADY_APPLIED") || expected.equals("READY")) {
            check(fake.updates == 0 && fake.audits == 0 && fake.commits == 0 && fake.rollbacks == 1);
        } else {
            check(fake.rollbacks == 1 && !fake.pendingUser && !fake.pendingAudit && result.auditId() == 0);
            if (!expected.equals("HOLD_UNKNOWN")) {
                check(!fake.committedUser && !fake.committedAudit && fake.commits == 0);
            }
        }
        tests++;
    }

    static byte[] frame(String[] fields) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(bytes)) {
            data.writeInt(0x41465031);
            for (String field : fields) {
                byte[] raw = field.getBytes(StandardCharsets.US_ASCII);
                data.writeInt(raw.length);
                data.write(raw);
            }
        }
        return bytes.toByteArray();
    }

    static void framing() throws Exception {
        String[] fields = {"X".repeat(32), "d".repeat(64), "e".repeat(64), "7",
                "f".repeat(64), "a".repeat(32), "0"};
        byte[] valid = frame(fields);
        check(RuntimeFeatureProvisioner.readInput(new DataInputStream(new ByteArrayInputStream(valid))).userId() == 7);
        tests++;
        List<byte[]> invalid = new ArrayList<>();
        invalid.add(Arrays.copyOf(valid, valid.length - 1));
        invalid.add(Arrays.copyOf(valid, valid.length + 1));
        byte[] magic = valid.clone();
        magic[0] = 0;
        invalid.add(magic);
        byte[] length = valid.clone();
        Arrays.fill(length, 4, 8, (byte) 255);
        invalid.add(length);
        for (int index = 0; index < fields.length; index++) {
            String[] changed = fields.clone();
            changed[index] = index == 0 ? "\n".repeat(32) : "bad";
            invalid.add(frame(changed));
        }
        String[] overflow = fields.clone();
        overflow[3] = "9223372036854775808";
        invalid.add(frame(overflow));
        for (byte[] raw : invalid) {
            boolean rejected = false;
            try {
                RuntimeFeatureProvisioner.readInput(new DataInputStream(new ByteArrayInputStream(raw)));
            } catch (Exception expected) {
                rejected = true;
            }
            check(rejected);
            tests++;
        }
    }

    public static void main(String[] args) throws Exception {
        check(!RuntimeFeatureProvisioner.NATIVE_RELEASED);
        framing();
        scenario("READY", "inspect", 0, fake -> {});
        scenario("APPLIED", "promote-first-admin", 0, fake -> {});
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.targetRows = 0);
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.targetRows = 2);
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.deleted = "1");
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.verified = "0");
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.loggedOut = "0");
        scenario("HOLD_TARGET", "promote-first-admin", 0, fake -> fake.targetRole = "ADMIN");
        scenario("HOLD_ADMIN", "promote-first-admin", 0, fake -> fake.admins = List.of(List.of("8")));
        scenario("HOLD_ADMIN", "promote-first-admin", 0,
                fake -> fake.admins = List.of(List.of("7"), List.of("8")));
        scenario("ALREADY_APPLIED", "inspect", 9, fake -> {
            fake.admins = List.of(List.of("7")); fake.targetRole = "ADMIN";
        });
        scenario("ALREADY_APPLIED", "promote-first-admin", 9, fake -> {
            fake.admins = List.of(List.of("7")); fake.targetRole = "ADMIN";
        });
        scenario("HOLD_RECEIPT", "inspect", 9, fake -> {
            fake.admins = List.of(List.of("7")); fake.targetRole = "ADMIN"; fake.auditMatch = 0;
        });
        scenario("HOLD_ADMIN", "inspect", 9, fake -> fake.admins = List.of(List.of("8")));
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.updatedRows = 0);
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.updatedRows = 2);
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.failAudit = true);
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.auditRows = 0);
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.generated = 0);
        scenario("HOLD_UNKNOWN", "promote-first-admin", 0, fake -> fake.failCommit = true);
        scenario("HOLD_UNKNOWN", "promote-first-admin", 0, fake -> {
            fake.targetRows = 0; fake.failRollback = true;
        });
        scenario("HOLD_SESSION", "grant-test-entitlement", 0, fake -> {});
        for (String sql : List.of("SELECT CURRENT_USER()", "SELECT DATABASE()",
                "SELECT SHA2(@@server_uuid,256)", "SELECT @@session.transaction_isolation",
                "SELECT @@session.transaction_read_only",
                "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306")) {
            scenario("HOLD_IDENTITY", "promote-first-admin", 0, fake -> fake.overrides.put(sql, "drift"));
        }
        scenario("HOLD_IDENTITY", "promote-first-admin", 0,
                fake -> fake.overrides.put(RuntimeFeatureProvisioner.METADATA.get(0), "THROW"));
        scenario("HOLD_ADMIN", "promote-first-admin", 0,
                fake -> fake.overrides.put(RuntimeFeatureProvisioner.ADMIN_LOCK, "THROW"));
        scenario("HOLD_IDENTITY", "promote-first-admin", 0, fake -> fake.metadataDrift = true);
        scenario("HOLD_IDENTITY", "promote-first-admin", 0, fake -> fake.metadataRows = 257);
        scenario("HOLD_IDENTITY", "promote-first-admin", 0, fake -> fake.overrides.put(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE" + RuntimeFeatureProvisioner.TABLE_FILTER
                + " AND ENGINE='InnoDB' AND TABLE_TYPE='BASE TABLE'", "1"));
        scenario("HOLD_IDENTITY", "promote-first-admin", 0, fake -> fake.overrides.put(
                "SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA=DATABASE() "
                + "AND EVENT_OBJECT_TABLE IN ('users','admin_operation_audit_logs')", "1"));
        scenario("HOLD_TARGET", "promote-first-admin", 0,
                fake -> fake.overrides.put(RuntimeFeatureProvisioner.TARGET + " FOR UPDATE", "THROW"));
        scenario("HOLD_WRITE", "promote-first-admin", 0, fake -> fake.auditRows = 2);
        scenario("HOLD_RECEIPT", "inspect", 9, fake -> {
            fake.admins = List.of(List.of("7")); fake.targetRole = "ADMIN"; fake.auditMatch = 2;
        });
        scenario("HOLD_TARGET", "inspect", 9, fake -> {
            fake.admins = List.of(List.of("7")); fake.targetRole = "ADMIN"; fake.deleted = "1";
        });
        scenario("HOLD_ADMIN", "inspect", 9, fake -> {});
        System.out.println("PASS " + tests + " local JDBC contract cases; MySQL concurrency NOT_RUN");
    }
}
