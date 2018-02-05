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
package sql.util.odbc;

import demetra.design.ServiceDefinition;
import sql.util.odbc.spi.OdbcFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;

/**
 *
 * @see
 * http://msdn.microsoft.com/en-us/library/windows/desktop/ms715432(v=vs.85).aspx
 * @author Philippe Charles
 */
@ServiceDefinition
public interface OdbcRegistry {

    @Nonnull
    List<OdbcDataSource> getDataSources(@Nonnull OdbcDataSource.Type... types) throws IOException;

    @Nonnull
    List<OdbcDriver> getDrivers() throws IOException;

    @Nonnull
    static OdbcRegistry getDefault() {
        for (OdbcFactory o : ServiceLoader.load(OdbcFactory.class)) {
            if (o.isAvailable()) {
                return o.getRegistry();
            }
        }
        return noOp();
    }

    @Nonnull
    static OdbcRegistry noOp() {
        return new OdbcRegistry() {
            @Override
            public List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) throws IOException {
                return Collections.emptyList();
            }

            @Override
            public List<OdbcDriver> getDrivers() throws IOException {
                return Collections.emptyList();
            }
        };
    }
}
