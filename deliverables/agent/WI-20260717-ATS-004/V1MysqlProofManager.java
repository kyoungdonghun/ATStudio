import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class V1MysqlProofManager {

    private static final String LOCAL_DATABASE = "atstudio";
    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_wi004_\\d{8}_[a-z0-9]{8}$");
    private static final List<String> PAYMENT_PROVIDER_TABLES = List.of(
            "billing_agreements",
            "payment_orders",
            "subscription_payments",
            "payment_settlements",
            "payment_refunds",
            "payment_entitlement_corrections",
            "payment_reconciliation_incidents",
            "payment_receipts",
            "payment_operation_audit_logs");

    private final Path workspace;
    private final String databaseName;
    private final DbConfig config;

    private V1MysqlProofManager(Path workspace, String databaseName, DbConfig config) {
        this.workspace = workspace;
        this.databaseName = databaseName;
        this.config = config;
    }

    public static void main(String[] args) {
        Map<String, String> parsed = parseArgs(args);
        try {
            Path workspace = Path.of(parsed.getOrDefault("workspace", "."))
                    .toAbsolutePath()
                    .normalize();
            String database = parsed.getOrDefault("database", "");
            DbConfig config = DbConfig.fromEnvironmentOrLocalFile(
                    workspace.resolve("application-local.yml"));
            V1MysqlProofManager manager = new V1MysqlProofManager(workspace, database, config);
            manager.execute(
                    parsed.getOrDefault("mode", "preflight"),
                    Boolean.parseBoolean(parsed.getOrDefault("approved-local-recreate", "false")));
            safe("manager.result", "PASS");
        } catch (Exception exception) {
            safe("manager.result", "FAIL");
            safe("failure.class", exception.getClass().getSimpleName());
            SQLException sqlException = findCause(exception, SQLException.class);
            if (sqlException != null) {
                safe("failure.sqlState", safeSqlState(sqlException.getSQLState()));
                safe("failure.errorCode", Integer.toString(sqlException.getErrorCode()));
            }
            System.exit(1);
        }
    }

    private void execute(String mode, boolean approvedLocalRecreate) throws Exception {
        config.requireLoopbackAtstudio();
        safe("database.hostClass", "loopback");
        safe("database.sourceName", LOCAL_DATABASE);

        switch (mode) {
            case "preflight" -> preflight();
            case "create-disposable" -> createDisposable();
            case "apply-first" -> applyFirst();
            case "apply-second-expect-fail" -> applySecondExpectFailure();
            case "manifest" -> verifyManifest(databaseName);
            case "drop-disposable" -> dropDisposable();
            case "recreate-local" -> recreateLocal(approvedLocalRecreate);
            default -> throw new IllegalArgumentException("Unsupported proof-manager mode.");
        }
    }

    private void preflight() throws SQLException {
        if (!LOCAL_DATABASE.equals(databaseName)) {
            throw new IllegalArgumentException("Preflight requires the exact local database name.");
        }
        try (Connection admin = config.openAdminConnection()) {
            safe("database.name", LOCAL_DATABASE);
            safe("database.exists", Long.toString(databaseCount(admin, LOCAL_DATABASE)));
            long disposables = wi004DisposableDatabaseCount(admin);
            safe("disposable.databases", Long.toString(disposables));
            if (disposables != 0L) {
                throw new IllegalStateException("Prior WI-004 disposable databases must be absent.");
            }
            long sessions = activeDatabaseSessions(admin, LOCAL_DATABASE);
            safe("database.activeSessions", Long.toString(sessions));
            if (sessions != 0L) {
                throw new IllegalStateException("Active local database sessions block destructive proof.");
            }
        }
    }

    private void createDisposable() throws SQLException {
        requireDisposableName();
        try (Connection admin = config.openAdminConnection()) {
            if (databaseCount(admin, databaseName) != 0L) {
                throw new IllegalStateException("Disposable database must be absent before creation.");
            }
            execute(admin, "CREATE DATABASE `" + databaseName
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            safe("disposable.create", "PASS");
        }
    }

    private void applyFirst() throws Exception {
        requireDisposableName();
        try (Connection database = config.openDatabaseConnection(databaseName)) {
            requireSelectedDatabase(database, databaseName);
            long before = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()");
            if (before != 0L) {
                throw new IllegalStateException("First apply requires an empty disposable database.");
            }
            applySqlFile(database, "src/main/resources/schema.sql", "schema");
            applySqlFile(database, "src/main/resources/seed.sql", "seed");
            safe("schema.firstApply", "PASS");
        }
    }

    private void applySecondExpectFailure() throws Exception {
        requireDisposableName();
        try (Connection database = config.openDatabaseConnection(databaseName)) {
            requireSelectedDatabase(database, databaseName);
            try {
                applySqlFile(database, "src/main/resources/schema.sql", "schema-second");
            } catch (SQLException expected) {
                if (!"42S01".equals(expected.getSQLState()) && expected.getErrorCode() != 1050) {
                    throw expected;
                }
                safe("schema.secondApply", "EXPECTED_FAILURE");
                safe("schema.secondApply.sqlState", safeSqlState(expected.getSQLState()));
                return;
            }
            throw new IllegalStateException("Second schema apply unexpectedly succeeded.");
        }
    }

    private void recreateLocal(boolean approved) throws Exception {
        if (!approved || !LOCAL_DATABASE.equals(databaseName)) {
            throw new IllegalArgumentException("Local recreation requires exact target and explicit approval flag.");
        }
        try (Connection admin = config.openAdminConnection()) {
            long sessions = activeDatabaseSessions(admin, LOCAL_DATABASE);
            safe("database.activeSessions", Long.toString(sessions));
            if (sessions != 0L) {
                throw new IllegalStateException("Active local database sessions block destructive recreation.");
            }
            if (databaseCount(admin, LOCAL_DATABASE) == 1L) {
                execute(admin, "DROP DATABASE `" + LOCAL_DATABASE + "`");
            }
            execute(admin, "CREATE DATABASE `" + LOCAL_DATABASE
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        try (Connection database = config.openDatabaseConnection(LOCAL_DATABASE)) {
            requireSelectedDatabase(database, LOCAL_DATABASE);
            applySqlFile(database, "src/main/resources/schema.sql", "schema");
            applySqlFile(database, "src/main/resources/seed.sql", "seed");
        }
        verifyManifest(LOCAL_DATABASE);
        safe("local.recreate", "PASS");
    }

    private void verifyManifest(String target) throws Exception {
        requireAllowedTarget(target);
        try (Connection database = config.openDatabaseConnection(target)) {
            requireSelectedDatabase(database, target);
            long tables = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'");
            long columns = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE()");
            long indexes = scalar(database,
                    "SELECT COUNT(DISTINCT table_name, index_name) FROM information_schema.statistics "
                            + "WHERE table_schema = DATABASE()");
            long foreignKeys = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.referential_constraints "
                            + "WHERE constraint_schema = DATABASE()");
            long forbiddenTables = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() "
                            + "AND table_name IN ('play_histories', 'download_queue')");
            long forbiddenColumns = scalar(database,
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND ("
                            + "(table_name = 'tracks' AND column_name = 'preview_file') OR "
                            + "(table_name = 'whitelist_export_items' "
                            + "AND column_name IN ('user_id_snapshot', 'user_nickname_snapshot'))) ");
            long plans = scalar(database, "SELECT COUNT(*) FROM subscriptions");
            long planKeys = scalar(database,
                    "SELECT COUNT(DISTINCT name, user_type) FROM subscriptions");
            ProviderCounts providerCounts = providerCounts(database);

            if (tables != 39L || forbiddenTables != 0L || forbiddenColumns != 0L
                    || plans != 6L || planKeys != 6L
                    || providerCounts.total() != PAYMENT_PROVIDER_TABLES.size()
                    || providerCounts.tossOnly() != PAYMENT_PROVIDER_TABLES.size()
                    || providerCounts.nonNull() != 7L) {
                throw new IllegalStateException("Database manifest does not match the V1 baseline contract.");
            }

            safe("manifest.tables", Long.toString(tables));
            safe("manifest.columns", Long.toString(columns));
            safe("manifest.indexes", Long.toString(indexes));
            safe("manifest.foreignKeys", Long.toString(foreignKeys));
            safe("manifest.forbiddenTables", Long.toString(forbiddenTables));
            safe("manifest.forbiddenColumns", Long.toString(forbiddenColumns));
            safe("manifest.plans", Long.toString(plans));
            safe("manifest.providerColumns", Long.toString(providerCounts.total()));
            safe("manifest.providerTossOnly", Long.toString(providerCounts.tossOnly()));
            safe("manifest.sha256", manifestHash(database));
        }
    }

    private ProviderCounts providerCounts(Connection connection) throws SQLException {
        String placeholders = String.join(",", PAYMENT_PROVIDER_TABLES.stream().map(ignored -> "?").toList());
        String sql = "SELECT COUNT(*), "
                + "SUM(LOWER(column_type) = 'enum(''toss'')'), "
                + "SUM(is_nullable = 'NO') "
                + "FROM information_schema.columns WHERE table_schema = DATABASE() "
                + "AND column_name = 'provider' AND table_name IN (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < PAYMENT_PROVIDER_TABLES.size(); index++) {
                statement.setString(index + 1, PAYMENT_PROVIDER_TABLES.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new ProviderCounts(
                        resultSet.getLong(1),
                        resultSet.getLong(2),
                        resultSet.getLong(3));
            }
        }
    }

    private String manifestHash(Connection connection) throws Exception {
        String sql = "SELECT table_name, ordinal_position, column_name, column_type, is_nullable "
                + "FROM information_schema.columns WHERE table_schema = DATABASE() "
                + "ORDER BY table_name, ordinal_position";
        StringBuilder manifest = new StringBuilder();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                manifest.append(resultSet.getString(1)).append('|')
                        .append(resultSet.getInt(2)).append('|')
                        .append(resultSet.getString(3)).append('|')
                        .append(resultSet.getString(4)).append('|')
                        .append(resultSet.getString(5)).append('\n');
            }
        }
        return sha256(manifest.toString());
    }

    private void dropDisposable() throws SQLException {
        requireDisposableName();
        try (Connection admin = config.openAdminConnection()) {
            execute(admin, "DROP DATABASE IF EXISTS `" + databaseName + "`");
            long remaining = databaseCount(admin, databaseName);
            safe("disposable.remaining", Long.toString(remaining));
            if (remaining != 0L) {
                throw new IllegalStateException("Disposable database cleanup failed.");
            }
        }
    }

    private void applySqlFile(Connection connection, String relativePath, String label) throws Exception {
        Path path = workspace.resolve(relativePath).normalize();
        if (!path.startsWith(workspace) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("SQL input is missing or outside the workspace.");
        }
        String sql = Files.readString(path, StandardCharsets.UTF_8);
        int executed = 0;
        for (String command : splitStatements(sql)) {
            if (!command.isBlank()) {
                executeAndDrain(connection, command);
                executed++;
            }
        }
        safe(label + ".statements", Integer.toString(executed));
        safe(label + ".sha256", sha256(sql));
    }

    private void requireDisposableName() {
        if (!DISPOSABLE_DATABASE.matcher(databaseName).matches()) {
            throw new IllegalArgumentException("Disposable database name failed the WI-004 guard.");
        }
    }

    private void requireAllowedTarget(String target) {
        if (!LOCAL_DATABASE.equals(target) && !DISPOSABLE_DATABASE.matcher(target).matches()) {
            throw new IllegalArgumentException("Database target failed the proof guard.");
        }
    }

    private static void requireSelectedDatabase(Connection connection, String expected) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DATABASE()")) {
            if (!resultSet.next() || !expected.equals(resultSet.getString(1))) {
                throw new IllegalStateException("Connection selected an unexpected database.");
            }
        }
    }

    private static long activeDatabaseSessions(Connection connection, String database) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.processlist "
                        + "WHERE db = ? AND id <> CONNECTION_ID()")) {
            statement.setString(1, database);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private static long databaseCount(Connection connection, String database) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?")) {
            statement.setString(1, database);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private static long wi004DisposableDatabaseCount(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.schemata "
                        + "WHERE schema_name REGEXP '^ats_wi004_[0-9]{8}_[a-z0-9]{8}$'");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private static long scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void executeAndDrain(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean result = statement.execute(sql);
            while (true) {
                if (result) {
                    try (ResultSet ignored = statement.getResultSet()) {
                        while (ignored.next()) {
                            // Results are intentionally discarded and never written to proof logs.
                        }
                    }
                } else if (statement.getUpdateCount() == -1) {
                    break;
                }
                result = statement.getMoreResults();
            }
        }
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\\R", -1)) {
            if (line.trim().startsWith("--")) {
                continue;
            }
            current.append(line).append(System.lineSeparator());
            String pending = current.toString().trim();
            if (pending.endsWith(";")) {
                statements.add(pending.substring(0, pending.length() - 1));
                current.setLength(0);
            }
        }
        if (!current.toString().isBlank()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }

    private static String sha256(String text) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder value = new StringBuilder();
        for (byte item : digest) {
            value.append(String.format("%02x", item));
        }
        return value.toString();
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            if (args[index].startsWith("--") && index + 1 < args.length) {
                values.put(args[index].substring(2), args[++index]);
            }
        }
        return values;
    }

    private static <T extends Throwable> T findCause(Throwable failure, Class<T> type) {
        Throwable current = failure;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private static String safeSqlState(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private static void safe(String key, String value) {
        System.out.println(key + "=" + value);
    }

    private record ProviderCounts(long total, long tossOnly, long nonNull) {
    }

    private record DbConfig(String jdbcUrl, String username, String password) {

        static DbConfig fromEnvironmentOrLocalFile(Path applicationLocal) throws Exception {
            String url = System.getenv("SPRING_DATASOURCE_URL");
            String username = System.getenv("SPRING_DATASOURCE_USERNAME");
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (hasText(url) && hasText(username) && hasText(password)) {
                return new DbConfig(url, username, password);
            }
            if (!Files.isRegularFile(applicationLocal)) {
                throw new IllegalStateException("Datasource credentials are unavailable.");
            }
            String content = Files.readString(applicationLocal, StandardCharsets.UTF_8);
            url = yamlScalar(content, "url");
            username = yamlScalar(content, "username");
            password = yamlScalar(content, "password");
            if (!hasText(url) || !hasText(username) || !hasText(password)
                    || url.contains("REPLACE_WITH")
                    || username.contains("REPLACE_WITH")
                    || password.contains("REPLACE_WITH")) {
                throw new IllegalStateException("Datasource credentials are missing or placeholders.");
            }
            return new DbConfig(url, username, password);
        }

        void requireLoopbackAtstudio() {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            String host = parts.hostPort().toLowerCase(Locale.ROOT);
            boolean loopback = host.equals("localhost")
                    || host.startsWith("localhost:")
                    || host.equals("127.0.0.1")
                    || host.startsWith("127.0.0.1:")
                    || host.equals("[::1]")
                    || host.startsWith("[::1]:");
            if (!loopback || !LOCAL_DATABASE.equals(parts.databaseName())) {
                throw new IllegalArgumentException("Datasource must target loopback atstudio exactly.");
            }
        }

        Connection openAdminConnection() throws SQLException {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return DriverManager.getConnection(
                    "jdbc:mysql://" + parts.hostPort() + "/" + parts.filteredQuery(),
                    username,
                    password);
        }

        Connection openDatabaseConnection(String database) throws SQLException {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return DriverManager.getConnection(
                    "jdbc:mysql://" + parts.hostPort() + "/" + database + parts.filteredQuery(),
                    username,
                    password);
        }

        private static String yamlScalar(String content, String key) {
            Matcher matcher = Pattern.compile(
                    "(?m)^\\s*" + Pattern.quote(key) + "\\s*:\\s*(.+?)\\s*$")
                    .matcher(content);
            if (!matcher.find()) {
                return "";
            }
            String value = matcher.group(1).trim();
            if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                    || value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }

    private record JdbcParts(String hostPort, String databaseName, String query) {

        static JdbcParts parse(String jdbcUrl) {
            if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
                throw new IllegalArgumentException("Only jdbc:mysql datasource URLs are supported.");
            }
            String remainder = jdbcUrl.substring("jdbc:mysql://".length());
            int slash = remainder.indexOf('/');
            if (slash <= 0) {
                throw new IllegalArgumentException("Datasource URL has no database path.");
            }
            String hostPort = remainder.substring(0, slash);
            String pathAndQuery = remainder.substring(slash + 1);
            int question = pathAndQuery.indexOf('?');
            String database = question >= 0
                    ? pathAndQuery.substring(0, question)
                    : pathAndQuery;
            String query = question >= 0 ? pathAndQuery.substring(question + 1) : "";
            return new JdbcParts(hostPort, database, query);
        }

        String filteredQuery() {
            if (query == null || query.isBlank()) {
                return "";
            }
            String filtered = String.join("&", Arrays.stream(query.split("&"))
                    .filter(part -> !part.toLowerCase(Locale.ROOT)
                            .startsWith("createdatabaseifnotexist="))
                    .toList());
            return filtered.isBlank() ? "" : "?" + filtered;
        }
    }
}
