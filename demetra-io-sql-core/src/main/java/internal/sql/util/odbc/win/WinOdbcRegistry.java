/*
 * Copyright 2013 National Bank of Belgium
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
package internal.sql.util.odbc.win;

import sql.util.odbc.OdbcDataSource;
import static sql.util.odbc.OdbcDataSource.Type.SYSTEM;
import static sql.util.odbc.OdbcDataSource.Type.USER;
import sql.util.odbc.OdbcDriver;
import sql.util.odbc.OdbcRegistry;
import internal.util.Strings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class WinOdbcRegistry implements OdbcRegistry {

    public interface Accessor {

        static enum Root {

            HKEY_LOCAL_MACHINE, HKEY_CURRENT_USER
        }

        boolean keyExists(@Nonnull Root root, @Nonnull String key) throws IOException;

        @Nullable
        Object getValue(@Nonnull Root root, @Nonnull String key, @Nonnull String name) throws IOException;

        @Nonnull
        SortedMap<String, Object> getValues(@Nonnull Root root, @Nonnull String key) throws IOException;
    }

    private static final String DATA_SOURCES_KEY = "SOFTWARE\\ODBC\\ODBC.INI\\ODBC Data Sources";
    private static final String DATA_SOURCE_KEY = "SOFTWARE\\ODBC\\ODBC.INI";
    private static final String DRIVERS_KEY = "SOFTWARE\\ODBC\\Odbcinst.INI\\ODBC Drivers";
    private static final String DRIVER_KEY = "SOFTWARE\\ODBC\\Odbcinst.INI";
    private static final String KEY_SEPARATOR = "\\";

    @lombok.NonNull
    private final Accessor reg;

    @Override
    public List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) throws IOException {
        List<OdbcDataSource> result = new ArrayList<>();
        for (OdbcDataSource.Type o : types) {
            forEachDataSource(o, result::add);
        }
        return result;
    }

    private void forEachDataSource(OdbcDataSource.Type type, Consumer<OdbcDataSource> consumer) throws IOException {
        Accessor.Root root = getRoot(type);
        if (reg.keyExists(root, DATA_SOURCES_KEY)) {
            for (Entry<String, Object> master : reg.getValues(root, DATA_SOURCES_KEY).entrySet()) {
                String dataSourceKey = DATA_SOURCE_KEY + KEY_SEPARATOR + master.getKey();
                if (reg.keyExists(root, dataSourceKey)) {
                    consumer.accept(dataSourceOf(type, master, reg.getValues(root, dataSourceKey)));
                }
            }
        }
    }

    private static OdbcDataSource dataSourceOf(OdbcDataSource.Type type, Entry<String, Object> master, SortedMap<String, Object> details) {
        return OdbcDataSource
                .builder()
                .type(type)
                .name(master.getKey())
                .description(toString(details.get("Description"), null))
                .driverName(toString(master.getValue(), null))
                .driverPath(toFile(details.get("Driver"), null))
                .serverName(toString(details.get("Server"), null))
                .build();
    }

    private static Accessor.Root getRoot(OdbcDataSource.Type type) {
        switch (type) {
            case SYSTEM:
                return Accessor.Root.HKEY_LOCAL_MACHINE;
            case USER:
                return Accessor.Root.HKEY_CURRENT_USER;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<OdbcDriver> getDrivers() throws IOException {
        List<OdbcDriver> result = new ArrayList<>();
        Accessor.Root localMachine = Accessor.Root.HKEY_LOCAL_MACHINE;
        if (reg.keyExists(localMachine, DRIVERS_KEY)) {
            for (String name : reg.getValues(localMachine, DRIVERS_KEY).keySet()) {
                String driverKey = DRIVER_KEY + KEY_SEPARATOR + name;
                if (reg.keyExists(localMachine, driverKey)) {
                    result.add(driverOf(name, reg.getValues(localMachine, driverKey)));
                }
            }
        }
        return result;
    }

    private static OdbcDriver driverOf(String name, SortedMap<String, Object> details) {
        return OdbcDriver
                .builder()
                .name(name)
                .apiLevel(toEnum(details.get("APILevel"), OdbcDriver.ApiLevel.class, OdbcDriver.ApiLevel.NONE))
                .connectFunctions(toConnectFunctions(details.get("ConnectFunctions"), null))
                .driverPath(toFile(details.get("Driver"), null))
                .driverOdbcVer(toString(details.get("DriverOdbcVer"), null))
                .fileExtensions(toFileExtensions(details.get("FileExtns")))
                .fileUsage(toEnum(details.get("FileUsage"), OdbcDriver.FileUsage.class, OdbcDriver.FileUsage.NONE))
                .setupPath(toFile(details.get("Setup"), null))
                .sqlLevel(toEnum(details.get("SQLLevel"), OdbcDriver.SqlLevel.class, OdbcDriver.SqlLevel.SQL_92_ENTRY))
                .usageCount(toInt(details.get("UsageCount"), -1))
                .build();
    }

    private static String toString(Object obj, String defaultValue) {
        return obj instanceof String ? (String) obj : defaultValue;
    }

    private static File toFile(Object obj, File defaultValue) {
        return obj instanceof String ? new File((String) obj) : defaultValue;
    }

    private static int toInt(Object obj, int defaultValue) {
        return obj instanceof Integer ? (Integer) obj : defaultValue;
    }

    private static <Z extends Enum<Z> & IntSupplier> Z toEnum(Object obj, Class<Z> enumType, Z defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(obj.toString());
            return toEnum(value, enumType, defaultValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static <Z extends Enum<Z> & IntSupplier> Z toEnum(int value, Class<Z> enumType, Z defaultValue) {
        for (Z o : enumType.getEnumConstants()) {
            if (o.getAsInt() == value) {
                return o;
            }
        }
        return defaultValue;
    }

    private static List<String> toFileExtensions(Object obj) {
        return obj != null
                ? Strings
                .splitToStream(",", obj.toString())
                .map(WinOdbcRegistry::getFileExtension)
                .filter(o -> !o.isEmpty())
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    private static String getFileExtension(String input) {
        int index = input.lastIndexOf('.');
        return index != -1 ? input.substring(index + 1) : "";
    }

    private static OdbcDriver.ConnectFunctions toConnectFunctions(Object obj, OdbcDriver.ConnectFunctions defaultValue) {
        return obj != null
                ? OdbcDriver.ConnectFunctions.parse(obj.toString(), defaultValue)
                : defaultValue;
    }
}
