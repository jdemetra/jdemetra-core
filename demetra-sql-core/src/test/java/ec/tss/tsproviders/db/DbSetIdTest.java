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

package ec.tss.tsproviders.db;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Philippe Charles
 */
public class DbSetIdTest {

    static final String[] COLUMNS = {"Region", "Sector"};
    static final String[] V0 = {};
    static final String[] V1 = {"Belgium"};
    static final String[] V2 = {"Belgium", "Industry"};
    static final DbSetId ID0 = DbSetId.root(COLUMNS);
    static final DbSetId ID1 = ID0.child(V1);
    static final DbSetId ID2 = ID0.child(V2);

    @Test(expected = IllegalArgumentException.class)
    public void testRoot() {
        String[] tmp = new String[1];
        tmp[0] = null;
        DbSetId.root(tmp);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings({"null", "ConstantConditions"})
    public void testRoot2() {
        DbSetId.root((String[])null);
    }

    @Test
    public void testGetColumn() {
        assertEquals(COLUMNS[0], ID0.getColumn(0));
        assertEquals(COLUMNS[1], ID0.getColumn(1));
    }

    @Test
    public void testGetDepth() {
        assertEquals(COLUMNS.length - 0, ID0.getDepth());
        assertEquals(COLUMNS.length - 1, ID1.getDepth());
        assertEquals(COLUMNS.length - 2, ID2.getDepth());
    }

    @Test
    public void testGetLevel() {
        assertEquals(V0.length, ID0.getLevel());
        assertEquals(V1.length, ID1.getLevel());
        assertEquals(V2.length, ID2.getLevel());
    }

    @Test
    public void testGetMaxLevel() {
        assertEquals(COLUMNS.length, ID0.getMaxLevel());
        assertEquals(COLUMNS.length, ID1.getMaxLevel());
        assertEquals(COLUMNS.length, ID2.getMaxLevel());
    }

    @Test
    public void testGetValue() {
        assertEquals(V2[0], ID2.getValue(0));
        assertEquals(V2[1], ID2.getValue(1));
    }

    @Test
    public void testIsSeries() {
        assertFalse(ID0.isSeries());
        assertFalse(ID1.isSeries());
        assertTrue(ID2.isSeries());
    }

    @Test
    public void testParent() {
        assertFalse(ID0.parent().isPresent());
        assertTrue(ID1.parent().isPresent());
        assertEquals(ID0, ID1.parent().get());
        assertTrue(ID2.parent().isPresent());
        assertEquals(ID1, ID2.parent().get());
    }

    @Test
    public void testEquals() {
        assertTrue(ID0.equals(ID0));
        assertFalse(ID0.equals(ID1));
        assertFalse(ID1.equals(ID0));
        assertTrue(ID0.child("A", "B").equals(ID0.child("A", "B")));
        assertFalse(ID0.child("A", "B").equals(ID0.child("B", "A")));
    }
}
