import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaValidator;

/** Public synthetic fixtures only. No Connector/J registration and no socket access. */
public final class RuntimeSchemaProbeHarness {
    private RuntimeSchemaProbeHarness() {}
    static boolean readOnly;
    static boolean autoCommit = true;
    static boolean closed;
    static String scenario;
    static int connections;
    static int rollbacks;

    static Object defaultValue(Class<?> type) {
        if (type == boolean.class) { return false; }
        if (type == int.class) { return 0; }
        if (type == long.class) { return 0L; }
        return null;
    }

    static ResultSet rows(String query) {
        for (int index = 0; index < RuntimeSchemaProbe.QUERIES.length; index++) {
            if (query.equals(RuntimeSchemaProbe.QUERIES[index])) { return metadataRows(index); }
        }
        String value = switch (query) {
            case "SELECT @@session.transaction_read_only" -> scenario.equals("readonly") ? "0" : "1";
            case "SELECT CURRENT_USER()" -> scenario.equals("account") ? "wrong" : "atstudio_app@127.0.0.1";
            case "SELECT DATABASE()" -> scenario.equals("database") ? "wrong" : "atstudio_prod";
            case "SELECT SHA2(@@server_uuid,256)" -> scenario.equals("server") ? "d".repeat(64) : "c".repeat(64);
            case "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306" -> scenario.equals("version") ? "0" : "1";
            default -> throw new AssertionError("UNEXPECTED_QUERY");
        };
        int[] calls = {0};
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> ++calls[0] == 1;
                    case "getString" -> value;
                    default -> defaultValue(method.getReturnType());
                });
    }

    static ResultSet metadataRows(int query) {
        int[] row = {0};
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("next")) {
                        int limit = query == 0 ? 43 : scenario.equals("overlimit") ? 4097 : 1;
                        return ++row[0] <= limit;
                    }
                    if (method.getName().equals("getString")) {
                        int column = (int) args[0];
                        if (query == 0 && column == 1) {
                            return scenario.equals("table") ? "wrong" : RuntimeJdbcProbe.TABLES.get(row[0] - 1);
                        }
                        if (query == 0 && column == 2) { return "BASE TABLE"; }
                        return switch (scenario) {
                            case "null" -> null;
                            case "empty" -> "";
                            case "long" -> "x".repeat(8193);
                            default -> "synthetic";
                        };
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    static Connection connection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setReadOnly" -> { readOnly = (boolean) args[0]; yield null; }
                    case "setAutoCommit" -> { autoCommit = (boolean) args[0]; yield null; }
                    case "isReadOnly" -> readOnly;
                    case "getAutoCommit" -> autoCommit;
                    case "isClosed" -> closed;
                    case "close" -> { closed = true; yield null; }
                    case "rollback" -> { rollbacks++; yield null; }
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "createStatement" -> Proxy.newProxyInstance(Statement.class.getClassLoader(),
                            new Class<?>[]{Statement.class}, (statement, operation, values) -> {
                                if (operation.getName().equals("executeQuery")) {
                                    if (!readOnly || autoCommit) { throw new AssertionError("SESSION_ORDER"); }
                                    return rows((String) values[0]);
                                }
                                if (operation.getName().startsWith("execute")) {
                                    throw new AssertionError("WRITE_ATTEMPT");
                                }
                                return defaultValue(operation.getReturnType());
                            });
                    default -> defaultValue(method.getReturnType());
                });
    }

    public static void main(String[] args) throws Exception {
        try (var provider = new RuntimeSchemaProbe.ReadOnlyProvider(null, null)) {
            var registry = new StandardServiceRegistryBuilder().clearSettings()
                    .applySettings(RuntimeSchemaProbe.settings()).addService(ConnectionProvider.class, provider).build();
            try {
                RuntimeSchemaProbe.metadata(registry, Path.of(args[0]));
            } finally {
                StandardServiceRegistryBuilder.destroy(registry);
            }
        }
        Driver fake = new Driver() {
            public Connection connect(String url, Properties props) throws SQLException {
                connections++;
                if (!url.equals(RuntimeJdbcProbe.URL)) { throw new AssertionError("URL"); }
                if (scenario.equals("connect")) { throw new SQLException("SYNTHETIC-SENTINEL"); }
                return connection();
            }
            public boolean acceptsURL(String url) { return true; }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties props) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 9; }
            public int getMinorVersion() { return 7; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getAnonymousLogger(); }
        };
        DriverManager.registerDriver(fake);
        for (String name : new String[]{"pass", "readonly", "account", "database", "server", "version", "connect"}) {
            scenario = name;
            readOnly = false;
            autoCommit = true;
            closed = false;
            try (var provider = new RuntimeSchemaProbe.ReadOnlyProvider(
                    RuntimeJdbcProbe.properties("SYNTHETIC-NOT-SECRET-xxxxxxxxxxx"), "c".repeat(64))) {
                try {
                    Connection connection = provider.getConnection();
                    if (!name.equals("pass")) { throw new AssertionError("ACCEPTED_BAD_IDENTITY"); }
                    provider.closeConnection(connection);
                } catch (SQLException expected) {
                    if (name.equals("pass")) { throw new AssertionError("REJECTED_GOOD_IDENTITY"); }
                }
            }
            if (!name.equals("connect") && !closed) { throw new AssertionError("LEAKED_CONNECTION"); }
        }
        DriverManager.deregisterDriver(fake);
        if (connections != 7 || rollbacks != 6) { throw new AssertionError("COUNTS"); }
        readOnly = true;
        autoCommit = false;
        scenario = "null";
        String first = RuntimeSchemaProbe.snapshot(connection());
        if (!first.equals(RuntimeSchemaProbe.snapshot(connection())) || !first.startsWith("43|1|1|1|")) {
            throw new AssertionError("SNAPSHOT_STABILITY");
        }
        scenario = "empty";
        if (first.equals(RuntimeSchemaProbe.snapshot(connection()))) { throw new AssertionError("NULL_ENCODING"); }
        for (String bad : new String[]{"table", "long", "overlimit"}) {
            scenario = bad;
            try {
                RuntimeSchemaProbe.snapshot(connection());
                throw new AssertionError("SNAPSHOT_BOUND");
            } catch (java.io.IOException expected) { /* Synthetic invalid metadata. */ }
        }
        int[] validationCalls = {0};
        SchemaValidator validator = (metadata, options, matcher) -> {
            if (options.shouldManageNamespaces()
                    || !"none".equals(options.getConfigurationValues().get("hibernate.hbm2ddl.auto"))) {
                throw new AssertionError("SCHEMA_ACTION");
            }
            validationCalls[0]++;
        };
        SchemaManagementTool tool = (SchemaManagementTool) Proxy.newProxyInstance(
                SchemaManagementTool.class.getClassLoader(), new Class<?>[]{SchemaManagementTool.class},
                (proxy, method, values) -> {
                    if (!method.getName().equals("getSchemaValidator")) { throw new AssertionError("UNSAFE_SCHEMA_TOOL"); }
                    return validator;
                });
        StandardServiceRegistry registry = (StandardServiceRegistry) Proxy.newProxyInstance(
                StandardServiceRegistry.class.getClassLoader(), new Class<?>[]{StandardServiceRegistry.class},
                (proxy, method, values) -> {
                    if (!method.getName().equals("getService") || values[0] != SchemaManagementTool.class) {
                        throw new AssertionError("UNEXPECTED_SERVICE");
                    }
                    return tool;
                });
        RuntimeSchemaProbe.validate(null, registry, RuntimeSchemaProbe.settings());
        if (validationCalls[0] != 1) { throw new AssertionError("VALIDATION_CALLS"); }
        System.out.print("SYNTHETIC_PASS\n");
    }
}
