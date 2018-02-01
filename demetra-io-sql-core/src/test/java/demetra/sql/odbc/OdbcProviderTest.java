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

import demetra.bridge.FromDataSourceLoader;
import ec.tss.tsproviders.IDataSourceLoaderAssert;
import ec.tss.tsproviders.odbc.OdbcProviderX;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OdbcProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        IDataSourceLoaderAssert
                .assertThat(new FromDataSourceLoader(getProvider()))
                .isEquivalentTo(getPreviousProvider(), o -> o.encodeBean(getPreviousBean(o)));
    }

    @Test
    public void testTspCompliance() {
        IDataSourceLoaderAssert.Sampler<FromDataSourceLoader<OdbcProvider>> sampler = o -> getBean(o.getDelegate());
        IDataSourceLoaderAssert.assertCompliance(() -> new FromDataSourceLoader(getProvider()), sampler);
    }

    private static OdbcProvider getProvider() {
        OdbcProvider provider = new OdbcProvider();
        provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    private static OdbcBean getBean(OdbcProvider o) {
        OdbcBean bean = o.newBean();
        bean.setDsn("mydb");
        bean.setTable("Table2");
        bean.setDimColumns(Arrays.asList("Sector", "Region"));
        bean.setPeriodColumn("Table2.Period");
        bean.setValueColumn("Rate");
        return bean;
    }

    private static ec.tss.tsproviders.odbc.OdbcProvider getPreviousProvider() {
        return OdbcProviderX.create(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
    }

    private static ec.tss.tsproviders.odbc.OdbcBean getPreviousBean(ec.tss.tsproviders.odbc.OdbcProvider o) {
        ec.tss.tsproviders.odbc.OdbcBean bean = o.newBean();
        bean.setDbName("mydb");
        bean.setTableName("Table2");
        bean.setDimColumns("Sector, Region");
        bean.setPeriodColumn("Table2.Period");
        bean.setValueColumn("Rate");
        return bean;
    }
}
