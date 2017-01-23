package ec.tss.tsproviders.jdbc;

import java.sql.DriverManager;

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
/**
 *
 * @author Philippe Charles
 */
public final class JdbcSamples {

    private JdbcSamples() {
        // static class
    }

    public static ConnectionSupplier mydbConnectionSupplier() {
        return o -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", "");
    }

    public static JdbcBean mydbNoDimsBean() {
        JdbcBean result = new JdbcBean();
        result.setDbName("mydb");
        result.setTableName("Table0");
        result.setDimColumns("");
        // FIXME: "PERIOD" is a keyword in SQL2011 and escaping fails for some raison
        result.setPeriodColumn("Table0.Period");
        result.setValueColumn("Rate");
        return result;
    }

    public static JdbcBean mydbTwoDimsBean() {
        JdbcBean result = new JdbcBean();
        result.setDbName("mydb");
        result.setTableName("Table2");
        result.setDimColumns("Sector, Region");
        result.setPeriodColumn("Table2.Period");
        result.setValueColumn("Rate");
        return result;
    }
}
