/*
 * Copyright 2018 National Bank of Belgium
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
package internal.util.sql.odbc.win;

import demetra.design.DirectImpl;
import internal.util.sql.odbc.win.RegCommandWrapper.RegValue;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;
import util.sql.odbc.OdbcDataSource;
import util.sql.odbc.OdbcDriver;
import util.sql.odbc.OdbcRegistrySpi;

/**
 *
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(service = OdbcRegistrySpi.class)
public final class RegOdbcRegistry implements OdbcRegistrySpi {

    @Override
    public String getName() {
        return "OdbcRegistryOverRegCommand";
    }

    @Override
    public boolean isAvailable() {
        return WinOdbcRegistryUtil.isWindows() && isCommandAvailable();
    }

    @Override
    public List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) throws IOException {
        Map<String, List<RegValue>> data = new HashMap<>();
        for (OdbcDataSource.Type o : types) {
            data.putAll(RegCommandWrapper.query(WinOdbcRegistryUtil.getRoot(o) + "\\SOFTWARE\\ODBC\\ODBC.INI"));
        }
        return WinOdbcRegistryUtil.getDataSources(new CachedRegistry(data), types);
    }

    @Override
    public List<OdbcDriver> getDrivers() throws IOException {
        Map<String, List<RegValue>> data = RegCommandWrapper.query("HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\Odbcinst.INI");
        return WinOdbcRegistryUtil.getDrivers(new CachedRegistry(data));
    }

    private static boolean isCommandAvailable() {
        // TODO
        return true;
    }

    @lombok.AllArgsConstructor
    private static class CachedRegistry implements WinOdbcRegistryUtil.Registry {

        @lombok.NonNull
        private final Map<String, List<RegValue>> result;

        @Override
        public boolean keyExists(WinOdbcRegistryUtil.Registry.Root root, String key) throws IOException {
            String target = root + WinOdbcRegistryUtil.KEY_SEPARATOR + key;
            return result.keySet().stream().anyMatch(o -> o.startsWith(target));
        }

        @Override
        public Map<String, Object> getValues(WinOdbcRegistryUtil.Registry.Root root, String key) throws IOException {
            String target = root + WinOdbcRegistryUtil.KEY_SEPARATOR + key;
            return result.containsKey(target)
                    ? result.get(target).stream().collect(Collectors.toMap(RegValue::getName, RegValue::getValue))
                    : Collections.emptySortedMap();
        }
    }
}
