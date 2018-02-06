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
package internal.spreadsheet;

import demetra.tsprovider.TsCollection;
import demetra.tsprovider.grid.GridImport;
import internal.spreadsheet.grid.SheetGrid;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import _test.DataForTest;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetAccessorTest {

    @Test
    public void testWithCache() throws IOException {
        ConcurrentMap cache = new ConcurrentHashMap();
        SpreadSheetAccessor grid = SheetGrid.of(DataForTest.FACTORY, new File(""), GridImport.DEFAULT).withCache(cache);

        cache.clear();
        assertThat(grid.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(cache).containsOnlyKeys("getSheetByName/s1");

        cache.clear();
        assertThat(grid.getSheetByName("other")).isEmpty();
        assertThat(cache).containsOnlyKeys("getSheetByName/other");

        cache.clear();
        assertThat(grid.getSheetNames()).containsExactly("s1", "s2");
        assertThat(cache).containsOnlyKeys("getSheetNames");

        cache.clear();
        assertThat(grid.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
        assertThat(cache).containsOnlyKeys("getSheets");

        cache.clear();
        assertThat(grid.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
        assertThat(grid.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(grid.getSheetNames()).containsExactly("s1", "s2");
        assertThat(cache).containsOnlyKeys("getSheets");
    }
}
