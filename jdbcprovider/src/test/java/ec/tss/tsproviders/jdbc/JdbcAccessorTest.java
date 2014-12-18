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

package ec.tss.tsproviders.jdbc;

import ec.tss.tsproviders.db.DbAccessor;
import ec.tss.tsproviders.db.DbSeries;
import ec.tss.tsproviders.db.DbSetId;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

/**
 *
 * @author Philippe Charles
 */
public class JdbcAccessorTest {

    static final Logger LOGGER = NOPLogger.NOP_LOGGER;
    static final ConnectionSupplier SUPPLIER = new ConnectionSupplier() {
        @Override
        public Connection getConnection(JdbcBean bean) throws SQLException {
            return DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", "");
        }
    };
    //
    static final double[][] D0 = {{1.2, 2.3}};
    static final double[][] D2 = {{1.2, 2.3}, {3.4, 4.5}, {5.6, 6.7}, {7.8, 8.9}};
    static JdbcAccessor A0;
    static JdbcAccessor A2;

    @BeforeClass
    public static void beforeClass() {
        JdbcBean bean0 = new JdbcBean();
        bean0.setDbName("mydb");
        bean0.setTableName("Table0");
        bean0.setDimColumns("");
        bean0.setPeriodColumn("Period");
        bean0.setValueColumn("Rate");
        A0 = new JdbcAccessor(LOGGER, bean0, SUPPLIER);
        JdbcBean bean2 = new JdbcBean();
        bean2.setDbName("mydb");
        bean2.setTableName("Table2");
        bean2.setDimColumns("Sector, Region");
        bean2.setPeriodColumn("Period");
        bean2.setValueColumn("Rate");
        A2 = new JdbcAccessor(LOGGER, bean2, SUPPLIER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChildren_Val0Dim0() throws Exception {
        A0.getChildren();
    }

    @Test
    public void testGetChildren_Val0Dim2() throws Exception {
        assertArrayEquals(new String[]{"Industry", "Other"}, A2.getChildren().toArray());
    }

    @Test
    public void testGetChildren_Val1Dim2() throws Exception {
        assertArrayEquals(new String[]{"Belgium", "Europe"}, A2.getChildren("Industry").toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChildren_Val2Dim2() throws Exception {
        A2.getChildren("Industry", "Belgium");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSeries_Val0Dim0() throws Exception {
        A0.getAllSeries();
    }

    @Test
    public void testGetAllSeries_Val0Dim2() throws Exception {
        List<DbSetId> data = A2.getAllSeries();
        assertEquals(4, data.size());
        assertEquals(A2.getRoot().child("Industry", "Belgium"), data.get(0));
        assertEquals(A2.getRoot().child("Industry", "Europe"), data.get(1));
        assertEquals(A2.getRoot().child("Other", "Belgium"), data.get(2));
        assertEquals(A2.getRoot().child("Other", "Europe"), data.get(3));
    }

    @Test
    public void testGetAllSeries_Val1Dim2() throws Exception {
        List<DbSetId> data = A2.getAllSeries("Industry");
        assertEquals(2, data.size());
        assertEquals(A2.getRoot().child("Industry", "Belgium"), data.get(0));
        assertEquals(A2.getRoot().child("Industry", "Europe"), data.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSeries_Val2Dim2() throws Exception {
        A2.getAllSeries("Industry", "Belgium");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSeriesWithData_Val0Dim0() throws Exception {
        A0.getAllSeriesWithData();
    }

    @Test
    public void testGetAllSeriesWithData_Val0Dim2() throws Exception {
        List<DbSeries> data = A2.getAllSeriesWithData();
        assertEquals(4, data.size());
        assertEquals(A2.getRoot().child("Industry", "Belgium"), data.get(0).getId());
        assertArrayEquals(D2[0], data.get(0).getData().get().getValues().internalStorage(), 0);
        assertEquals(A2.getRoot().child("Industry", "Europe"), data.get(1).getId());
        assertArrayEquals(D2[1], data.get(1).getData().get().getValues().internalStorage(), 0);
        assertEquals(A2.getRoot().child("Other", "Belgium"), data.get(2).getId());
        assertArrayEquals(D2[2], data.get(2).getData().get().getValues().internalStorage(), 0);
        assertEquals(A2.getRoot().child("Other", "Europe"), data.get(3).getId());
        assertArrayEquals(D2[3], data.get(3).getData().get().getValues().internalStorage(), 0);
    }

    @Test
    public void testGetAllSeriesWithData_Val1Dim2() throws Exception {
        List<DbSeries> data = A2.getAllSeriesWithData("Industry");
        assertEquals(2, data.size());
        assertEquals(A2.getRoot().child("Industry", "Belgium"), data.get(0).getId());
        assertArrayEquals(D2[0], data.get(0).getData().get().getValues().internalStorage(), 0);
        assertEquals(A2.getRoot().child("Industry", "Europe"), data.get(1).getId());
        assertArrayEquals(D2[1], data.get(1).getData().get().getValues().internalStorage(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSeriesWithData_Val2Dim2() throws Exception {
        A2.getAllSeriesWithData("Industry", "Belgium");
    }

    @Test
    public void testSeriesWithData_Val0Dim0() throws Exception {
        DbSeries data = A0.getSeriesWithData();
        assertEquals(A0.getRoot(), data.getId());
        assertArrayEquals(D0[0], data.getData().get().getValues().internalStorage(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSeriesWithData_Val0Dim2() throws Exception {
        A2.getSeriesWithData();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSeriesWithData_Val1Dim2() throws Exception {
        A2.getSeriesWithData("Industry");
    }

    @Test
    public void testSeriesWithData_Val2Dim2() throws Exception {
        DbSeries data = A2.getSeriesWithData("Industry", "Belgium");
        assertEquals(A2.getRoot().child("Industry", "Belgium"), data.getId());
        assertArrayEquals(D2[0], data.getData().get().getValues().internalStorage(), 0);
    }

    @Test
    public void testCache() throws Exception {
        JdbcBean b2c1 = new JdbcBean();
        b2c1.setDbName("mydb");
        b2c1.setTableName("Table2");
        b2c1.setDimColumns("Sector, Region");
        b2c1.setPeriodColumn("Period");
        b2c1.setValueColumn("Rate");
        b2c1.setCacheDepth(1);
        DbAccessor a2c1 = new JdbcAccessor(LOGGER, b2c1, SUPPLIER).memoize();
        DbSeries a2c1_first = a2c1.getSeriesWithData("Industry", "Belgium");
        DbSeries a2c1_second = a2c1.getSeriesWithData("Industry", "Belgium");

        assertEquals(a2c1_first, a2c1_second);
        assertSame(a2c1_first, a2c1_second);

        JdbcBean b2c2 = new JdbcBean();
        b2c2.setDbName("mydb");
        b2c2.setTableName("Table2");
        b2c2.setDimColumns("Sector, Region");
        b2c2.setPeriodColumn("Period");
        b2c2.setValueColumn("Rate");
        b2c2.setCacheDepth(2);
        DbAccessor a2c2 = new JdbcAccessor(LOGGER, b2c2, SUPPLIER).memoize();
        DbSeries a2c2_first = a2c2.getSeriesWithData("Industry", "Belgium");
        DbSeries a2c2_second = a2c2.getSeriesWithData("Industry", "Belgium");

        assertEquals(a2c2_first, a2c2_second);
        assertSame(a2c2_first, a2c2_second);

        assertEquals(a2c1_first, a2c2_first);
        assertNotSame(a2c1_first, a2c2_first);
    }
}
