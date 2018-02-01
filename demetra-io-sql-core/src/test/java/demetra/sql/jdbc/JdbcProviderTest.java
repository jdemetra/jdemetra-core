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

import demetra.bridge.FromDataSourceLoader;
import ec.tss.tsproviders.IDataSourceLoaderAssert;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class JdbcProviderTest {

    @Test
    public void testEquivalence() throws IOException {
        IDataSourceLoaderAssert
                .assertThat(new FromDataSourceLoader(getProvider()))
                .isEquivalentTo(getPreviousProvider(), o -> o.encodeBean(getPreviousBean(o)));
    }

    @Test
    public void testTspCompliance() {
        IDataSourceLoaderAssert.Sampler<FromDataSourceLoader<JdbcProvider>> sampler = o -> getBean(o.getDelegate());
        IDataSourceLoaderAssert.assertCompliance(() -> new FromDataSourceLoader(getProvider()), sampler);
    }

    private static JdbcProvider getProvider() {
        JdbcProvider provider = new JdbcProvider();
        provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    private static JdbcBean getBean(JdbcProvider o) {
        JdbcBean bean = o.newBean();
        bean.setDatabase("mydb");
        bean.setTable("Table2");
        bean.setDimColumns(Arrays.asList("Sector", "Region"));
        bean.setPeriodColumn("Table2.Period");
        bean.setValueColumn("Rate");
        return bean;
    }

    private static ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider getPreviousProvider() {
        ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider provider = new ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider();
        provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    private static ec.tss.tsproviders.jdbc.JdbcBean getPreviousBean(ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider o) {
        ec.tss.tsproviders.jdbc.JdbcBean bean = o.newBean();
        bean.setDbName("mydb");
        bean.setTableName("Table2");
        bean.setDimColumns("Sector, Region");
        bean.setPeriodColumn("Table2.Period");
        bean.setValueColumn("Rate");
        return bean;
    }
}
