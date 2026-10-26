import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaManagementException;

/** WI009 adaptation of PG007 ActualValidatorHarness; no real driver or sockets. */
public final class RuntimeSchemaValidatorHarness {
    static Metadata model;
    static final Map<String, Table> tables = new TreeMap<>();
    static String scenario;
    static int connections;
    static int closes;
    static int commits;
    static int rollbacks;

    private RuntimeSchemaValidatorHarness() {}

    static Object defaultValue(Class<?> type) {
        if (type == boolean.class) { return false; }
        if (type == int.class) { return 0; }
        if (type == long.class) { return 0L; }
        return null;
    }

    static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    static ResultSet rows(List<Map<String, Object>> values) {
        int[] at = {-1};
        return proxy(ResultSet.class, (instance, method, args) -> {
            if (method.getName().equals("next")) { return ++at[0] < values.size(); }
            if (method.getName().equals("close")) { return null; }
            if (method.getName().equals("getString") || method.getName().equals("getInt")) {
                Object value = values.get(at[0]).get(args[0].toString());
                if (method.getName().equals("getInt")) { return value == null ? 0 : ((Number) value).intValue(); }
                return value == null ? null : value.toString();
            }
            return defaultValue(method.getReturnType());
        });
    }

    static ResultSet scalar(String query) {
        String value = switch (query) {
            case "SELECT @@session.transaction_read_only" -> "1";
            case "SELECT CURRENT_USER()" -> "atstudio_app@127.0.0.1";
            case "SELECT DATABASE()" -> "atstudio_prod";
            case "SELECT SHA2(@@server_uuid,256)" -> "c".repeat(64);
            case "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306" -> "1";
            default -> throw new AssertionError("UNEXPECTED_SQL");
        };
        return rows(List.of(Map.of("1", value)));
    }

    static ResultSet tableRows() {
        List<Map<String, Object>> values = new ArrayList<>();
        if (scenario.equals("missing-table")) { return rows(values); }
        for (String name : tables.keySet()) {
            values.add(Map.of("TABLE_CAT", "atstudio_prod", "TABLE_NAME", name,
                    "TABLE_TYPE", "TABLE", "REMARKS", ""));
        }
        return rows(values);
    }

    static ResultSet columnRows() {
        List<Map<String, Object>> values = new ArrayList<>();
        for (Table table : tables.values()) {
            for (var column : table.getColumns()) {
                if (scenario.equals("missing-column") && column.getName().equals("id")) { continue; }
                Map<String, Object> row = new HashMap<>();
                row.put("TABLE_CAT", "atstudio_prod");
                row.put("TABLE_NAME", table.getName());
                row.put("COLUMN_NAME", column.getName());
                row.put("TYPE_NAME", column.getSqlType(model));
                row.put("DATA_TYPE", column.getSqlTypeCode(model));
                row.put("COLUMN_SIZE", 255);
                row.put("DECIMAL_DIGITS", 2);
                row.put("IS_NULLABLE", column.isNullable() ? "YES" : "NO");
                row.put("NULLABLE", column.isNullable() ? 1 : 0);
                values.add(row);
            }
        }
        return rows(values);
    }

    static Connection connection() {
        boolean[] readOnly = {false};
        boolean[] auto = {true};
        boolean[] closed = {false};
        return proxy(Connection.class, (instance, method, args) -> switch (method.getName()) {
            case "setReadOnly" -> {
                if (!(boolean) args[0]) { throw new AssertionError("READ_ONLY_REMOVED"); }
                readOnly[0] = true;
                yield null;
            }
            case "setAutoCommit" -> { auto[0] = (boolean) args[0]; yield null; }
            case "isReadOnly" -> readOnly[0];
            case "getAutoCommit" -> auto[0];
            case "isClosed" -> closed[0];
            case "close" -> { closes++; closed[0] = true; yield null; }
            case "commit" -> {
                if (!readOnly[0]) { throw new AssertionError("WRITABLE_COMMIT"); }
                commits++;
                yield null;
            }
            case "rollback" -> { rollbacks++; yield null; }
            case "equals" -> instance == args[0];
            case "hashCode" -> System.identityHashCode(instance);
            case "getCatalog" -> "atstudio_prod";
            case "getSchema" -> null;
            case "createStatement" -> proxy(Statement.class, (statement, operation, values) -> {
                if (operation.getName().equals("executeQuery")) {
                    if (!readOnly[0]) { throw new AssertionError("READ_BEFORE_GUARD"); }
                    return scalar((String) values[0]);
                }
                if (operation.getName().startsWith("execute")) { throw new AssertionError("WRITE_SQL"); }
                return defaultValue(operation.getReturnType());
            });
            case "getMetaData" -> proxy(DatabaseMetaData.class, (metadata, operation, values) -> {
                if (!readOnly[0]) { throw new AssertionError("METADATA_BEFORE_GUARD"); }
                return switch (operation.getName()) {
                    case "getTables" -> tableRows();
                    case "getColumns" -> columnRows();
                    case "getCatalogs" -> rows(List.of(Map.of("TABLE_CAT", "atstudio_prod")));
                    case "getSchemas", "getImportedKeys", "getIndexInfo", "getPrimaryKeys" -> rows(List.of());
                    case "getDatabaseProductName" -> "MySQL";
                    case "getDatabaseProductVersion" -> "8.0.46";
                    case "getDatabaseMajorVersion" -> 8;
                    case "getDatabaseMinorVersion" -> 0;
                    case "storesLowerCaseIdentifiers" -> true;
                    case "getSearchStringEscape" -> "\\";
                    default -> defaultValue(operation.getReturnType());
                };
            });
            default -> throw new AssertionError("UNEXPECTED_CONNECTION");
        });
    }

    static void run(Path entities) throws Exception {
        if (DriverManager.getDrivers().hasMoreElements()) { throw new AssertionError("REAL_DRIVER_PRESENT"); }
        Driver fake = new Driver() {
            public Connection connect(String url, Properties properties) {
                if (!url.equals(RuntimeJdbcProbe.URL)) { throw new AssertionError("URL"); }
                connections++;
                return connection();
            }
            public boolean acceptsURL(String url) { return url.equals(RuntimeJdbcProbe.URL); }
            public int getMajorVersion() { return 9; }
            public int getMinorVersion() { return 7; }
            public boolean jdbcCompliant() { return false; }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) { return new DriverPropertyInfo[0]; }
            public Logger getParentLogger() { return Logger.getAnonymousLogger(); }
        };
        DriverManager.registerDriver(fake);
        try {
            for (String name : List.of("match", "missing-table", "missing-column")) {
                scenario = name;
                connections = closes = commits = rollbacks = 0;
                try (var provider = new RuntimeSchemaProbe.ReadOnlyProvider(
                        RuntimeJdbcProbe.properties("SYNTHETIC-NOT-SECRET-xxxxxxxxxxx"), "c".repeat(64))) {
                    var settings = RuntimeSchemaProbe.settings();
                    var registry = new StandardServiceRegistryBuilder().clearSettings().applySettings(settings)
                            .addService(ConnectionProvider.class, provider).build();
                    try {
                        model = RuntimeSchemaProbe.metadata(registry, entities);
                        tables.clear();
                        for (var namespace : model.getDatabase().getNamespaces()) {
                            for (var table : namespace.getTables()) { tables.put(table.getName(), table); }
                        }
                        try {
                            RuntimeSchemaProbe.validate(model, registry, settings);
                            if (!name.equals("match")) { throw new AssertionError("INVALID_SCHEMA_ACCEPTED"); }
                        } catch (SchemaManagementException error) {
                            String expected = name.equals("missing-table")
                                    ? "Schema validation: missing table" : "Schema validation: missing column";
                            if (name.equals("match") || error.getMessage() == null
                                    || !error.getMessage().startsWith(expected)) {
                                throw new AssertionError("UNEXPECTED_SCHEMA_REJECTION");
                            }
                        }
                    } finally {
                        StandardServiceRegistryBuilder.destroy(registry);
                    }
                }
                if (connections < 1 || connections != closes || commits < 1 || rollbacks < 1) {
                    throw new AssertionError("TRANSACTION_COUNTS");
                }
            }
        } finally {
            DriverManager.deregisterDriver(fake);
        }
    }

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        String status = "ACTUAL_VALIDATOR_REJECTED";
        int code = 2;
        try {
            if (args.length != 1) { throw new AssertionError("INPUT"); }
            run(Path.of(args[0]));
            status = "ACTUAL_VALIDATOR_SYNTHETIC_PASS";
            code = 0;
        } catch (ClassCastException ignored) {
            if (connections == 0) { status = "ACTUAL_VALIDATOR_TYPE_REJECTED"; }
        } catch (Throwable ignored) {
            // 합성 시험도 원시 예외와 메타데이터를 출력하지 않는다.
        }
        System.out.print(status + "\n");
        System.exit(code);
    }
}
