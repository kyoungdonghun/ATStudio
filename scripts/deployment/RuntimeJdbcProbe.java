import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

/** WI029 standalone read-only probe. The Python wrapper owns approval and secret input. */
public final class RuntimeJdbcProbe {
    static final String URL = "jdbc:mysql://127.0.0.1:3306/atstudio_prod?serverTimezone=Asia/Seoul"
            + "&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
    static final List<String> TABLES = List.of(
            "admin_operation_audit_logs", "admin_subscription_corrections", "album_likes",
            "album_tracks", "albums", "answers", "billing_agreements",
            "company_certification_audit_logs", "company_certification_documents",
            "company_certifications", "email_verification_tokens", "licenses", "likes",
            "notice_attachments", "notices", "password_reset_tokens",
            "payment_entitlement_corrections", "payment_operation_audit_logs", "payment_orders",
            "payment_receipts", "payment_reconciliation_incidents", "payment_refunds",
            "payment_settlement_import_attempts", "payment_settlements", "playlist_tracks",
            "playlists", "question_attachments", "questions", "site_settings", "social_accounts",
            "storage_mutations", "subscription_payments", "subscriptions", "tags",
            "track_downloads", "track_tags", "tracks", "user_consents", "user_subscriptions",
            "users", "whitelist_channels", "whitelist_export_batches", "whitelist_export_items");

    private RuntimeJdbcProbe() {}

    static void require(boolean condition) throws IOException {
        if (!condition) {
            throw new IOException("REJECTED");
        }
    }

    static String readField(DataInputStream input, int length) throws IOException {
        require(input.readInt() == length);
        byte[] bytes = input.readNBytes(length);
        require(bytes.length == length);
        try {
            for (byte value : bytes) {
                require(value >= 33 && value <= 126);
            }
            return new String(bytes, StandardCharsets.US_ASCII);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    static Properties properties(String password) {
        Properties values = new Properties();
        values.setProperty("user", "atstudio_app");
        values.setProperty("password", password);
        values.setProperty("connectTimeout", "5000");
        values.setProperty("socketTimeout", "5000");
        values.setProperty("queryTimeoutKillsConnection", "true");
        values.setProperty("autoReconnect", "false");
        values.setProperty("readOnlyPropagatesToServer", "true");
        values.setProperty("useLocalSessionState", "false");
        values.setProperty("logger", "com.mysql.cj.log.NullLogger");
        values.setProperty("profileSQL", "false");
        values.setProperty("traceProtocol", "false");
        values.setProperty("enablePacketDebug", "false");
        values.setProperty("dumpQueriesOnException", "false");
        values.setProperty("autoGenerateTestcaseScript", "false");
        values.setProperty("logSlowQueries", "false");
        values.setProperty("gatherPerfMetrics", "false");
        values.setProperty("openTelemetry", "DISABLED");
        return values;
    }

    static String scalar(Statement statement, String query) throws Exception {
        require(query.startsWith("SELECT "));
        try (ResultSet result = statement.executeQuery(query)) {
            require(result.next());
            String value = result.getString(1);
            require(value != null && !result.next());
            return value;
        }
    }

    public static void main(String[] args) {
        PrintStream receipt = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        String status = "INPUT_REJECTED";
        int exit = 2;
        Properties credentials = null;
        try {
            LogManager.getLogManager().reset();
            DriverManager.setLogWriter(null);
            DriverManager.setLoginTimeout(5);
            require(Runtime.version().feature() == 17);
            require(args.length == 1 && (args[0].equals("smoke") || args[0].equals("probe")));
            String password = null;
            String serverHash = null;
            if (args[0].equals("probe")) {
                try (DataInputStream input = new DataInputStream(System.in)) {
                    require(input.readInt() == 0x41545331);
                    password = readField(input, 32);
                    serverHash = readField(input, 64);
                    require(serverHash.matches("[0-9a-f]{64}") && input.read() == -1);
                }
            }
            status = "DRIVER_REJECTED";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Driver driver = DriverManager.getDriver(URL);
            require(driver.getClass().getName().equals("com.mysql.cj.jdbc.Driver")
                    && driver.getMajorVersion() == 9 && driver.getMinorVersion() == 7);
            if (args[0].equals("smoke")) {
                status = "DRIVER_LOADED";
            } else {
                credentials = properties(password);
                password = null;
                status = "JDBC_CONNECT_FAILED";
                try (Connection connection = DriverManager.getConnection(URL, credentials)) {
                    credentials.clear();
                    status = "SESSION_REJECTED";
                    connection.setReadOnly(true);
                    connection.setAutoCommit(false);
                    require(connection.isReadOnly() && !connection.getAutoCommit());
                    try (Statement statement = connection.createStatement()) {
                        statement.setQueryTimeout(3);
                        statement.setMaxRows(44);
                        require("1".equals(scalar(statement, "SELECT @@session.transaction_read_only")));
                        status = "IDENTITY_REJECTED";
                        require("atstudio_app@127.0.0.1".equals(scalar(statement, "SELECT CURRENT_USER()")));
                        require("atstudio_prod".equals(scalar(statement, "SELECT DATABASE()")));
                        require(serverHash.equals(scalar(statement, "SELECT SHA2(@@server_uuid,256)")));
                        require("1".equals(scalar(statement,
                                "SELECT VERSION() REGEXP '^8[.]0[.]46(-|$)' AND @@port=3306")));
                        status = "TABLES_REJECTED";
                        try (ResultSet rows = statement.executeQuery(
                                "SELECT TABLE_NAME FROM information_schema.TABLES "
                                + "WHERE TABLE_SCHEMA='atstudio_prod' AND TABLE_TYPE='BASE TABLE' "
                                + "ORDER BY TABLE_NAME")) {
                            for (String table : TABLES) {
                                require(rows.next() && table.equals(rows.getString(1)));
                            }
                            require(!rows.next());
                        }
                        status = "ROWS_REJECTED";
                        for (String table : TABLES) {
                            String expected = table.equals("subscriptions") ? "6" : "0";
                            require(expected.equals(scalar(statement,
                                    "SELECT COUNT(*) FROM `atstudio_prod`.`" + table + "`")));
                        }
                        require("1".equals(scalar(statement,
                                "SELECT COUNT(*)=6 AND COUNT(DISTINCT name,user_type)=6 "
                                + "AND SUM(name IN ('STANDARD','DELUXE','PREMIUM') "
                                + "AND user_type IN ('INDIVIDUAL','BUSINESS') AND is_active=1)=6 "
                                + "FROM atstudio_prod.subscriptions")));
                    }
                    status = "CLOSE_FAILED";
                    connection.rollback();
                }
                status = "PASS";
            }
            exit = 0;
        } catch (Throwable ignored) {
            // Raw JDBC exceptions, suppressed exceptions and causes never reach output.
        } finally {
            if (credentials != null) {
                credentials.clear();
            }
        }
        receipt.print(status + "\n");
        receipt.flush();
        System.exit(exit);
    }
}
