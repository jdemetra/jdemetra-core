/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package sql.util;

import internal.util.Strings;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public class SqlTable {

    /**
     * Creates a complete list of tables in a database.
     *
     * @param md
     * @return a non-null list of tables
     * @throws SQLException
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Nonnull
    public static List<SqlTable> allOf(@Nonnull DatabaseMetaData md) throws SQLException {
        return allOf(md, null, null, "%", null);
    }

    /**
     * Creates a partial list of tables in a database by using patterns.
     *
     * @param md
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param types
     * @return a non-null list of tables
     * @throws SQLException
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Nonnull
    public static List<SqlTable> allOf(
            @Nonnull DatabaseMetaData md,
            @Nullable String catalog,
            @Nullable String schemaPattern,
            @Nonnull String tableNamePattern,
            @Nullable String[] types
    ) throws SQLException {
        try (ResultSet rs = md.getTables(catalog, schemaPattern, tableNamePattern, types)) {
            return allOf(rs);
        }
    }

    private static List<SqlTable> allOf(ResultSet rs) throws SQLException {
        // some infos are not supported by all drivers!
        String[] normalizedColumnNames = getNormalizedColumnNames(rs.getMetaData());

        List<SqlTable> result = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        while (rs.next()) {
            for (int i = 0; i < normalizedColumnNames.length; i++) {
                row.put(normalizedColumnNames[i], rs.getString(i + 1));
            }
            result.add(fromMap(row));
        }
        return result;
    }

    private static String[] getNormalizedColumnNames(ResultSetMetaData md) throws SQLException {
        String[] columnNames = new String[md.getColumnCount()];
        for (int i = 0; i < columnNames.length; i++) {
            // normalize to upper case (postgresql driver returns lower case)
            columnNames[i] = md.getColumnName(i + 1).toUpperCase(Locale.ROOT);
        }
        return columnNames;
    }

    @Nonnull
    private static SqlTable fromMap(@Nonnull Map<String, String> map) {
        return new SqlTable(
                get(map, "TABLE_CAT", "TABLE_CATALOG"),
                get(map, "TABLE_SCHEM", "TABLE_SCHEMA"),
                Strings.nullToEmpty(get(map, "TABLE_NAME")),
                Strings.nullToEmpty(get(map, "TABLE_TYPE")),
                get(map, "REMARKS"),
                get(map, "TYPE_CAT"),
                get(map, "TYPE_SCHEM"),
                get(map, "TYPE_NAME"),
                get(map, "SELF_REFERENCING_COL_NAME"),
                get(map, "REF_GENERATION"));
    }

    @Nullable
    private static String get(@Nonnull Map<String, String> map, String... keys) {
        for (String key : keys) {
            String result = map.get(key);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * table catalog (may be <code>null</code>)
     */
    private String catalog;

    /**
     * table schema (may be <code>null</code>)
     */
    private String schema;

    /**
     * table name
     */
    @lombok.NonNull
    private String name;

    /**
     * table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL
     * TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    @Nonnull
    private String type;

    /**
     * explanatory comment on the table
     */
    private String remarks;

    /**
     * the types catalog (may be <code>null</code>)
     */
    private String typesCatalog;

    /**
     * the types schema (may be <code>null</code>)
     */
    private String typesSchema;

    /**
     * type name (may be <code>null</code>)
     */
    private String typeName;

    /**
     * String => name of the designated "identifier" column of a typed table
     * (may be <code>null</code>)
     */
    private String selfReferencingColumnName;

    /**
     * specifies how values in SELF_REFERENCING_COL_NAME are created. Values are
     * "SYSTEM", "USER", "DERIVED". (may be <code>null</code>)
     */
    private String refGeneration;
}
