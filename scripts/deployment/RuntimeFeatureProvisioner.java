import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

/** WI063 local candidate. Native entry remains locked pending independent MySQL proof. */
public final class RuntimeFeatureProvisioner {
    static final boolean NATIVE_RELEASED = false;
    static final String BEFORE = "{\"role\":\"USER\",\"isDeleted\":false}";
    static final String AFTER = "{\"role\":\"ADMIN\",\"isDeleted\":false}";
    static final String ADMIN_LOCK = "SELECT id FROM users FORCE INDEX (PRIMARY) "
            + "WHERE role='ADMIN' ORDER BY id FOR UPDATE";
    static final String TARGET = "SELECT role,is_deleted,is_verified,refresh_token IS NULL "
            + "FROM users WHERE id=? AND SHA2(email,256)=?";
    static final String UPDATE = "UPDATE users SET role='ADMIN' WHERE id=? "
            + "AND SHA2(email,256)=? AND role='USER' AND is_deleted=0 "
            + "AND is_verified=1 AND refresh_token IS NULL";
    static final String AUDIT = "INSERT INTO admin_operation_audit_logs "
            + "(action,target_type,target_id,actor_user_id,outcome,before_state,after_state,"
            + "reason_code,reason_note) VALUES ('USER_ROLE_CHANGE','USER',?,NULL,"
            + "'SUCCEEDED',?,?,'ROLE_CHANGED',?)";
    static final String AUDIT_MATCH = "SELECT COUNT(*) FROM admin_operation_audit_logs "
            + "WHERE id=? AND target_id=? AND actor_user_id IS NULL "
            + "AND action='USER_ROLE_CHANGE' AND target_type='USER' AND outcome='SUCCEEDED' "
            + "AND BINARY before_state=BINARY ? AND BINARY after_state=BINARY ? "
            + "AND reason_code='ROLE_CHANGED' AND BINARY reason_note=BINARY ?";
    static final String TABLE_FILTER = " TABLE_SCHEMA=DATABASE() "
            + "AND TABLE_NAME IN ('users','admin_operation_audit_logs')";
    static final List<String> METADATA = List.of(
            "SELECT TABLE_NAME,ENGINE,TABLE_COLLATION FROM information_schema.TABLES WHERE"
                    + TABLE_FILTER + " ORDER BY TABLE_NAME",
            "SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT,EXTRA,"
                    + "COLLATION_NAME FROM information_schema.COLUMNS WHERE" + TABLE_FILTER
                    + " ORDER BY TABLE_NAME,ORDINAL_POSITION",
            "SELECT TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME,SUB_PART,"
                    + "INDEX_TYPE FROM information_schema.STATISTICS WHERE" + TABLE_FILTER
                    + " ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX");

    private RuntimeFeatureProvisioner() {}

    record Input(String password, String serverHash, String metadataHash, long userId,
                 String principalHash, String nonce, long receiptAuditId) {}
    record Outcome(String status, long auditId) {}

    static void require(boolean condition) throws IOException {
        if (!condition) {
            throw new IOException("REJECTED");
        }
    }

    static String field(DataInputStream input, int max) throws IOException {
        int length = input.readInt();
        require(length > 0 && length <= max);
        byte[] bytes = input.readNBytes(length);
        try {
            require(bytes.length == length);
            for (byte value : bytes) {
                require(value >= 33 && value <= 126);
            }
            return new String(bytes, StandardCharsets.US_ASCII);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    static Input readInput(DataInputStream stream) throws Exception {
        require(stream.readInt() == 0x41465031);
        String password = field(stream, 32);
        String server = field(stream, 64);
        String metadata = field(stream, 64);
        String user = field(stream, 19);
        String principal = field(stream, 64);
        String nonce = field(stream, 32);
        String audit = field(stream, 19);
        require(password.length() == 32 && server.matches("[a-f0-9]{64}")
                && metadata.matches("[a-f0-9]{64}") && principal.matches("[a-f0-9]{64}")
                && nonce.matches("[a-f0-9]{32}") && user.matches("[1-9][0-9]{0,18}")
                && audit.matches("0|[1-9][0-9]{0,18}") && stream.read() == -1);
        long userId = Long.parseLong(user);
        long auditId = Long.parseLong(audit);
        require(userId > 0 && auditId >= 0);
        return new Input(password, server, metadata, userId, principal, nonce, auditId);
    }

    static PreparedStatement prepare(Connection connection, String sql, Object... values)
            throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            statement.setQueryTimeout(3);
            statement.setMaxRows(257);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            return statement;
        } catch (Exception error) {
            statement.close();
            throw error;
        }
    }

    static String scalar(Connection connection, String sql, Object... values) throws Exception {
        try (PreparedStatement statement = prepare(connection, sql, values);
             ResultSet rows = statement.executeQuery()) {
            require(rows.next());
            String value = rows.getString(1);
            require(value != null && !rows.next());
            return value;
        }
    }

    static String metadataHash(Connection connection) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int[] widths = {3, 7, 7};
        for (int index = 0; index < METADATA.size(); index++) {
            digest.update((byte) index);
            int count = 0;
            try (PreparedStatement statement = prepare(connection, METADATA.get(index));
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    require(++count <= 256);
                    for (int column = 1; column <= widths[index]; column++) {
                        String value = rows.getString(column);
                        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
                        require(bytes.length <= 2048);
                        digest.update(ByteBuffer.allocate(4).putInt(value == null ? -1 : bytes.length).array());
                        digest.update(bytes);
                    }
                }
            }
            require(count > 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    static void identity(Connection connection, Input input, boolean readOnly) throws Exception {
        require("atstudio_app@127.0.0.1".equals(scalar(connection, "SELECT CURRENT_USER()")));
        require("atstudio_prod".equals(scalar(connection, "SELECT DATABASE()")));
        require(input.serverHash().equals(scalar(connection, "SELECT SHA2(@@server_uuid,256)")));
        require("1".equals(scalar(connection,
                "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306")));
        require("SERIALIZABLE".equals(scalar(connection, "SELECT @@session.transaction_isolation")));
        require((readOnly ? "1" : "0").equals(scalar(connection,
                "SELECT @@session.transaction_read_only")));
        require("2".equals(scalar(connection, "SELECT COUNT(*) FROM information_schema.TABLES WHERE"
                + TABLE_FILTER + " AND ENGINE='InnoDB' AND TABLE_TYPE='BASE TABLE'")));
        require("0".equals(scalar(connection, "SELECT COUNT(*) FROM information_schema.TRIGGERS "
                + "WHERE TRIGGER_SCHEMA=DATABASE() AND EVENT_OBJECT_TABLE "
                + "IN ('users','admin_operation_audit_logs')")));
        require(input.metadataHash().equals(metadataHash(connection)));
    }

    static Outcome transact(Connection connection, Input input, String mode) {
        boolean committing = false;
        String failure = "HOLD_SESSION";
        try {
            require(mode.equals("inspect") || mode.equals("promote-first-admin"));
            boolean write = mode.equals("promote-first-admin") && input.receiptAuditId() == 0;
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setReadOnly(!write);
            connection.setAutoCommit(false);
            require(connection.getTransactionIsolation() == Connection.TRANSACTION_SERIALIZABLE
                    && connection.isReadOnly() == !write && !connection.getAutoCommit());
            try (PreparedStatement limit = prepare(connection, "SET SESSION innodb_lock_wait_timeout=3")) {
                limit.execute();
            }
            failure = "HOLD_IDENTITY";
            identity(connection, input, !write);
            failure = "HOLD_ADMIN";
            long admin = 0;
            int count = 0;
            String query = write ? ADMIN_LOCK : "SELECT id FROM users WHERE role='ADMIN' ORDER BY id";
            try (PreparedStatement statement = prepare(connection, query);
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    require(++count <= 1);
                    admin = rows.getLong(1);
                }
            }
            require(input.receiptAuditId() > 0 ? count == 1 && admin == input.userId() : count == 0);
            failure = "HOLD_TARGET";
            try (PreparedStatement statement = prepare(connection, TARGET + (write ? " FOR UPDATE" : ""),
                    input.userId(), input.principalHash()); ResultSet rows = statement.executeQuery()) {
                require(rows.next());
                String expected = input.receiptAuditId() > 0 ? "ADMIN" : "USER";
                require(expected.equals(rows.getString(1)) && "0".equals(rows.getString(2))
                        && "1".equals(rows.getString(3)) && "1".equals(rows.getString(4)) && !rows.next());
            }
            String reason = "FIRST_ADMIN_BOOTSTRAP:" + input.nonce();
            if (input.receiptAuditId() > 0) {
                failure = "HOLD_RECEIPT";
                require("1".equals(scalar(connection, AUDIT_MATCH, input.receiptAuditId(), input.userId(),
                        BEFORE, AFTER, reason)));
                connection.rollback();
                return new Outcome("ALREADY_APPLIED", input.receiptAuditId());
            }
            if (!write) {
                connection.rollback();
                return new Outcome("READY", 0);
            }
            failure = "HOLD_WRITE";
            try (PreparedStatement update = prepare(connection, UPDATE, input.userId(), input.principalHash())) {
                require(update.executeUpdate() == 1);
            }
            long auditId;
            try (PreparedStatement audit = prepare(connection, AUDIT, input.userId(), BEFORE, AFTER, reason)) {
                require(audit.executeUpdate() == 1);
                try (ResultSet keys = audit.getGeneratedKeys()) {
                    require(keys.next());
                    auditId = keys.getLong(1);
                    require(auditId > 0 && !keys.next());
                }
            }
            committing = true;
            connection.commit();
            return new Outcome("APPLIED", auditId);
        } catch (Exception ignored) {
            try {
                connection.rollback();
            } catch (Exception rollbackFailure) {
                return new Outcome("HOLD_UNKNOWN", 0);
            }
            return new Outcome(committing ? "HOLD_UNKNOWN" : failure, 0);
        }
    }

    public static void main(String[] args) {
        PrintStream receipt = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        Outcome outcome = new Outcome("HOLD_NATIVE_GATE", 0);
        Properties properties = null;
        try {
            require(NATIVE_RELEASED);
            LogManager.getLogManager().reset();
            DriverManager.setLogWriter(null);
            DriverManager.setLoginTimeout(5);
            outcome = new Outcome("HOLD_INPUT", 0);
            require(Runtime.version().feature() == 17 && args.length == 1
                    && (args[0].equals("inspect") || args[0].equals("promote-first-admin")));
            Input input;
            try (DataInputStream stream = new DataInputStream(System.in)) {
                input = readInput(stream);
            }
            outcome = new Outcome("HOLD_DRIVER", 0);
            Class.forName("com.mysql.cj.jdbc.Driver");
            require(DriverManager.getDriver(RuntimeJdbcProbe.URL).getClass().getName()
                    .equals("com.mysql.cj.jdbc.Driver"));
            properties = RuntimeJdbcProbe.properties(input.password());
            outcome = new Outcome("HOLD_CONNECT", 0);
            try (Connection connection = DriverManager.getConnection(RuntimeJdbcProbe.URL, properties)) {
                properties.clear();
                outcome = transact(connection, input, args[0]);
            } catch (Exception ignored) {
                outcome = new Outcome("HOLD_UNKNOWN", 0);
            }
        } catch (Throwable ignored) {
            // Only fixed status and private audit identity cross the process boundary.
        } finally {
            if (properties != null) {
                properties.clear();
            }
        }
        receipt.print(outcome.status() + ":" + outcome.auditId() + "\n");
        receipt.flush();
        System.exit(outcome.status().startsWith("HOLD_") ? 2 : 0);
    }
}
