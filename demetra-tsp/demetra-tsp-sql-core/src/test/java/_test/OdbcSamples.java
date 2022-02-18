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
import demetra.sql.odbc.OdbcBean;
import demetra.sql.odbc.OdbcProvider;
import demetra.tsprovider.cube.TableAsCube;
import ec.tss.tsproviders.odbc.OdbcProviderX;
import java.sql.DriverManager;

/**
 *
 * @author Philippe Charles
 */
public enum OdbcSamples implements ProviderResources.Loader2<ec.tss.tsproviders.odbc.OdbcProvider>, ProviderResources.Loader3<OdbcProvider> {
    TABLE2;

    @Override
    public ec.tss.tsproviders.odbc.OdbcProvider getProvider2() {
        return OdbcProviderX.create(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
    }

    @Override
    public ec.tss.tsproviders.odbc.OdbcBean getBean2(ec.tss.tsproviders.odbc.OdbcProvider provider) {
        ec.tss.tsproviders.odbc.OdbcBean bean = provider.newBean();
        bean.setDbName("mydb");
        bean.setTableName("Table2");
        bean.setDimColumns("Sector, Region");
        bean.setPeriodColumn("Period");
        bean.setValueColumn("Rate");
        return bean;
    }

    @Override
    public OdbcProvider getProvider3() {
        OdbcProvider provider = new OdbcProvider();
        provider.setConnectionSupplier(o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    @Override
    public OdbcBean getBean3(OdbcProvider provider) {
        OdbcBean bean = provider.newBean();
        bean.setDsn("mydb");
        bean.setTable("Table2");
        bean.setCube(TableAsCube
                .builder()
                .dimension("Sector")
                .dimension("Region")
                .timeDimension("Period")
                .measure("Rate")
                .build());
        return bean;
    }
}
