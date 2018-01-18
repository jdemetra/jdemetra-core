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
package ec.tss.tsproviders.odbc.registry;

import com.google.common.base.StandardSystemProperty;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class JnaOdbcRegistryTest {

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue(StandardSystemProperty.OS_NAME.value().startsWith("Windows "));
    }

    @Test
    @Ignore
    public void testGetDataSources() {
        JnaOdbcRegistry registry = new JnaOdbcRegistry();
        for (OdbcDataSource o : registry.getDataSources(OdbcDataSource.Type.USER)) {
            System.out.println(o);
        }
    }

    @Test
    @Ignore
    public void testGetDrivers() {
        JnaOdbcRegistry registry = new JnaOdbcRegistry();
        for (OdbcDriver o : registry.getDrivers()) {
            System.out.println(o);
        }
    }

    @Test
    @Ignore
    public void testGetDrivers2() {
        JnaOdbcRegistry registry = new JnaOdbcRegistry();
        for (OdbcDriver o : registry.getDrivers()) {
            if (o.getFileUsage().isFileBased() && o.getFileExtns().contains("mdb")) {
                System.out.println(o);
            }
        }
    }
}
