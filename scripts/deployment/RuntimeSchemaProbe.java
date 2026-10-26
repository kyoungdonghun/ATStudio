import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;

import jakarta.persistence.Entity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.schema.internal.ExceptionHandlerHaltImpl;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExceptionHandler;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;

/** WI006: metadata bootstrap and explicit schema validation, never a SessionFactory. */
public final class RuntimeSchemaProbe {
    static final String[] QUERIES = {
        "SELECT TABLE_NAME,TABLE_TYPE,ENGINE,TABLE_COLLATION FROM information_schema.TABLES "
            + "WHERE TABLE_SCHEMA='atstudio_prod' ORDER BY TABLE_NAME",
        "SELECT TABLE_NAME,COLUMN_NAME,ORDINAL_POSITION,COLUMN_TYPE,IS_NULLABLE,"
            + "COLUMN_DEFAULT,EXTRA,CHARACTER_SET_NAME,COLLATION_NAME "
            + "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='atstudio_prod' "
            + "ORDER BY TABLE_NAME,ORDINAL_POSITION",
        "SELECT TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME,NON_UNIQUE,INDEX_TYPE,"
            + "SUB_PART,EXPRESSION,IS_VISIBLE FROM information_schema.STATISTICS "
            + "WHERE TABLE_SCHEMA='atstudio_prod' ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX",
        "SELECT TABLE_NAME,CONSTRAINT_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,"
            + "REFERENCED_COLUMN_NAME,ORDINAL_POSITION FROM information_schema.KEY_COLUMN_USAGE "
            + "WHERE TABLE_SCHEMA='atstudio_prod' AND REFERENCED_TABLE_NAME IS NOT NULL "
            + "ORDER BY TABLE_NAME,CONSTRAINT_NAME,ORDINAL_POSITION"
    };
    static final int[] WIDTHS = {4, 9, 9, 6};

    private RuntimeSchemaProbe() {}

    static Map<String, Object> settings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        settings.put("jakarta.persistence.database-product-name", "MySQL");
        settings.put("jakarta.persistence.database-major-version", "8");
        settings.put("jakarta.persistence.database-minor-version", "0");
        settings.put("hibernate.boot.allow_jdbc_metadata_access", false);
        settings.put("hibernate.implicit_naming_strategy",
                "org.springframework.boot.hibernate.SpringImplicitNamingStrategy");
        settings.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl");
        settings.put("hibernate.default_catalog", "atstudio_prod");
        settings.put("hibernate.hbm2ddl.auto", "none");
        settings.put("jakarta.persistence.schema-generation.database.action", "none");
        settings.put("jakarta.persistence.schema-generation.scripts.action", "none");
        settings.put("jakarta.persistence.validation.mode", "none");
        settings.put("hibernate.archive.autodetection", "none");
        settings.put("hibernate.show_sql", false);
        settings.put("hibernate.format_sql", false);
        settings.put("hibernate.use_sql_comments", false);
        settings.put("hibernate.default_batch_fetch_size", 100);
        return settings;
    }

    static Metadata metadata(StandardServiceRegistry registry, Path names) throws Exception {
        RuntimeJdbcProbe.require(Files.size(names) < 8192);
        List<String> classes = Files.readAllLines(names, StandardCharsets.US_ASCII);
        RuntimeJdbcProbe.require(classes.size() == 43 && classes.stream().distinct().count() == 43);
        MetadataSources source = new MetadataSources(registry);
        for (String name : classes) {
            RuntimeJdbcProbe.require(name.matches("com[.]atstudio[.]atstudio[.]entity[.][A-Z][A-Za-z0-9]*"));
            Class<?> type = Class.forName(name, false, RuntimeSchemaProbe.class.getClassLoader());
            RuntimeJdbcProbe.require(type.isAnnotationPresent(Entity.class));
            source.addAnnotatedClass(type);
        }
        Metadata result = source.buildMetadata();
        RuntimeJdbcProbe.require(result.getEntityBindings().size() == 43);
        List<String> tables = result.getDatabase().getNamespaces().iterator().next().getTables()
                .stream().map(table -> table.getName()).sorted().toList();
        RuntimeJdbcProbe.require(tables.equals(RuntimeJdbcProbe.TABLES));
        return result;
    }

    static void validate(Metadata metadata, StandardServiceRegistry registry,
                         Map<String, Object> settings) {
        ExecutionOptions options = new ExecutionOptions() {
            public Map<String, Object> getConfigurationValues() { return settings; }
            public boolean shouldManageNamespaces() { return false; }
            public ExceptionHandler getExceptionHandler() { return ExceptionHandlerHaltImpl.INSTANCE; }
        };
        registry.getService(SchemaManagementTool.class).getSchemaValidator(settings)
                .doValidation(metadata, options, ContributableMatcher.ALL);
    }

    static String snapshot(Connection connection) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        StringBuilder counts = new StringBuilder();
        for (int query = 0; query < QUERIES.length; query++) {
            digest.update((byte) query);
            int count = 0;
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(3);
                statement.setMaxRows(4097);
                try (ResultSet rows = statement.executeQuery(QUERIES[query])) {
                    while (rows.next()) {
                        RuntimeJdbcProbe.require(++count <= 4096);
                        if (query == 0) {
                            RuntimeJdbcProbe.require(count <= RuntimeJdbcProbe.TABLES.size()
                                    && RuntimeJdbcProbe.TABLES.get(count - 1).equals(rows.getString(1))
                                    && "BASE TABLE".equals(rows.getString(2)));
                        }
                        for (int column = 1; column <= WIDTHS[query]; column++) {
                            String value = rows.getString(column);
                            byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
                            RuntimeJdbcProbe.require(bytes.length <= 8192);
                            digest.update(ByteBuffer.allocate(4).putInt(value == null ? -1 : bytes.length).array());
                            digest.update(bytes);
                        }
                    }
                }
            }
            RuntimeJdbcProbe.require(query != 0 || count == 43);
            counts.append(count).append('|');
        }
        return counts + HexFormat.of().formatHex(digest.digest());
    }

    static final class ReadOnlyProvider implements ConnectionProvider, AutoCloseable {
        private static final long serialVersionUID = 1L;
        private final Properties credentials;
        private final String serverHash;
        private final List<Connection> opened = new ArrayList<>();
        private int attempts;

        ReadOnlyProvider(Properties credentials, String serverHash) {
            this.credentials = credentials;
            this.serverHash = serverHash;
        }

        public Connection getConnection() throws SQLException {
            if (credentials == null || ++attempts > 4) {
                throw new SQLException("CONNECTION_REJECTED");
            }
            Connection connection = DriverManager.getConnection(RuntimeJdbcProbe.URL, credentials);
            opened.add(connection);
            try {
                connection.setReadOnly(true);
                connection.setAutoCommit(false);
                RuntimeJdbcProbe.require(connection.isReadOnly() && !connection.getAutoCommit());
                try (Statement statement = connection.createStatement()) {
                    statement.setQueryTimeout(3);
                    statement.setMaxRows(2);
                    RuntimeJdbcProbe.require("1".equals(RuntimeJdbcProbe.scalar(statement,
                            "SELECT @@session.transaction_read_only")));
                    RuntimeJdbcProbe.require("atstudio_app@127.0.0.1".equals(RuntimeJdbcProbe.scalar(
                            statement, "SELECT CURRENT_USER()")));
                    RuntimeJdbcProbe.require("atstudio_prod".equals(RuntimeJdbcProbe.scalar(
                            statement, "SELECT DATABASE()")));
                    RuntimeJdbcProbe.require(serverHash.equals(RuntimeJdbcProbe.scalar(
                            statement, "SELECT SHA2(@@server_uuid,256)")));
                    RuntimeJdbcProbe.require("1".equals(RuntimeJdbcProbe.scalar(statement,
                            "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306")));
                }
                return connection;
            } catch (Exception ignored) {
                closeConnection(connection);
                throw new SQLException("CONNECTION_REJECTED");
            }
        }

        public void closeConnection(Connection connection) throws SQLException {
            try {
                if (!connection.isClosed() && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } finally {
                connection.close();
                opened.remove(connection);
            }
        }
        public boolean supportsAggressiveRelease() { return false; }
        public boolean isUnwrappableAs(Class<?> type) { return false; }
        public <T> T unwrap(Class<T> type) { throw new UnsupportedOperationException("REJECTED"); }
        public void close() throws SQLException {
            boolean failed = false;
            for (Connection connection : List.copyOf(opened)) {
                try { closeConnection(connection); } catch (SQLException ignored) { failed = true; }
            }
            if (failed) { throw new SQLException("CLOSE_FAILED"); }
        }
    }

    public static void main(String[] args) {
        PrintStream receipt = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        String status = "INPUT_REJECTED";
        int exit = 2;
        Properties credentials = null;
        StandardServiceRegistry registry = null;
        try {
            LogManager.getLogManager().reset();
            DriverManager.setLogWriter(null);
            DriverManager.setLoginTimeout(5);
            RuntimeJdbcProbe.require(Runtime.version().feature() == 17 && args.length == 2
                    && (args[0].equals("smoke") || args[0].equals("probe")));
            String serverHash = null;
            if (args[0].equals("probe")) {
                try (DataInputStream input = new DataInputStream(System.in)) {
                    RuntimeJdbcProbe.require(input.readInt() == 0x41545331);
                    credentials = RuntimeJdbcProbe.properties(RuntimeJdbcProbe.readField(input, 32));
                    serverHash = RuntimeJdbcProbe.readField(input, 64);
                    RuntimeJdbcProbe.require(serverHash.matches("[0-9a-f]{64}") && input.read() == -1);
                }
            }
            status = "DRIVER_REJECTED";
            Class.forName("com.mysql.cj.jdbc.Driver");
            RuntimeJdbcProbe.require(DriverManager.getDriver(RuntimeJdbcProbe.URL).getMajorVersion() == 9
                    && DriverManager.getDriver(RuntimeJdbcProbe.URL).getMinorVersion() == 7);
            try (ReadOnlyProvider provider = new ReadOnlyProvider(credentials, serverHash)) {
                status = "MAPPING_REJECTED";
                Map<String, Object> settings = settings();
                registry = new StandardServiceRegistryBuilder().clearSettings().applySettings(settings)
                        .addService(ConnectionProvider.class, provider).build();
                Metadata metadata = metadata(registry, Path.of(args[1]));
                if (credentials == null) {
                    status = "MAPPING_READY";
                } else {
                    status = "SESSION_REJECTED";
                    Connection connection = provider.getConnection();
                    status = "SNAPSHOT_REJECTED";
                    String before = snapshot(connection);
                    provider.closeConnection(connection);
                    status = "SCHEMA_REJECTED";
                    validate(metadata, registry, settings);
                    status = "SNAPSHOT_REJECTED";
                    connection = provider.getConnection();
                    RuntimeJdbcProbe.require(before.equals(snapshot(connection)));
                    status = "CLOSE_FAILED";
                    provider.closeConnection(connection);
                    status = "PASS|" + before;
                }
                StandardServiceRegistryBuilder.destroy(registry);
                registry = null;
            }
            exit = 0;
        } catch (Throwable ignored) {
            if (status.startsWith("PASS|") || status.equals("MAPPING_READY")) {
                status = "CLOSE_FAILED";
            }
        } finally {
            if (credentials != null) { credentials.clear(); }
            if (registry != null) {
                try { StandardServiceRegistryBuilder.destroy(registry); }
                catch (Throwable ignored) { status = "CLOSE_FAILED"; exit = 2; }
            }
        }
        receipt.print(status + "\n");
        receipt.flush();
        System.exit(exit);
    }
}
