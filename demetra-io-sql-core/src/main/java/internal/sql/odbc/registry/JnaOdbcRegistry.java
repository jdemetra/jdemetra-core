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
package internal.sql.odbc.registry;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import ec.tstoolkit.design.IntValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author Philippe Charles
 */
@Deprecated
public class JnaOdbcRegistry implements IOdbcRegistry {

    public JnaOdbcRegistry() {
    }

    @Override
    public List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) {
        if (types.length == 0) {
            return Collections.emptyList();
        }
        List<OdbcDataSource> result = new ArrayList<>();
        for (OdbcDataSource.Type type : types) {
            WinReg.HKEY root = getOdbcRootKey(type);
            if (!Advapi32Util.registryKeyExists(root, "SOFTWARE\\ODBC\\ODBC.INI\\ODBC Data Sources")) {
                break;
            }
            for (Entry<String, Object> ds : Advapi32Util.registryGetValues(root, "SOFTWARE\\ODBC\\ODBC.INI\\ODBC Data Sources").entrySet()) {
                String dsKey = "SOFTWARE\\ODBC\\ODBC.INI\\" + ds.getKey();
                if (Advapi32Util.registryKeyExists(root, dsKey)) {
                    TreeMap<String, Object> details = Advapi32Util.registryGetValues(root, dsKey);
                    result.add(new OdbcDataSource(type, ds.getKey(),
                            toString(details.get("Description")),
                            toString(ds.getValue()),
                            toString(details.get("Driver")),
                            toString(details.get("Server"))));
                }
            }
        }
        return result;
    }

    private WinReg.HKEY getOdbcRootKey(OdbcDataSource.Type type) {
        switch (type) {
            case SYSTEM:
                return WinReg.HKEY_LOCAL_MACHINE;
            case USER:
                return WinReg.HKEY_CURRENT_USER;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<OdbcDriver> getDrivers() {
        WinReg.HKEY root = WinReg.HKEY_LOCAL_MACHINE;
        if (!Advapi32Util.registryKeyExists(root, "SOFTWARE\\ODBC\\Odbcinst.INI\\ODBC Drivers")) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<OdbcDriver> result = ImmutableList.builder();
        for (Entry<String, Object> o : Advapi32Util.registryGetValues(root, "SOFTWARE\\ODBC\\Odbcinst.INI\\ODBC Drivers").entrySet()) {
            String driverKey = "SOFTWARE\\ODBC\\Odbcinst.INI\\" + o.getKey();
            if (Advapi32Util.registryKeyExists(root, driverKey)) {
                TreeMap<String, Object> details = Advapi32Util.registryGetValues(root, driverKey);
                result.add(
                        new OdbcDriver(o.getKey(),
                                readEnum(details.get("APILevel"), OdbcDriver.ApiLevel.class, OdbcDriver.ApiLevel.NONE),
                                OdbcDriver.ConnectFunctions.valueOf(toString(details.get("ConnectFunctions"))),
                                toString(details.get("Driver")),
                                toString(details.get("DriverOdbcVer")),
                                readFileExtns(details.get("FileExtns")),
                                readEnum(details.get("FileUsage"), OdbcDriver.FileUsage.class, OdbcDriver.FileUsage.NONE),
                                toString(details.get("Setup")),
                                readEnum(details.get("SQLLevel"), OdbcDriver.SqlLevel.class, OdbcDriver.SqlLevel.SQL_92_ENTRY),
                                readDwordAsInt(details.get("UsageCount"))));
            }
        }
        return result.build();
    }

    private static String toString(Object o) {
        return o != null ? o.toString() : null;
    }

    static <Z extends Enum<Z> & IntValue> Z readEnum(Object obj, Class<Z> clazz, Z defaultValue) {
        String input = toString(obj);
        if (input == null) {
            return defaultValue;
        }
        int value = Integer.parseInt(input);
        for (Z o : clazz.getEnumConstants()) {
            if (o.intValue() == value) {
                return o;
            }
        }
        return defaultValue;
    }

    static ImmutableList<String> readFileExtns(Object obj) {
        String input = toString(obj);
        if (input == null) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String o : EXTNS_SPLITTER.split(input)) {
            builder.add(Files.getFileExtension(o));
        }
        return builder.build();
    }

    static final Splitter EXTNS_SPLITTER = Splitter.on(',').omitEmptyStrings();

    static int readValueAsInt(Object obj) {
        return Integer.parseInt(toString(obj));
    }

    // FIXME: really slow
    static int readDwordAsInt(Object obj) {
        return -1;
//        try {
//            return Integer.decode(regor.readDword(key, name)).intValue();
//        } catch (RegistryErrorException ex) {
//            return -1;
//        }
    }
}
