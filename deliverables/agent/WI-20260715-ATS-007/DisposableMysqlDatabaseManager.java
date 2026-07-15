import java.io.IOException;
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

public class DisposableMysqlDatabaseManager {

    private static final Pattern DISPOSABLE_NAME =
            Pattern.compile("^ats_wi007_\\d{8}_[a-z0-9]{8}$");
    private static final List<String> PROTECTED_DATABASE_NAMES = List.of(
            "atstudio",
            "mysql",
            "information_schema",
            "performance_schema",
            "sys",
            "preview",
            "stage",
            "staging",
            "prod",
            "production");
    private static final List<String> SQL_FILES = List.of(
            "src/main/resources/schema.sql",
            "src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql",
            "src/main/resources/db/manual/20260618_company_certification_documents.sql",
            "src/main/resources/db/manual/20260714_storage_mutations_journal.sql",
            "src/main/resources/db/manual/20260714_payment_db_integrity.sql");

    private final Path workspace;
    private final String databaseName;
    private final List<String> log = new ArrayList<>();

    private DisposableMysqlDatabaseManager(Path workspace, String databaseName) {
        this.workspace = workspace;
        this.databaseName = databaseName;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> parsed = parseArgs(args);
        Path workspace = Path.of(parsed.getOrDefault("workspace", "."))
                .toAbsolutePath()
                .normalize();
        String database = parsed.get("database");
        Path logPath = Path.of(parsed.getOrDefault(
                "log",
                workspace.resolve(
                        "deliverables/agent/WI-20260715-ATS-007/database-manager.log")
                        .toString()))
                .toAbsolutePath()
                .normalize();
        DisposableMysqlDatabaseManager manager =
                new DisposableMysqlDatabaseManager(workspace, database);
        try {
            manager.execute(parsed.getOrDefault("mode", "create"));
            manager.log("result=PASS");
        } catch (Exception exception) {
            manager.log("result=FAIL");
            manager.log("failure.class=" + exception.getClass().getSimpleName());
            SQLException sqlException = findCause(exception, SQLException.class);
            if (sqlException != null) {
                manager.log("failure.sqlState=" + safeSqlState(sqlException.getSQLState()));
            }
            throw exception;
        } finally {
            Files.createDirectories(logPath.getParent());
            Files.write(logPath, manager.log, StandardCharsets.UTF_8);
        }
    }

    private void execute(String mode) throws Exception {
        validateDisposableName(databaseName, null);
        DbConfig config = DbConfig.fromEnvironmentOrLocalFile(
                workspace.resolve("application-local.yml"));
        validateDisposableName(databaseName, config.applicationDatabaseName());
        config.requireLoopbackHost();
        log("wi=WI-20260715-ATS-007");
        log("credential.source=process-env-or-application-local-yml; values-not-printed");
        log("database.alias=WI007_DISPOSABLE; actual-name-not-printed");
        log("database.hostClass=loopback");

        switch (mode) {
            case "create" -> create(config);
            case "drop" -> drop(config);
            case "verify-absent" -> verifyAbsent(config);
            case "diagnostics" -> diagnostics(config);
            default -> throw new IllegalArgumentException("Unsupported database manager mode.");
        }
    }

    private void create(DbConfig config) throws Exception {
        try (Connection admin = config.openAdminConnection()) {
            admin.setAutoCommit(true);
            if (databaseExists(admin)) {
                throw new IllegalStateException("Disposable database must be absent before creation.");
            }
            execute(admin, "CREATE DATABASE `" + databaseName
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            log("create.database=OK");
        }

        try (Connection database = config.openDatabaseConnection(databaseName)) {
            database.setAutoCommit(true);
            assertSelectedDisposableDatabase(database);
            for (String relativePath : SQL_FILES) {
                applySqlFile(database, relativePath);
            }
            log("schema.apply=OK");
        }
    }

    private void drop(DbConfig config) throws SQLException {
        try (Connection admin = config.openAdminConnection()) {
            admin.setAutoCommit(true);
            validateDisposableName(databaseName, config.applicationDatabaseName());
            execute(admin, "DROP DATABASE IF EXISTS `" + databaseName + "`");
            log("drop.database=OK");
            long remaining = databaseCount(admin);
            log("cleanup.database.exists=" + remaining);
            if (remaining != 0L) {
                throw new SQLException("Disposable database remained after drop.");
            }
        }
    }

    private void verifyAbsent(DbConfig config) throws SQLException {
        try (Connection admin = config.openAdminConnection()) {
            admin.setAutoCommit(true);
            long remaining = databaseCount(admin);
            log("cleanup.database.exists=" + remaining);
            if (remaining != 0L) {
                throw new SQLException("Disposable database still exists.");
            }
        }
    }

    private void diagnostics(DbConfig config) throws Exception {
        try (Connection admin = config.openAdminConnection()) {
            admin.setAutoCommit(true);
            if (!databaseExists(admin)) {
                log("diagnostics.database.exists=0");
                return;
            }
        }
        try (Connection database = config.openDatabaseConnection(databaseName);
             Statement statement = database.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW ENGINE INNODB STATUS")) {
            String status = "";
            if (resultSet.next()) {
                status = resultSet.getString("Status");
            }
            String normalized = status == null ? "" : status.toLowerCase(Locale.ROOT);
            log("diagnostics.innodbStatusCaptured=" + !normalized.isBlank());
            log("diagnostics.latestDeadlockPresent="
                    + normalized.contains("latest detected deadlock"));
            log("diagnostics.lockWaitPresent=" + normalized.contains("lock wait"));
            log("diagnostics.statusSha256=" + sha256(status == null ? "" : status));
        }
    }

    private void applySqlFile(Connection connection, String relativePath) throws Exception {
        Path path = workspace.resolve(relativePath).normalize();
        if (!path.startsWith(workspace) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("SQL input pointer is missing or outside the workspace.");
        }
        String sql = Files.readString(path, StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sql);
        int executed = 0;
        for (String statement : statements) {
            if (statement.isBlank()) {
                continue;
            }
            executeAndDrain(connection, statement);
            executed++;
        }
        log("sql.file=" + relativePath.replace('\\', '/'));
        log("sql.sha256=" + sha256(sql));
        log("sql.executed.count=" + executed);
    }

    private void assertSelectedDisposableDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DATABASE()")) {
            if (!resultSet.next() || !databaseName.equals(resultSet.getString(1))) {
                throw new SQLException("Connection did not select the guarded disposable database.");
            }
        }
    }

    private boolean databaseExists(Connection connection) throws SQLException {
        return databaseCount(connection) == 1L;
    }

    private long databaseCount(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?")) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
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
                            // Drain result sets without persisting row content in evidence.
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
        String delimiter = ";";
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\\R", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue;
            }
            if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                addPendingStatement(statements, current);
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
        addPendingStatement(statements, current);
        return statements;
    }

    private static void addPendingStatement(List<String> statements, StringBuilder current) {
        String pending = current.toString().trim();
        if (!pending.isEmpty()) {
            statements.add(pending);
            current.setLength(0);
        }
    }

    private static void validateDisposableName(String databaseName, String applicationDatabaseName) {
        if (databaseName == null || !DISPOSABLE_NAME.matcher(databaseName).matches()) {
            throw new IllegalArgumentException("Database name failed the WI007 disposable pattern.");
        }
        String lower = databaseName.toLowerCase(Locale.ROOT);
        if (PROTECTED_DATABASE_NAMES.contains(lower)
                || applicationDatabaseName != null
                && lower.equals(applicationDatabaseName.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Refusing a protected database target.");
        }
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

    private static String safeSqlState(String sqlState) {
        return sqlState == null || sqlState.isBlank() ? "unknown" : sqlState;
    }

    private static <T extends Throwable> T findCause(Throwable exception, Class<T> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private void log(String value) {
        log.add(value);
        System.out.println(value);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            if (!args[index].startsWith("--")) {
                continue;
            }
            String key = args[index].substring(2);
            if (index + 1 < args.length && !args[index + 1].startsWith("--")) {
                values.put(key, args[++index]);
            } else {
                values.put(key, "true");
            }
        }
        return values;
    }

    private record DbConfig(String jdbcUrl, String username, String password) {

        static DbConfig fromEnvironmentOrLocalFile(Path applicationLocal) throws IOException {
            String envUrl = System.getenv("SPRING_DATASOURCE_URL");
            String envUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
            String envPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (hasText(envUrl) && hasText(envUsername) && hasText(envPassword)) {
                return new DbConfig(envUrl, envUsername, envPassword);
            }
            if (!Files.isRegularFile(applicationLocal)) {
                throw new IllegalStateException("Datasource credentials are unavailable.");
            }
            String content = Files.readString(applicationLocal, StandardCharsets.UTF_8);
            String url = extractYamlScalar(content, "url");
            String username = extractYamlScalar(content, "username");
            String password = extractYamlScalar(content, "password");
            if (!hasText(url) || !hasText(username) || !hasText(password)
                    || url.contains("REPLACE_WITH")
                    || username.contains("REPLACE_WITH")
                    || password.contains("REPLACE_WITH")) {
                throw new IllegalStateException("Datasource credentials are missing or placeholders.");
            }
            return new DbConfig(url, username, password);
        }

        String applicationDatabaseName() {
            return JdbcParts.parse(jdbcUrl).databaseName();
        }

        void requireLoopbackHost() {
            String host = JdbcParts.parse(jdbcUrl).hostPort().toLowerCase(Locale.ROOT);
            if (!(host.equals("localhost")
                    || host.startsWith("localhost:")
                    || host.equals("127.0.0.1")
                    || host.startsWith("127.0.0.1:")
                    || host.equals("[::1]")
                    || host.startsWith("[::1]:"))) {
                throw new IllegalArgumentException("Package G accepts only a loopback MySQL credential source.");
            }
        }

        Connection openAdminConnection() throws SQLException {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return DriverManager.getConnection(
                    "jdbc:mysql://" + parts.hostPort() + "/" + parts.filteredQuery(),
                    username,
                    password);
        }

        Connection openDatabaseConnection(String databaseName) throws SQLException {
            JdbcParts parts = JdbcParts.parse(jdbcUrl);
            return DriverManager.getConnection(
                    "jdbc:mysql://" + parts.hostPort() + "/" + databaseName + parts.filteredQuery(),
                    username,
                    password);
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }

        private static String extractYamlScalar(String content, String key) {
            Pattern pattern = Pattern.compile(
                    "(?m)^\\s*" + Pattern.quote(key) + "\\s*:\\s*(.+?)\\s*$");
            Matcher matcher = pattern.matcher(content);
            if (!matcher.find()) {
                return "";
            }
            String value = matcher.group(1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || value.startsWith("'") && value.endsWith("'")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
    }

    private record JdbcParts(String hostPort, String databaseName, String query) {

        static JdbcParts parse(String jdbcUrl) {
            if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
                throw new IllegalArgumentException("Only jdbc:mysql credential sources are supported.");
            }
            String remainder = jdbcUrl.substring("jdbc:mysql://".length());
            int slash = remainder.indexOf('/');
            if (slash <= 0) {
                throw new IllegalArgumentException("JDBC credential source has no database path.");
            }
            String hostPort = remainder.substring(0, slash);
            String pathAndQuery = remainder.substring(slash + 1);
            int question = pathAndQuery.indexOf('?');
            String database = question >= 0
                    ? pathAndQuery.substring(0, question)
                    : pathAndQuery;
            String query = question >= 0 ? pathAndQuery.substring(question + 1) : "";
            if (database.isBlank()) {
                throw new IllegalArgumentException("JDBC credential source database is missing.");
            }
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
