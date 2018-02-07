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
package util.sql.odbc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import javax.annotation.Nonnull;

/**
 *
 * @see
 * http://msdn.microsoft.com/en-us/library/windows/desktop/ms715432(v=vs.85).aspx
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor(staticName = "of")
public final class OdbcRegistry {

    @Nonnull
    public static OdbcRegistry ofServiceLoader() {
        for (OdbcRegistrySpi o : ServiceLoader.load(OdbcRegistrySpi.class)) {
            if (o.isAvailable()) {
                return new OdbcRegistry(o);
            }
        }
        return noOp();
    }

    @Nonnull
    public static OdbcRegistry noOp() {
        return new OdbcRegistry(NoOpRegistry.INSTANCE);
    }

    @lombok.NonNull
    private final OdbcRegistrySpi spi;

    @Nonnull
    @SuppressWarnings("null")
    public String getName() {
        String result;

        try {
            result = spi.getName();
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, "Unexpected exception while getting name for '" + spi.getClass().getName() + "'", ex);
            return spi.getClass().getName();
        }

        if (result == null) {
            log.log(Level.WARNING, "Unexpected null while getting name for ''{0}''", spi.getClass().getName());
            return spi.getClass().getName();
        }

        return result;
    }

    @Nonnull
    public List<OdbcDataSource> getDataSources(@Nonnull OdbcDataSource.Type... types) throws IOException {
        List<OdbcDataSource> result;

        try {
            result = spi.getDataSources(types);
        } catch (RuntimeException ex) {
            throw new IOException("Unexpected exception while getting data sources for " + Arrays.toString(types), ex);
        }

        if (result == null) {
            throw new IOException("Unexpected null while getting data sources for " + Arrays.toString(types));
        }

        return result;
    }

    @Nonnull
    public List<OdbcDriver> getDrivers() throws IOException {
        List<OdbcDriver> result;

        try {
            result = spi.getDrivers();
        } catch (RuntimeException ex) {
            throw new IOException("Unexpected exception while getting drivers", ex);
        }

        if (result == null) {
            throw new IOException("Unexpected null while getting data drivers");
        }

        return result;
    }

    private enum NoOpRegistry implements OdbcRegistrySpi {
        INSTANCE;

        @Override
        public String getName() {
            return "noOp";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) {
            return Collections.emptyList();
        }

        @Override
        public List<OdbcDriver> getDrivers() {
            return Collections.emptyList();
        }
    }
}
