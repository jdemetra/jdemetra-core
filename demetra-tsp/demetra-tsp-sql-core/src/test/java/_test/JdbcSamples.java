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
package _test;

import demetra.demo.ProviderResources;
import demetra.sql.jdbc.JdbcBean;
import demetra.sql.jdbc.JdbcProvider;
import ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider;
import java.sql.DriverManager;
import java.util.Arrays;

/**
 *
 * @author Philippe Charles
 */
public enum JdbcSamples implements ProviderResources.Loader2<JndiJdbcProvider>, ProviderResources.Loader3<JdbcProvider> {
    TABLE2;

    @Override
    public JndiJdbcProvider getProvider2() {
        JndiJdbcProvider provider = new JndiJdbcProvider();
        provider.setConnectionSupplier((ec.tss.tsproviders.jdbc.JdbcBean o) -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    @Override
    public ec.tss.tsproviders.jdbc.JdbcBean getBean2(JndiJdbcProvider provider) {
        ec.tss.tsproviders.jdbc.JdbcBean bean = provider.newBean();
        bean.setDbName("mydb");
        bean.setTableName("Table2");
        bean.setDimColumns("Sector, Region");
        bean.setPeriodColumn("Period");
        bean.setValueColumn("Rate");
        return bean;
    }

    @Override
    public JdbcProvider getProvider3() {
        JdbcProvider provider = new JdbcProvider();
        provider.setConnectionSupplier((java.lang.String o) -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    @Override
    public JdbcBean getBean3(JdbcProvider provider) {
        JdbcBean bean = provider.newBean();
        bean.setDatabase("mydb");
        bean.setTable("Table2");
        bean.setDimColumns(Arrays.asList("Sector", "Region"));
        // FIXME: "PERIOD" is a keyword in SQL2011 and escaping fails for some raison
        bean.setPeriodColumn("Table2.Period");
        bean.setValueColumn("Rate");
        return bean;
    }
}
