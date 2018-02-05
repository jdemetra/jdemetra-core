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
package sql.util.odbc;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 * @author Philippe Charles
 */
public class OdbcConnectionStringTest {

    private final String s1 = "Driver={SQL Server};Server=(local);Trusted_Connection=Yes;Database=AdventureWorks;";
    private final String s2 = "Driver={Microsoft ODBC for Oracle};Server=ORACLE8i7;Persist Security Info=False;Trusted_Connection=Yes";
    private final String s3 = "Driver={Microsoft Access Driver (*.mdb)};DBQ=c:\\bin\\Northwind.mdb";
    private final String s4 = "Driver={Microsoft Excel Driver (*.xls)};DBQ=c:\\bin\\book1.xls";
    private final String s5 = "Driver={Microsoft Text Driver (*.txt; *.csv)};DBQ=c:\\bin";
    private final String s6 = "DSN=dsnname";

    @Test
    public void testFormat() {
        assertThat(OdbcConnectionString
                .builder()
                .with("Driver", "SQL Server")
                .with("Server", "(local)")
                .with("Trusted_Connection", "Yes")
                .with("Database", "AdventureWorks")
                .build()
                .toString() + ";"
        ).isEqualTo(s1);

        assertThat(OdbcConnectionString
                .builder()
                .with("Driver", "Microsoft ODBC for Oracle")
                .with("Server", "ORACLE8i7")
                .with("Persist Security Info", "False")
                .with("Trusted_Connection", "Yes")
                .build()
                .toString()
        ).isEqualTo(s2);
    }

    @Test
    public void testParse() {
        assertThat(OdbcConnectionString.parse(s1))
                .isEqualTo(OdbcConnectionString
                        .builder()
                        .with("Driver", "SQL Server")
                        .with("Server", "(local)")
                        .with("Trusted_Connection", "Yes")
                        .with("Database", "AdventureWorks")
                        .build());
    }

    @Test
    public void testGetDriver() {
        assertThat(OdbcConnectionString.parse(s1).getDriver()).isEqualTo("SQL Server");
        assertThat(OdbcConnectionString.parse(s2).getDriver()).isEqualTo("Microsoft ODBC for Oracle");
        assertThat(OdbcConnectionString.parse(s3).getDriver()).isEqualTo("Microsoft Access Driver (*.mdb)");
        assertThat(OdbcConnectionString.parse(s4).getDriver()).isEqualTo("Microsoft Excel Driver (*.xls)");
        assertThat(OdbcConnectionString.parse(s5).getDriver()).isEqualTo("Microsoft Text Driver (*.txt; *.csv)");
        assertThat(OdbcConnectionString.parse(s6).getDriver()).isNull();
    }
}
