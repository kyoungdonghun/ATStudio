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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Internal implementation for bootstrap-disposable-mysql.ps1.
 *
 * <p>The PowerShell wrapper is the supported operator entry point. This class
 * repeats the safety checks so direct invocation also refuses unsafe targets
 * before loading credentials or opening a connection.</p>
 */
public final class DisposableMysqlBootstrap {

    private static final Pattern DISPOSABLE_NAME =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");
    private static final Pattern CREATE_TABLE_STATEMENT =
            Pattern.compile("^CREATE\\s+TABLE\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> LOOPBACK_HOSTS =
            Set.of("localhost", "127.0.0.1", "::1", "[::1]");
    private static final Set<String> PROTECTED_DATABASE_NAMES = Set.of(
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
    private static final String SCHEMA_RELATIVE_PATH =
            "src/main/resources/schema.sql";
    private static final String SEED_RELATIVE_PATH =
            "src/main/resources/seed.sql";
    private static final long EXPECTED_SOURCE_CREATE_TABLE_STATEMENTS = 42L;
    private static final MysqlManifestExpectation CURRENT_MYSQL_MANIFEST_EXPECTATION =
            new RecordedMysqlManifestExpectation(
                    42L,
                    506L,
                    173L,
                    90L,
                    6L,
                    "acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65");
    private static final int DEFAULT_PORT = 3306;

    private DisposableMysqlBootstrap() {
    }

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        try {
            Config config = Config.from(args);
            Inputs inputs = validateBeforeConnection(config);
            safe("action", config.action().value);
            safe("safety.databasePattern", "PASS");
            safe("safety.hostClass", "loopback");
            safe("sql.order", "schema.sql->seed.sql");
            safe(
                    "source.schema.createTableStatements",
                    Long.toString(inputs.schemaCreateTableStatements()));
            safe("source.schema.createTableStatementsCheck", "PASS");
            safe(
                    "mysql.manifest.expectation",
                    CURRENT_MYSQL_MANIFEST_EXPECTATION.state());
            safe(
                    "sql.schema.normalizedTextSha256",
                    normalizedTextSha256(inputs.schema()));
            safe(
                    "sql.seed.normalizedTextSha256",
                    normalizedTextSha256(inputs.seed()));

            enforceManifestAction(config.requestedAction());

            if (config.action() == Action.PREFLIGHT) {
                safe("status", "PASS");
                return 0;
            }

            Credentials credentials = Credentials.fromEnvironment();
            Class.forName("com.mysql.cj.jdbc.Driver");
            Bootstrap bootstrap = new Bootstrap(config, inputs, credentials);
            switch (config.action()) {
                case CREATE, OBSERVE -> bootstrap.create();
                case VALIDATE -> bootstrap.validate();
                case DROP -> bootstrap.drop();
                default -> throw new GuardException("UNSUPPORTED_ACTION");
            }
            safe("status", "PASS");
            return 0;
        } catch (GuardException exception) {
            safeError("status", "REFUSED");
            safeError("reason", exception.code);
            return 2;
        } catch (SQLException exception) {
            safeError("status", "FAILED");
            safeError("failure.class", exception.getClass().getSimpleName());
            safeError("failure.sqlState", safeSqlState(exception.getSQLState()));
            return 1;
        } catch (Exception exception) {
            safeError("status", "FAILED");
            safeError("failure.class", exception.getClass().getSimpleName());
            return 1;
        }
    }

    private static Inputs validateBeforeConnection(Config config) throws Exception {
        validateDatabaseName(config.databaseName());
        validateLoopbackHost(config.host());
        if (config.port() < 1 || config.port() > 65535) {
            throw new GuardException("INVALID_PORT");
        }

        Path workspace = config.workspace().toAbsolutePath().normalize();
        Path schema = workspace.resolve(SCHEMA_RELATIVE_PATH).normalize();
        Path seed = workspace.resolve(SEED_RELATIVE_PATH).normalize();
        if (!schema.startsWith(workspace)
                || !seed.startsWith(workspace)
                || !Files.isRegularFile(schema)
                || !Files.isRegularFile(seed)) {
            throw new GuardException("CURRENT_SQL_INPUTS_UNAVAILABLE");
        }
        String schemaSql = Files.readString(schema, StandardCharsets.UTF_8);
        long schemaCreateTableStatements = splitSqlStatements(schemaSql).stream()
                .filter(statement -> CREATE_TABLE_STATEMENT.matcher(statement).find())
                .count();
        if (schemaCreateTableStatements != EXPECTED_SOURCE_CREATE_TABLE_STATEMENTS) {
            throw new GuardException("CURRENT_SCHEMA_CREATE_TABLE_COUNT_MISMATCH");
        }
        return new Inputs(schema, seed, schemaCreateTableStatements);
    }

    private static void enforceManifestAction(Action requestedAction) {
        if (!CURRENT_MYSQL_MANIFEST_EXPECTATION.isRecorded()
                && (requestedAction == Action.CREATE || requestedAction == Action.VALIDATE)) {
            throw new GuardException("MYSQL_MANIFEST_EXPECTATION_UNRECORDED");
        }
        if (CURRENT_MYSQL_MANIFEST_EXPECTATION.isRecorded()
                && requestedAction == Action.OBSERVE) {
            throw new GuardException("MYSQL_MANIFEST_OBSERVATION_NOT_REQUIRED");
        }
    }

    private static void validateDatabaseName(String databaseName) {
        if (databaseName == null) {
            throw new GuardException("MISSING_DATABASE_NAME");
        }
        String normalized = databaseName.toLowerCase(Locale.ROOT);
        if (PROTECTED_DATABASE_NAMES.contains(normalized)) {
            throw new GuardException("PROTECTED_DATABASE_NAME");
        }
        if (!DISPOSABLE_NAME.matcher(databaseName).matches()) {
            throw new GuardException("INVALID_DISPOSABLE_DATABASE_NAME");
        }
    }

    private static void validateLoopbackHost(String host) {
        if (host == null || !LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new GuardException("NON_LOOPBACK_HOST");
        }
    }

    private static String normalizedTextSha256(Path path) throws Exception {
        String text = Files.readString(path, StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        return sha256(text);
    }

    private static String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) {
            result.append(String.format("%02x", value));
        }
        return result.toString();
    }

    private static String safeSqlState(String sqlState) {
        if (sqlState == null || !sqlState.matches("[A-Za-z0-9]{5}")) {
            return "unknown";
        }
        return sqlState;
    }

    private static void safe(String key, String value) {
        System.out.println(key + "=" + value);
    }

    private static void safeError(String key, String value) {
        System.err.println(key + "=" + value);
    }

    private enum Action {
        PREFLIGHT("preflight"),
        OBSERVE("observe"),
        CREATE("create"),
        VALIDATE("validate"),
        DROP("drop");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        static Action parse(String value) {
            if (value == null) {
                throw new GuardException("MISSING_ACTION");
            }
            for (Action action : values()) {
                if (action.value.equals(value.toLowerCase(Locale.ROOT))) {
                    return action;
                }
            }
            throw new GuardException("UNSUPPORTED_ACTION");
        }
    }

    private record Config(
            Action action,
            Action requestedAction,
            Path workspace,
            String host,
            int port,
            String databaseName) {

        static Config from(String[] args) {
            Map<String, String> values = parseArgs(args);
            int port;
            try {
                port = Integer.parseInt(values.getOrDefault("port", Integer.toString(DEFAULT_PORT)));
            } catch (NumberFormatException exception) {
                throw new GuardException("INVALID_PORT");
            }
            String workspace = values.get("workspace");
            if (workspace == null || workspace.isBlank()) {
                throw new GuardException("MISSING_WORKSPACE");
            }
            Action action = Action.parse(values.get("action"));
            String requestedActionValue = values.get("requested-action");
            if (requestedActionValue != null && action != Action.PREFLIGHT) {
                throw new GuardException("INVALID_ARGUMENT_COMBINATION");
            }
            Action requestedAction = requestedActionValue == null
                    ? action
                    : Action.parse(requestedActionValue);
            return new Config(
                    action,
                    requestedAction,
                    Path.of(workspace),
                    values.get("host"),
                    port,
                    values.get("database"));
        }

        private static Map<String, String> parseArgs(String[] args) {
            Map<String, String> values = new LinkedHashMap<>();
            for (int index = 0; index < args.length; index++) {
                String argument = args[index];
                if (!argument.startsWith("--") || argument.length() == 2) {
                    throw new GuardException("INVALID_ARGUMENT");
                }
                if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                    throw new GuardException("MISSING_ARGUMENT_VALUE");
                }
                String key = argument.substring(2);
                if (!Set.of(
                        "action",
                        "requested-action",
                        "workspace",
                        "host",
                        "port",
                        "database").contains(key)) {
                    throw new GuardException("UNKNOWN_ARGUMENT");
                }
                if (values.put(key, args[++index]) != null) {
                    throw new GuardException("DUPLICATE_ARGUMENT");
                }
            }
            return values;
        }
    }

    private record Inputs(Path schema, Path seed, long schemaCreateTableStatements) {
    }

    private record Credentials(String username, String password) {

        static Credentials fromEnvironment() {
            String username = System.getenv("SPRING_DATASOURCE_USERNAME");
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (username == null || username.isBlank()
                    || password == null || password.isBlank()) {
                throw new GuardException("CREDENTIALS_UNAVAILABLE");
            }
            return new Credentials(username, password);
        }
    }

    private static final class Bootstrap {

        private final Config config;
        private final Inputs inputs;
        private final Credentials credentials;

        private Bootstrap(Config config, Inputs inputs, Credentials credentials) {
            this.config = config;
            this.inputs = inputs;
            this.credentials = credentials;
        }

        void create() throws Exception {
            boolean created = false;
            try {
                try (Connection admin = openAdminConnection()) {
                    if (databaseCount(admin) != 0L) {
                        throw new GuardException("DISPOSABLE_DATABASE_ALREADY_EXISTS");
                    }
                    execute(admin, "CREATE DATABASE `" + config.databaseName()
                            + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    created = true;
                }

                try (Connection database = openDatabaseConnection()) {
                    requireSelectedDatabase(database);
                    applySqlFile(database, inputs.schema());
                    safe("schema.apply", "PASS");
                    applySqlFile(database, inputs.seed());
                    safe("seed.apply", "PASS");
                }
                validate();
            } catch (Exception exception) {
                if (created) {
                    cleanupCreatedDatabase();
                }
                throw exception;
            }
        }

        void validate() throws Exception {
            try (Connection admin = openAdminConnection()) {
                if (databaseCount(admin) != 1L) {
                    throw new GuardException("DISPOSABLE_DATABASE_NOT_FOUND");
                }
            }

            try (Connection database = openDatabaseConnection()) {
                requireSelectedDatabase(database);
                Manifest manifest = readManifest(database);
                outputManifest(manifest);
                if (!CURRENT_MYSQL_MANIFEST_EXPECTATION.matches(manifest)) {
                    if (!CURRENT_MYSQL_MANIFEST_EXPECTATION.isRecorded()) {
                        throw new GuardException("MYSQL_MANIFEST_EXPECTATION_UNRECORDED");
                    }
                    throw new GuardException("V1_MANIFEST_MISMATCH");
                }
                safe("manifest", "PASS");
            }
        }

        private void outputManifest(Manifest manifest) {
            safe("manifest.tables", Long.toString(manifest.tables()));
            safe("manifest.columns", Long.toString(manifest.columns()));
            safe("manifest.indexes", Long.toString(manifest.indexes()));
            safe("manifest.foreignKeys", Long.toString(manifest.foreignKeys()));
            safe("manifest.plans", Long.toString(manifest.plans()));
            safe("manifest.planKeys", Long.toString(manifest.planKeys()));
            safe("manifest.forbiddenTables", Long.toString(manifest.forbiddenTables()));
            safe("manifest.forbiddenColumns", Long.toString(manifest.forbiddenColumns()));
            safe("manifest.sha256", manifest.sha256());
        }

        void drop() throws SQLException {
            try (Connection admin = openAdminConnection()) {
                execute(admin, "DROP DATABASE IF EXISTS `" + config.databaseName() + "`");
                if (databaseCount(admin) != 0L) {
                    throw new SQLException("Disposable database cleanup failed.");
                }
                safe("drop", "PASS");
            }
        }

        private void cleanupCreatedDatabase() {
            try (Connection admin = openAdminConnection()) {
                execute(admin, "DROP DATABASE IF EXISTS `" + config.databaseName() + "`");
                safe("cleanupAfterFailure", databaseCount(admin) == 0L ? "PASS" : "FAILED");
            } catch (Exception ignored) {
                safeError("cleanupAfterFailure", "FAILED");
            }
        }

        private Connection openAdminConnection() throws SQLException {
            return DriverManager.getConnection(
                    jdbcUrl(null),
                    credentials.username(),
                    credentials.password());
        }

        private Connection openDatabaseConnection() throws SQLException {
            return DriverManager.getConnection(
                    jdbcUrl(config.databaseName()),
                    credentials.username(),
                    credentials.password());
        }

        private String jdbcUrl(String databaseName) {
            String host = config.host();
            if ("::1".equals(host)) {
                host = "[::1]";
            }
            String databasePath = databaseName == null ? "" : databaseName;
            return "jdbc:mysql://" + host + ":" + config.port() + "/" + databasePath
                    + "?allowPublicKeyRetrieval=true"
                    + "&connectTimeout=5000"
                    + "&socketTimeout=30000"
                    + "&serverTimezone=Asia%2FSeoul"
                    + "&characterEncoding=UTF-8";
        }

        private long databaseCount(Connection connection) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?")) {
                statement.setString(1, config.databaseName());
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getLong(1);
                }
            }
        }

        private void requireSelectedDatabase(Connection connection) throws SQLException {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT DATABASE()")) {
                if (!resultSet.next()
                        || !config.databaseName().equals(resultSet.getString(1))) {
                    throw new SQLException("Guarded disposable database was not selected.");
                }
            }
        }

        private void applySqlFile(Connection connection, Path path) throws Exception {
            String sql = Files.readString(path, StandardCharsets.UTF_8);
            for (String statementText : splitSqlStatements(sql)) {
                executeAndDrain(connection, statementText);
            }
        }

        private Manifest readManifest(Connection connection) throws Exception {
            long tables = scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'");
            long columns = scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE table_schema = DATABASE()");
            long indexes = scalar(connection,
                    "SELECT COUNT(DISTINCT table_name, index_name) "
                            + "FROM information_schema.statistics "
                            + "WHERE table_schema = DATABASE()");
            long foreignKeys = scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.referential_constraints "
                            + "WHERE constraint_schema = DATABASE()");
            long plans = scalar(connection, "SELECT COUNT(*) FROM subscriptions");
            long planKeys = scalar(connection,
                    "SELECT COUNT(DISTINCT name, user_type) FROM subscriptions");
            long forbiddenTables = scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.tables "
                            + "WHERE table_schema = DATABASE() "
                            + "AND table_name IN ('play_histories', 'download_queue')");
            long forbiddenColumns = scalar(connection,
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE table_schema = DATABASE() AND ("
                            + "(table_name = 'tracks' AND column_name = 'preview_file') OR "
                            + "(table_name = 'whitelist_export_items' AND "
                            + "column_name IN ('user_id_snapshot', 'user_nickname_snapshot')))");
            String manifestHash = manifestHash(connection);
            return new Manifest(
                    tables,
                    columns,
                    indexes,
                    foreignKeys,
                    plans,
                    planKeys,
                    forbiddenTables,
                    forbiddenColumns,
                    manifestHash);
        }

        private long scalar(Connection connection, String sql) throws SQLException {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                resultSet.next();
                return resultSet.getLong(1);
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
    }

    private record Manifest(
            long tables,
            long columns,
            long indexes,
            long foreignKeys,
            long plans,
            long planKeys,
            long forbiddenTables,
            long forbiddenColumns,
            String sha256) {
    }

    private interface MysqlManifestExpectation {

        boolean isRecorded();

        String state();

        boolean matches(Manifest manifest);
    }

    private enum UnrecordedMysqlManifestExpectation implements MysqlManifestExpectation {
        INSTANCE;

        @Override
        public boolean isRecorded() {
            return false;
        }

        @Override
        public String state() {
            return "UNRECORDED";
        }

        @Override
        public boolean matches(Manifest manifest) {
            return false;
        }
    }

    private record RecordedMysqlManifestExpectation(
            long tables,
            long columns,
            long indexes,
            long foreignKeys,
            long plans,
            String sha256) implements MysqlManifestExpectation {

        @Override
        public boolean isRecorded() {
            return true;
        }

        @Override
        public String state() {
            return "RECORDED";
        }

        @Override
        public boolean matches(Manifest manifest) {
            return manifest.tables() == tables
                    && manifest.columns() == columns
                    && manifest.indexes() == indexes
                    && manifest.foreignKeys() == foreignKeys
                    && manifest.plans() == plans
                    && manifest.planKeys() == plans
                    && manifest.forbiddenTables() == 0L
                    && manifest.forbiddenColumns() == 0L
                    && sha256.equals(manifest.sha256());
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void executeAndDrain(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean hasResult = statement.execute(sql);
            while (true) {
                if (hasResult) {
                    try (ResultSet resultSet = statement.getResultSet()) {
                        while (resultSet.next()) {
                            // SQL inputs are DDL/seed only; discard any unexpected rows.
                        }
                    }
                } else if (statement.getUpdateCount() == -1) {
                    break;
                }
                hasResult = statement.getMoreResults();
            }
        }
    }

    private static List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean backtickQuoted = false;
        boolean lineComment = false;
        boolean blockComment = false;

        for (int index = 0; index < sql.length(); index++) {
            char value = sql.charAt(index);
            char next = index + 1 < sql.length() ? sql.charAt(index + 1) : '\0';

            if (lineComment) {
                if (value == '\n' || value == '\r') {
                    lineComment = false;
                    current.append(value);
                }
                continue;
            }
            if (blockComment) {
                if (value == '*' && next == '/') {
                    blockComment = false;
                    index++;
                }
                continue;
            }
            if (!singleQuoted && !doubleQuoted && !backtickQuoted) {
                if (value == '-' && next == '-'
                        && (index + 2 >= sql.length()
                        || Character.isWhitespace(sql.charAt(index + 2)))) {
                    lineComment = true;
                    index++;
                    continue;
                }
                if (value == '/' && next == '*') {
                    blockComment = true;
                    index++;
                    continue;
                }
            }

            current.append(value);
            if (value == '\\' && (singleQuoted || doubleQuoted) && next != '\0') {
                current.append(next);
                index++;
                continue;
            }
            if (value == '\'' && !doubleQuoted && !backtickQuoted) {
                if (singleQuoted && next == '\'') {
                    current.append(next);
                    index++;
                } else {
                    singleQuoted = !singleQuoted;
                }
                continue;
            }
            if (value == '"' && !singleQuoted && !backtickQuoted) {
                doubleQuoted = !doubleQuoted;
                continue;
            }
            if (value == '`' && !singleQuoted && !doubleQuoted) {
                backtickQuoted = !backtickQuoted;
                continue;
            }
            if (value == ';' && !singleQuoted && !doubleQuoted && !backtickQuoted) {
                String statement = current.substring(0, current.length() - 1).trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                current.setLength(0);
            }
        }

        if (singleQuoted || doubleQuoted || backtickQuoted || blockComment) {
            throw new GuardException("INVALID_SQL_INPUT");
        }
        String pending = current.toString().trim();
        if (!pending.isEmpty()) {
            statements.add(pending);
        }
        return statements;
    }

    private static final class GuardException extends RuntimeException {

        private final String code;

        private GuardException(String code) {
            super(code);
            this.code = code;
        }
    }
}
