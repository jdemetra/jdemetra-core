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
package demetra.sql.jdbc;

import _test.JdbcSamples;
import demetra.bridge.FromDataSourceLoader;
import demetra.timeseries.TsMoniker;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.tck.DataSourceLoaderAssert;
import ec.tss.tsproviders.IDataSourceLoaderAssert;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class JdbcProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        IDataSourceLoaderAssert
                .assertThat(FromDataSourceLoader.fromDataSourceLoader(JdbcSamples.TABLE2.getProvider3()))
                .isEquivalentTo(JdbcSamples.TABLE2.getProvider2(), o -> o.encodeBean(JdbcSamples.TABLE2.getBean2(o)));
    }

    @Test
    public void testTspCompliance() {
        DataSourceLoaderAssert.assertCompliance(JdbcSamples.TABLE2::getProvider3, JdbcSamples.TABLE2::getBean3);
    }

    @Test
    public void testMonikerLegacy() {
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/JNDI-JDBC/20111201/SERIES?aggregationType=Last&cacheDepth=2&cacheTtl=1000&cleanMissing=false&datePattern=dd%2FMM%2Fyyyy&dbName=mydb&dimColumns=Sector%2C+Region&frequency=Monthly&labelColumn=Title&locale=fr&numberPattern=%23.%23&periodColumn=Table2.Period&tableName=Table2&valueColumn=Rate&versionColumn=Version#Region=Belgium&Sector=Industry";

        DataSource source = DataSource.builder("JNDI-JDBC", "20111201")
                .parameter("dbName", "mydb")
                .parameter("tableName", "Table2")
                .parameter("dimColumns", "Sector, Region")
                .parameter("periodColumn", "Table2.Period")
                .parameter("valueColumn", "Rate")
                .parameter("locale", "fr")
                .parameter("datePattern", "dd/MM/yyyy")
                .parameter("numberPattern", "#.#")
                .parameter("versionColumn", "Version")
                .parameter("labelColumn", "Title")
                .parameter("frequency", "Monthly")
                .parameter("aggregationType", "Last")
                .parameter("cleanMissing", "false")
                .parameter("cacheTtl", "1000")
                .parameter("cacheDepth", "2")
                .build();

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .parameter("Sector", "Industry")
                .parameter("Region", "Belgium")
                .build();

        try (DataSourceProvider p = new JdbcProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("JNDI-JDBC", uri))).hasValue(expected);
        }
    }
}
