package com.atstudio.atstudio.entity;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class TrackWaveformSchemaContractTest {

    private static final Path FRESH_SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path MANUAL_PATCH = Path.of(
            "src/main/resources/db/manual/20260715_track_waveform_data.sql");

    @Test
    void entityMapsWaveformDataAsNullableText() throws NoSuchFieldException {
        Field field = Track.class.getDeclaredField("waveformData");
        Column column = field.getAnnotation(Column.class);

        assertThat(column).isNotNull();
        assertThat(column.columnDefinition()).isEqualTo("TEXT");
        assertThat(column.nullable()).isTrue();
    }

    @Test
    void freshSchemaDefinesNullableTextWaveformData() throws IOException {
        String tracks = normalizeSql(tableDefinition(Files.readString(FRESH_SCHEMA), "tracks"));

        assertThat(tracks).contains("duration INT NOT NULL DEFAULT 0");
        assertThat(tracks).contains("waveform_data TEXT NULL");
        assertThat(tracks.indexOf("waveform_data TEXT NULL"))
                .isGreaterThan(tracks.indexOf("duration INT NOT NULL DEFAULT 0"));
        assertThat(tracks.indexOf("user_id BIGINT NOT NULL"))
                .isGreaterThan(tracks.indexOf("waveform_data TEXT NULL"));
    }

    @Test
    void manualPatchIsGuardedAdditiveAndNotAutomatic() throws IOException {
        String patch = Files.readString(MANUAL_PATCH);

        assertThat(patch).contains(
                "This file is NOT auto-run by Spring Boot.",
                "Apply only after separate operator approval.",
                "information_schema.tables",
                "information_schema.columns",
                "SIGNAL SQLSTATE '45000'",
                "ALTER TABLE tracks ADD COLUMN waveform_data TEXT NULL AFTER duration",
                "v_data_type <> 'text'",
                "v_is_nullable <> 'YES'");
        assertThat(patch).doesNotContain(
                "DELETE FROM",
                "INSERT INTO tracks",
                "UPDATE tracks",
                "TRUNCATE TABLE",
                "DROP TABLE");
    }

    private static String tableDefinition(String schema, String tableName) {
        Pattern pattern = Pattern.compile(
                "CREATE TABLE IF NOT EXISTS " + Pattern.quote(tableName)
                        + "\\s*\\((.*?)\\) ENGINE = InnoDB",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(schema);

        assertThat(matcher.find()).as("table %s exists", tableName).isTrue();
        return matcher.group(1);
    }

    private static String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
