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
package ec.util.spreadsheet.helpers;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CellRefHelperTest {

    @Test
    public void testGetColumnIndex() {
        Assert.assertEquals(0, CellRefHelper.getColumnIndex("A"));
        Assert.assertEquals(25, CellRefHelper.getColumnIndex("Z"));
        Assert.assertEquals(26, CellRefHelper.getColumnIndex("AA"));
        Assert.assertEquals(27, CellRefHelper.getColumnIndex("AB"));
        Assert.assertEquals(52, CellRefHelper.getColumnIndex("BA"));
    }
    
    @Test
    public void testGetRef() {
        Assert.assertEquals("A1", CellRefHelper.getCellRef(0, 0));
        Assert.assertEquals("B1", CellRefHelper.getCellRef(0, 1));
        Assert.assertEquals("A2", CellRefHelper.getCellRef(1, 0));
        Assert.assertEquals("AA1", CellRefHelper.getCellRef(0, 26));
    }
}
