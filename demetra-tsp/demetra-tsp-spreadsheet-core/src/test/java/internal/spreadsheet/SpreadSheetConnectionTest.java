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

import _test.DataForTest;
import demetra.timeseries.TsCollection;
import demetra.tsprovider.grid.GridReader;
import internal.spreadsheet.grid.SheetGrid;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SpreadSheetConnectionTest {

    @Test
    public void testWithCache() throws IOException {
        SheetGrid grid = SheetGrid.of(new File(""), DataForTest.FACTORY, GridReader.DEFAULT);
        Map<String, Object> cache = new HashMap<>();
        SpreadSheetConnection accessor = new CachedSpreadSheetConnection(grid, cache);

        cache.clear();
        assertThat(accessor.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(cache).containsKeys("getSheetByName/s1");

        cache.clear();
        assertThat(accessor.getSheetByName("other")).isEmpty();
        assertThat(cache).containsKeys("getSheetByName/other");

        cache.clear();
        assertThat(accessor.getSheetNames()).containsExactly("s1", "s2");
        assertThat(cache).containsKeys("getSheetNames");

        cache.clear();
        assertThat(accessor.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
        assertThat(cache).containsKeys("getSheets");

        cache.clear();
        assertThat(accessor.getSheets()).extracting(o -> o.getName()).containsExactly("s1", "s2");
        assertThat(accessor.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
        assertThat(accessor.getSheetNames()).containsExactly("s1", "s2");
        assertThat(cache).containsKeys("getSheets");
    }
}
