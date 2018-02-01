/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.sql.adodb;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class AdoContext {

    @Nonnull
    static AdoContext of(@Nonnull Wsh wsh, @Nonnull String connectionString) {
        Objects.requireNonNull(wsh);
        Objects.requireNonNull(connectionString);
        PropertyLoader loader = () -> loadAll(wsh, connectionString, CURRENT_CATALOG, SPECIAL_CHARACTERS, IDENTIFIER_CASE_SENSITIVITY, STRING_FUNCTIONS);
        return new AdoContext(wsh, connectionString, loader.memoizeWithExpiration(10, TimeUnit.MINUTES, System::nanoTime));
    }

    // https://msdn.microsoft.com/en-us/library/ms676695%28v=vs.85%29.aspx
    static final String CURRENT_CATALOG = "Current Catalog";
    static final String SPECIAL_CHARACTERS = "Special Characters";
    static final String IDENTIFIER_CASE_SENSITIVITY = "Identifier Case Sensitivity";
    static final String STRING_FUNCTIONS = "String Functions";

    private final Wsh wsh;
    private final String connectionString;
    private final PropertyLoader propertyLoader;

    private AdoContext(Wsh wsh, String connectionString, PropertyLoader propertyLoader) {
        this.wsh = wsh;
        this.connectionString = connectionString;
        this.propertyLoader = propertyLoader;
    }

    @Nonnull
    String getConnectionString() {
        return connectionString;
    }

    @Nullable
    String getProperty(@Nonnull String name) throws IOException {
        return propertyLoader.load(name);
    }

    @Nullable
    IdentifierCaseType getIdentifierCaseType() throws IOException {
        String property = getProperty(IDENTIFIER_CASE_SENSITIVITY);
        if (property != null) {
            try {
                int value = Integer.parseInt(property);
                return Arrays.stream(IdentifierCaseType.values())
                        .filter(o -> o.value == value)
                        .findFirst()
                        .orElse(null);
            } catch (NumberFormatException ex) {
                throw new IOException("Cannot parse identifier case type", ex);
            }
        }
        return null;
    }

    @Nonnull
    Stream<SqlStringFunction> getStringFunctions() throws IOException {
        try {
            String property = getProperty(STRING_FUNCTIONS);
            if (property != null) {
                int value = Integer.parseInt(property);
                return Arrays.stream(SqlStringFunction.values()).filter(o -> o.isBitmaskSet(value));
            }
            return Stream.empty();
        } catch (NumberFormatException ex) {
            throw new IOException("Cannot parse string functions bitmask", ex);
        }
    }

    @Nonnull
    TsvReader preparedStatement(@Nonnull String sql, @Nonnull List<String> parameters) throws IOException {
        String[] args = new String[2 + parameters.size()];
        args[0] = connectionString;
        args[1] = sql;
        for (int i = 0; i < parameters.size(); i++) {
            args[2 + i] = parameters.get(i);
        }
        return TsvReader.of(wsh.exec("PreparedStatement", args), 2);
    }

    @Nonnull
    TsvReader openSchema(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws IOException {
        String[] args = new String[4 + (types != null ? types.length : 0)];
        args[0] = connectionString;
        args[1] = catalog != null ? catalog : "\"\"";
        args[2] = schemaPattern != null && !schemaPattern.equals("%") ? schemaPattern : "\"\"";
        args[3] = tableNamePattern != null && !tableNamePattern.equals("%") ? tableNamePattern : "\"\"";
        if (types != null) {
            System.arraycopy(types, 0, args, 4, types.length);
        }
        return TsvReader.of(wsh.exec("OpenSchema", args), 2);
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static Map<String, String> loadAll(Wsh wsh, String connectionString, String... properties) throws IOException {
        String[] args = new String[properties.length + 1];
        args[0] = connectionString;
        System.arraycopy(properties, 0, args, 1, properties.length);
        try (TsvReader reader = TsvReader.of(wsh.exec("DbProperties", args), 2)) {
            Map<String, String> result = new HashMap<>();
            String[] array = new String[reader.getHeader(0).length];
            while (reader.readNextInto(array)) {
                result.put(array[0], array[1]);
            }
            return result;
        }
    }
    //</editor-fold>

    enum IdentifierCaseType {
        LOWER(2),
        MIXED(8),
        SENSITIVE(4),
        UPPER(1);

        private final int value;

        private IdentifierCaseType(int value) {
            this.value = value;
        }
    }

    //https://msdn.microsoft.com/en-us/library/ms710249(v=vs.85).aspx
    enum SqlStringFunction {
        SQL_FN_STR_CONCAT(0x00000001, "CONCAT"),
        SQL_FN_STR_INSERT(0x00000002, "INSERT"),
        SQL_FN_STR_LEFT(0x00000004, "LEFT"),
        SQL_FN_STR_LTRIM(0x00000008, "LTRIM"),
        SQL_FN_STR_LENGTH(0x00000010, "LENGTH"),
        SQL_FN_STR_LOCATE(0x00000020, "LOCATE"),
        SQL_FN_STR_LCASE(0x00000040, "LCASE"),
        SQL_FN_STR_REPEAT(0x00000080, "REPEAT"),
        SQL_FN_STR_REPLACE(0x00000100, "REPLACE"),
        SQL_FN_STR_RIGHT(0x00000200, "RIGHT"),
        SQL_FN_STR_RTRIM(0x00000400, "RTRIM"),
        SQL_FN_STR_SUBSTRING(0x00000800, "SUBSTRING"),
        SQL_FN_STR_UCASE(0x00001000, "UCASE"),
        SQL_FN_STR_ASCII(0x00002000, "ASCII"),
        SQL_FN_STR_CHAR(0x00004000, "CHAR"),
        SQL_FN_STR_DIFFERENCE(0x00008000, "DIFFERENCE"),
        SQL_FN_STR_LOCATE_2(0x00010000, "LOCATE_2"),
        SQL_FN_STR_SOUNDEX(0x00020000, "SOUNDEX"),
        SQL_FN_STR_SPACE(0x00040000, "SPACE"),
        SQL_FN_STR_BIT_LENGTH(0x00080000, "BIT_LENGTH"),
        SQL_FN_STR_CHAR_LENGTH(0x00100000, "CHAR_LENGTH"),
        SQL_FN_STR_CHARACTER_LENGTH(0x00200000, "CHARACTER_LENGTH"),
        SQL_FN_STR_OCTET_LENGTH(0x00400000, "OCTET_LENGTH"),
        SQL_FN_STR_POSITION(0x00800000, "POSITION");

        private final int bitmask;
        private final String label;

        private SqlStringFunction(int bitmask, String label) {
            this.bitmask = bitmask;
            this.label = label;
        }

        boolean isBitmaskSet(int input) {
            return (input & bitmask) == bitmask;
        }

        String getLabel() {
            return label;
        }
    }
}
