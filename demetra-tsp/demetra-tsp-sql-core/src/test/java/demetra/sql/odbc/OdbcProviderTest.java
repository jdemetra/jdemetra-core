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
package demetra.sql.odbc;

import _test.OdbcSamples;
import demetra.bridge.FromDataSourceLoader;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceProvider;
import demetra.timeseries.TsMoniker;
import ec.tss.tsproviders.IDataSourceLoaderAssert;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OdbcProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        IDataSourceLoaderAssert
                .assertThat(new FromDataSourceLoader(OdbcSamples.TABLE2.getProvider3()))
                .isEquivalentTo(OdbcSamples.TABLE2.getProvider2(), o -> o.encodeBean(OdbcSamples.TABLE2.getBean2(o)));
    }

    @Test
    public void testTspCompliance() {
        IDataSourceLoaderAssert.Sampler<FromDataSourceLoader> sampler = o -> OdbcSamples.TABLE2.getBean3((OdbcProvider) o.getDelegate());
        IDataSourceLoaderAssert.assertCompliance(() -> new FromDataSourceLoader(OdbcSamples.TABLE2.getProvider3()), sampler);
    }

    @Test
    public void testMonikerLegacy() {
    }

    @Test
    public void testMonikerUri() {
        String uri = "demetra://tsprovider/ODBCPRVDR/20111201/SERIES?aggregationType=Last&cacheDepth=2&cacheTtl=1000&cleanMissing=false&datePattern=dd%2FMM%2Fyyyy&dbName=mydb&dimColumns=Sector%2C+Region&frequency=Monthly&labelColumn=Title&locale=fr&numberPattern=%23.%23&periodColumn=Table2.Period&tableName=Table2&valueColumn=Rate&versionColumn=Version#Region=Belgium&Sector=Industry";

        DataSource source = DataSource.builder("ODBCPRVDR", "20111201")
                .put("dbName", "mydb")
                .put("tableName", "Table2")
                .put("dimColumns", "Sector, Region")
                .put("periodColumn", "Table2.Period")
                .put("valueColumn", "Rate")
                .put("locale", "fr")
                .put("datePattern", "dd/MM/yyyy")
                .put("numberPattern", "#.#")
                .put("versionColumn", "Version")
                .put("labelColumn", "Title")
                .put("frequency", "Monthly")
                .put("aggregationType", "Last")
                .put("cleanMissing", "false")
                .put("cacheTtl", "1000")
                .put("cacheDepth", "2")
                .build();

        DataSet expected = DataSet.builder(source, DataSet.Kind.SERIES)
                .put("Sector", "Industry")
                .put("Region", "Belgium")
                .build();

        try (DataSourceProvider p = new OdbcProvider()) {
            assertThat(p.toDataSet(TsMoniker.of("ODBCPRVDR", uri))).hasValue(expected);
        }
    }
}
