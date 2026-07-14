import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisposableMysqlRehearsal {
    private static final List<String> UNSAFE_DATABASE_NAMES = List.of(
            "atstudio",
            "mysql",
            "information_schema",
            "performance_schema",
            "sys"
    );

    private static final List<String> PAYMENT_ORDER_STATUSES = List.of(
            "READY",
            "IN_PROGRESS",
            "PROCESSING",
            "PROVIDER_SUCCEEDED",
            "PENDING_PROVIDER_CONFIRMATION",
            "DONE",
            "FAILED",
            "CANCELLED",
            "EXPIRED"
    );

    private static final List<String> PAYMENT_AUDIT_ACTIONS = List.of(
            "RECONCILIATION_INCIDENT_STATUS_UPDATE",
            "RECEIPT_EVIDENCE_CREATED",
            "PAYMENT_REFUND_REQUESTED",
            "PAYMENT_REFUND_APPROVED",
            "PAYMENT_REFUND_PROCESSING",
            "PAYMENT_REFUND_SUCCEEDED",
            "PAYMENT_REFUND_FAILED",
            "PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION",
            "PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED",
            "PAYMENT_ENTITLEMENT_CORRECTION_APPROVED",
            "PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING",
            "PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED",
            "PAYMENT_ENTITLEMENT_CORRECTION_FAILED",
            "PAYMENT_SETTLEMENT_IMPORTED",
            "PAYMENT_SETTLEMENT_RECONCILED",
            "PAYMENT_SETTLEMENT_IGNORED"
    );

    private static final List<String> PAYMENT_AUDIT_TARGETS = List.of(
            "RECONCILIATION_INCIDENT",
            "PAYMENT_RECEIPT",
            "PAYMENT_REFUND",
            "PAYMENT_ENTITLEMENT_CORRECTION",
            "PAYMENT_SETTLEMENT"
    );

    private static final List<String> STORAGE_DOMAINS = List.of(
            "TRACK",
            "PLAYLIST",
            "ALBUM",
            "COMPANY_CERTIFICATION",
            "NOTICE",
            "QUESTION"
    );

    private static final List<String> STORAGE_TYPES = List.of("CREATE", "REPLACE", "DELETE");
    private static final List<String> STORAGE_ROOTS = List.of("PUBLIC", "PRIVATE");
    private static final List<String> STORAGE_STATES = List.of(
            "PREPARED",
            "COMMITTED",
            "ROLLBACK_CLEANUP",
            "AFTER_COMMIT_DELETE",
            "RETRY",
            "DONE",
            "FAILED"
    );

    private final Path workspace;
    private final String requestedDatabaseName;
    private final List<String> log = new ArrayList<>();

    private DisposableMysqlRehearsal(Path workspace, String requestedDatabaseName) {
        this.workspace = workspace;
        this.requestedDatabaseName = requestedDatabaseName;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> parsedArgs = parseArgs(args);
        Path workspace = Path.of(parsedArgs.getOrDefault("workspace", ".")).toAbsolutePath().normalize();
        String dbName = parsedArgs.get("database");
        if (dbName == null || dbName.isBlank()) {
            dbName = "ats_wi021_" + LocalDate.now().toString().replace("-", "")
                    + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        DisposableMysqlRehearsal runner = new DisposableMysqlRehearsal(workspace, dbName);
        try {
            String mode = parsedArgs.getOrDefault("mode", "full");
            if ("drop-only".equals(mode)) {
                runner.dropOnly();
            } else {
                boolean dropAfter = !"false".equalsIgnoreCase(parsedArgs.getOrDefault("drop", "true"));
                runner.run(dropAfter);
            }
        } catch (Exception ex) {
            runner.log("RESULT: FAILED " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            throw ex;
        } finally {
            Path logPath = Path.of(parsedArgs.getOrDefault(
                    "log",
                    workspace.resolve("deliverables/agent/WI-20260714-ATS-021/rehearsal-jdbc.log").toString()
            ));
            Files.createDirectories(logPath.toAbsolutePath().normalize().getParent());
            Files.write(logPath, runner.log, StandardCharsets.UTF_8);
        }
    }

    private void run(boolean dropAfter) throws Exception {
        DbConfig config = DbConfig.fromEnvironmentOrLocalFile(workspace.resolve("application-local.yml"));
        validateDisposableName(requestedDatabaseName, config.applicationDatabaseName());

        log("WI: WI-20260714-ATS-021");
        log("connection.method: MySQL Connector/J via process environment; secret values not printed");
        log("mysql.cli.on.path: false-or-not-used");
        log("disposable.database: " + requestedDatabaseName);
        log("application.database.target.changed: false");
        log("application.database.name.equals.disposable: " + requestedDatabaseName.equals(config.applicationDatabaseName()));
        log("application.database.name.safe-check: " + maskDatabaseName(config.applicationDatabaseName()));
        log("admin.jdbc.url.class: " + config.adminTargetClass());

        try (Connection admin = DriverManager.getConnection(
                config.adminJdbcUrl(),
                config.username(),
                config.password())) {
            admin.setAutoCommit(true);
            logQueryScalar(admin, "server.version", "SELECT VERSION()");
            logQueryScalar(admin, "server.version_comment", "SELECT @@version_comment");
            verifyDatabaseAbsent(admin, requestedDatabaseName);
            execute(admin, "CREATE DATABASE `" + requestedDatabaseName
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            log("create.database: OK");
        }

        try (Connection db = DriverManager.getConnection(
                config.databaseJdbcUrl(requestedDatabaseName),
                config.username(),
                config.password())) {
            db.setAutoCommit(true);
            logQueryScalar(db, "selected.database", "SELECT DATABASE()");

            applySqlFile(db, workspace.resolve("src/main/resources/schema.sql"));
            applySqlFile(db, workspace.resolve("src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql"));
            applySqlFile(db, workspace.resolve("src/main/resources/db/manual/20260618_company_certification_documents.sql"));
            applySqlFile(db, workspace.resolve("src/main/resources/db/manual/20260714_storage_mutations_journal.sql"));
            applySqlFile(db, workspace.resolve("src/main/resources/db/manual/20260714_payment_db_integrity.sql"));

            assertContract(db);
            proveEnumFlushes(db);
            log("contract.validation: OK");
        } finally {
            if (dropAfter) {
                dropDatabase(config);
            } else {
                log("drop.database: deferred-for-hibernate-validate");
            }
        }

        log("RESULT: PASS");
    }

    private void dropOnly() throws Exception {
        DbConfig config = DbConfig.fromEnvironmentOrLocalFile(workspace.resolve("application-local.yml"));
        validateDisposableName(requestedDatabaseName, config.applicationDatabaseName());
        log("WI: WI-20260714-ATS-021");
        log("connection.method: MySQL Connector/J via process environment; secret values not printed");
        log("disposable.database: " + requestedDatabaseName);
        dropDatabase(config);
        log("RESULT: PASS");
    }

    private void dropDatabase(DbConfig config) throws SQLException {
        boolean dropAttempted = false;
        try (Connection admin = DriverManager.getConnection(
                config.adminJdbcUrl(),
                config.username(),
                config.password())) {
            admin.setAutoCommit(true);
            validateDisposableName(requestedDatabaseName, config.applicationDatabaseName());
            dropAttempted = true;
            execute(admin, "DROP DATABASE `" + requestedDatabaseName + "`");
            log("drop.database: OK");
            logQueryScalar(admin, "cleanup.database.exists",
                    "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '" + requestedDatabaseName + "'");
        } finally {
            log("drop.attempted: " + dropAttempted);
        }
    }

    private void applySqlFile(Connection connection, Path path) throws Exception {
        String relative = workspace.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
        String sql = Files.readString(path, StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sql);
        log("sql.file: " + relative);
        log("sql.sha256: " + sha256(sql));
        log("sql.statement.count: " + statements.size());
        int executed = 0;
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            executeStatement(connection, trimmed);
            executed++;
        }
        log("sql.executed.count: " + executed);
    }

    private void assertContract(Connection connection) throws SQLException {
        assertColumnEnumContains(connection, "payment_orders", "status", PAYMENT_ORDER_STATUSES);
        assertColumnExists(connection, "payment_orders", "command_key", "varchar(191)");
        assertColumnExists(connection, "payment_orders", "billing_period_start", "date");
        assertColumnExists(connection, "payment_orders", "provider_attempt", "int");
        assertColumnExists(connection, "payment_orders", "provider_idempotency_key", "varchar(100)");
        assertColumnExists(connection, "payment_orders", "processing_started_at", "datetime");
        assertIndex(connection, "payment_orders", "uq_payment_orders_command_key", true,
                "command_key");
        assertIndex(connection, "payment_orders", "uq_payment_orders_provider_attempt_key", true,
                "provider,provider_idempotency_key");
        assertIndex(connection, "payment_orders", "uq_payment_orders_renewal_period", true,
                "billing_agreement_id,user_subscription_id,purpose,billing_period_start");
        assertIndex(connection, "payment_orders", "idx_payment_orders_status_processing", false,
                "status,processing_started_at");

        assertColumnExists(connection, "subscription_payments", "pg_transaction_id", "varchar(200)");
        assertIndex(connection, "subscription_payments", "uq_subscription_payments_order", true,
                "payment_order_id");
        assertIndex(connection, "subscription_payments", "uq_subscription_payments_provider_transaction", true,
                "provider,pg_transaction_id");

        assertColumnEnumContains(connection, "payment_operation_audit_logs", "action", PAYMENT_AUDIT_ACTIONS);
        assertColumnEnumContains(connection, "payment_operation_audit_logs", "target_type", PAYMENT_AUDIT_TARGETS);

        assertColumnEnumContains(connection, "storage_mutations", "domain", STORAGE_DOMAINS);
        assertColumnEnumContains(connection, "storage_mutations", "mutation_type", STORAGE_TYPES);
        assertColumnEnumContains(connection, "storage_mutations", "storage_root", STORAGE_ROOTS);
        assertColumnEnumContains(connection, "storage_mutations", "state", STORAGE_STATES);
        assertIndex(connection, "storage_mutations", "idx_storage_mutations_recovery", false,
                "state,next_attempt_at,id");
        assertIndex(connection, "storage_mutations", "idx_storage_mutations_operation_id", false,
                "operation_id");
        assertCheckConstraint(connection, "storage_mutations", "chk_storage_mutations_keys");

        logQueryScalar(connection, "table.count",
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()");
    }

    private void proveEnumFlushes(Connection connection) throws SQLException {
        execute(connection,
                "INSERT INTO users (nickname, email, role, user_type, created_at, updated_at) "
                        + "VALUES ('wi021-user', 'wi021@example.invalid', 'USER', 'INDIVIDUAL', NOW(), NOW())");
        long userId = scalarLong(connection, "SELECT id FROM users WHERE email = 'wi021@example.invalid'");
        execute(connection,
                "INSERT INTO subscriptions (name, user_type, price_monthly, price_yearly, download_per_day, max_whitelist_channels, max_playlists, created_at, updated_at) "
                        + "VALUES ('WI021', 'INDIVIDUAL', 0, 0, 0, 0, 1, NOW(), NOW())");
        long subscriptionId = scalarLong(connection, "SELECT id FROM subscriptions WHERE name = 'WI021'");

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO payment_orders "
                        + "(order_id, command_key, user_id, purpose, provider, status, subscription_id, billing_cycle, amount, expires_at, created_at, updated_at) "
                        + "VALUES (?, ?, ?, 'SUBSCRIBE', 'MOCK', ?, ?, 'MONTHLY', 0, NOW(), NOW(), NOW())")) {
            int index = 0;
            for (String status : PAYMENT_ORDER_STATUSES) {
                statement.setString(1, "wi021-order-" + index);
                statement.setString(2, "WI021:" + index);
                statement.setLong(3, userId);
                statement.setString(4, status);
                statement.setLong(5, subscriptionId);
                statement.executeUpdate();
                index++;
            }
        }
        log("enum.flush.payment_order_status.count: " + PAYMENT_ORDER_STATUSES.size());

        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO payment_operation_audit_logs (action, target_type, created_at, updated_at) "
                        + "VALUES (?, ?, NOW(), NOW())")) {
            int index = 0;
            for (String action : PAYMENT_AUDIT_ACTIONS) {
                statement.setString(1, action);
                statement.setString(2, PAYMENT_AUDIT_TARGETS.get(index % PAYMENT_AUDIT_TARGETS.size()));
                statement.executeUpdate();
                index++;
            }
            for (String target : PAYMENT_AUDIT_TARGETS) {
                statement.setString(1, "PAYMENT_REFUND_REQUESTED");
                statement.setString(2, target);
                statement.executeUpdate();
            }
        }
        log("enum.flush.payment_audit_action.count: " + PAYMENT_AUDIT_ACTIONS.size());
        log("enum.flush.payment_audit_target.count: " + PAYMENT_AUDIT_TARGETS.size());

        insertStorageMutationRows(connection, STORAGE_DOMAINS, "domain");
        insertStorageMutationRows(connection, STORAGE_ROOTS, "storage_root");
        insertStorageMutationRows(connection, STORAGE_STATES, "state");
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO storage_mutations "
                        + "(operation_id, domain, mutation_type, storage_root, new_key, old_key, state, created_at, updated_at) "
                        + "VALUES (?, 'TRACK', ?, 'PUBLIC', ?, ?, 'PREPARED', NOW(), NOW())")) {
            int index = 0;
            for (String type : STORAGE_TYPES) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, type);
                if ("DELETE".equals(type)) {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                    statement.setString(4, "old-" + index);
                } else if ("REPLACE".equals(type)) {
                    statement.setString(3, "new-" + index);
                    statement.setString(4, "old-" + index);
                } else {
                    statement.setString(3, "new-" + index);
                    statement.setNull(4, java.sql.Types.VARCHAR);
                }
                statement.executeUpdate();
                index++;
            }
        }
        log("enum.flush.storage_domain.count: " + STORAGE_DOMAINS.size());
        log("enum.flush.storage_type.count: " + STORAGE_TYPES.size());
        log("enum.flush.storage_root.count: " + STORAGE_ROOTS.size());
        log("enum.flush.storage_state.count: " + STORAGE_STATES.size());
    }

    private void insertStorageMutationRows(Connection connection, List<String> values, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO storage_mutations "
                        + "(operation_id, domain, mutation_type, storage_root, new_key, state, created_at, updated_at) "
                        + "VALUES (?, ?, 'CREATE', ?, 'new-key', ?, NOW(), NOW())")) {
            for (String value : values) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, "domain".equals(column) ? value : "TRACK");
                statement.setString(3, "storage_root".equals(column) ? value : "PUBLIC");
                statement.setString(4, "state".equals(column) ? value : "PREPARED");
                statement.executeUpdate();
            }
        }
    }

    private void assertColumnExists(Connection connection, String table, String column, String expectedTypePrefix)
            throws SQLException {
        String type = columnType(connection, table, column);
        if (type == null || !type.toLowerCase(Locale.ROOT).startsWith(expectedTypePrefix)) {
            throw new SQLException("Column contract mismatch: " + table + "." + column + " -> " + type);
        }
        log("column.ok: " + table + "." + column + "=" + type);
    }

    private void assertColumnEnumContains(Connection connection, String table, String column, List<String> expected)
            throws SQLException {
        String type = columnType(connection, table, column);
        if (type == null) {
            throw new SQLException("Missing enum column: " + table + "." + column);
        }
        for (String value : expected) {
            if (!type.contains("'" + value + "'")) {
                throw new SQLException("Enum " + table + "." + column + " missing " + value);
            }
        }
        log("enum.ok: " + table + "." + column + " contains " + expected.size() + " values");
    }

    private String columnType(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT column_type FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        }
    }

    private void assertIndex(Connection connection, String table, String index, boolean unique, String columns)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT non_unique, GROUP_CONCAT(column_name ORDER BY seq_in_index) "
                        + "FROM information_schema.statistics "
                        + "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ? "
                        + "GROUP BY non_unique")) {
            statement.setString(1, table);
            statement.setString(2, index);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Missing index: " + table + "." + index);
                }
                int nonUnique = resultSet.getInt(1);
                String actualColumns = resultSet.getString(2);
                if ((unique && nonUnique != 0) || (!unique && nonUnique != 1)
                        || !columns.equals(actualColumns)) {
                    throw new SQLException("Index contract mismatch: " + table + "." + index);
                }
                log("index.ok: " + table + "." + index + " unique=" + unique + " columns=" + actualColumns);
            }
        }
    }

    private void assertCheckConstraint(Connection connection, String table, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.check_constraints "
                        + "WHERE constraint_schema = DATABASE() AND constraint_name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                if (resultSet.getLong(1) != 1) {
                    throw new SQLException("Missing check constraint: " + table + "." + name);
                }
                log("check.ok: " + table + "." + name);
            }
        }
    }

    private static void verifyDatabaseAbsent(Connection connection, String dbName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?")) {
            statement.setString(1, dbName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                if (resultSet.getLong(1) != 0) {
                    throw new SQLException("Disposable database name already exists: " + dbName);
                }
            }
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void executeStatement(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            if (hasResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    logResultPreview(sql, resultSet);
                }
            }
        }
    }

    private void logResultPreview(String sql, ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount++;
            if (rowCount <= 3) {
                List<String> values = new ArrayList<>();
                for (int column = 1; column <= columnCount; column++) {
                    values.add(metaData.getColumnLabel(column) + "=" + resultSet.getString(column));
                }
                log("select.preview: " + values);
            }
        }
        log("select.rows: " + rowCount + " sql=" + summarizeSql(sql));
    }

    private long scalarLong(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private void logQueryScalar(Connection connection, String key, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            log(key + ": " + resultSet.getString(1));
        }
    }

    private static List<String> splitStatements(String sql) throws IOException {
        List<String> statements = new ArrayList<>();
        String delimiter = ";";
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\\R", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue;
            }
            if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                String pending = current.toString().trim();
                if (!pending.isEmpty()) {
                    statements.add(pending);
                    current.setLength(0);
                }
                delimiter = trimmed.substring("DELIMITER ".length()).trim();
                continue;
            }
            current.append(line).append(System.lineSeparator());
            String buffer = current.toString().trim();
            if (buffer.endsWith(delimiter)) {
                statements.add(buffer.substring(0, buffer.length() - delimiter.length()));
                current.setLength(0);
            }
        }
        String pending = current.toString().trim();
        if (!pending.isEmpty()) {
            statements.add(pending);
        }
        return statements;
    }

    private static String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private static void validateDisposableName(String dbName, String appDbName) {
        if (!dbName.matches("^ats_wi021_\\d{8}_[a-z0-9]{8}$")) {
            throw new IllegalArgumentException("Unexpected disposable database name: " + dbName);
        }
        String lower = dbName.toLowerCase(Locale.ROOT);
        if (UNSAFE_DATABASE_NAMES.contains(lower) || lower.equalsIgnoreCase(appDbName)) {
            throw new IllegalArgumentException("Refusing unsafe database target: " + dbName);
        }
    }

    private static String maskDatabaseName(String dbName) {
        if (dbName == null || dbName.isBlank()) {
            return "absent";
        }
        if (dbName.length() <= 2) {
            return "**";
        }
        return dbName.charAt(0) + "***" + dbName.charAt(dbName.length() - 1)
                + " (not used)";
    }

    private static String summarizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim().substring(0, Math.min(96, sql.length()));
    }

    private void log(String line) {
        log.add(line);
        System.out.println(line);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    values.put(key, args[++i]);
                } else {
                    values.put(key, "true");
                }
            }
        }
        return values;
    }

    private record DbConfig(String source, String jdbcUrl, String username, String password) {
        static DbConfig fromEnvironmentOrLocalFile(Path applicationLocal) throws IOException {
            String envUrl = System.getenv("SPRING_DATASOURCE_URL");
            String envUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
            String envPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (hasText(envUrl) && hasText(envUsername) && hasText(envPassword)) {
                return new DbConfig("env", envUrl, envUsername, envPassword);
            }
            if (!Files.isRegularFile(applicationLocal)) {
                throw new IllegalStateException("No datasource env and no application-local.yml");
            }
            String content = Files.readString(applicationLocal, StandardCharsets.UTF_8);
            String url = extractYamlScalar(content, "url");
            String username = extractYamlScalar(content, "username");
            String password = extractYamlScalar(content, "password");
            if (!hasText(url) || !hasText(username) || !hasText(password)
                    || url.contains("REPLACE_WITH") || username.contains("REPLACE_WITH")
                    || password.contains("REPLACE_WITH")) {
                throw new IllegalStateException("Datasource values are missing or placeholders");
            }
            return new DbConfig("application-local.yml", url, username, password);
        }

        String applicationDatabaseName() {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return parts.databaseName();
        }

        String adminTargetClass() {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            if (parts.hostPort().startsWith("localhost")
                    || parts.hostPort().startsWith("127.0.0.1")
                    || parts.hostPort().startsWith("[::1]")) {
                return "loopback-or-localhost";
            }
            return "not-disclosed";
        }

        String adminJdbcUrl() {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return "jdbc:mysql://" + parts.hostPort() + "/"
                    + parts.queryWithoutCreateDatabaseFlag();
        }

        String databaseJdbcUrl(String databaseName) {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return "jdbc:mysql://" + parts.hostPort() + "/" + databaseName
                    + parts.queryWithoutCreateDatabaseFlag();
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }

        private static String extractYamlScalar(String content, String key) {
            Pattern pattern = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*:\\s*(.+?)\\s*$");
            Matcher matcher = pattern.matcher(content);
            if (!matcher.find()) {
                return "";
            }
            String value = matcher.group(1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    private record JdbcParts(String hostPort, String databaseName, String query) {
        static JdbcParts parse(String jdbcUrl) {
            if (!jdbcUrl.startsWith("jdbc:mysql://")) {
                throw new IllegalArgumentException("Only jdbc:mysql URLs are supported");
            }
            String remainder = jdbcUrl.substring("jdbc:mysql://".length());
            int slash = remainder.indexOf('/');
            if (slash < 0) {
                throw new IllegalArgumentException("JDBC URL has no database path");
            }
            String hostPort = remainder.substring(0, slash);
            String pathAndQuery = remainder.substring(slash + 1);
            int question = pathAndQuery.indexOf('?');
            String database = question >= 0 ? pathAndQuery.substring(0, question) : pathAndQuery;
            String query = question >= 0 ? pathAndQuery.substring(question + 1) : "";
            return new JdbcParts(hostPort, database, query);
        }

        String queryWithoutCreateDatabaseFlag() {
            if (query == null || query.isBlank()) {
                return "";
            }
            String filtered = String.join("&", Arrays.stream(query.split("&"))
                    .filter(part -> !part.toLowerCase(Locale.ROOT).startsWith("createdatabaseifnotexist="))
                    .toList());
            return filtered.isBlank() ? "" : "?" + filtered;
        }
    }
}
