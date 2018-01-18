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
package ec.tss.tsproviders.odbc;

import java.io.File;
import java.io.FileFilter;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class OdbcBeanTest {

    @Test
    public void testFileConnectionString() {
        File mdb = new File("hello.mdb");
        File accdb = new File("hello.accdb");
        File txt = new File("hello.txt");

        FileFilter ff = OdbcBean.AccessFileFilter.INSTANCE;
        Assert.assertTrue(ff.accept(mdb));
        Assert.assertTrue(ff.accept(accdb));
        Assert.assertFalse(ff.accept(txt));

        OdbcBean.AccessConnectionStringParser p = new OdbcBean.AccessConnectionStringParser();
        OdbcBean.AccessConnectionStringFormatter f = new OdbcBean.AccessConnectionStringFormatter();

        Assert.assertEquals(mdb, p.parse(f.format(mdb)));
        Assert.assertNotSame(mdb, p.parse(f.format(mdb)));

        Assert.assertNull(p.parse(txt.getPath()));
        Assert.assertNull(f.format(txt));
    }

    @Test
    public void testConnectionStringSplitter() {
        assertEquals("c:\\bin\\Northwind.mdb", OdbcBean.CONNECTION_STRING_SPLITTER.split("Driver={Microsoft Access Driver (*.mdb)};DBQ=c:\\bin\\Northwind.mdb").get("DBQ"));
    }
}
