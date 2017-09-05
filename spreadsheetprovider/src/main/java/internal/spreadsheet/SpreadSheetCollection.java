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
package internal.spreadsheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class SpreadSheetCollection implements Comparable<SpreadSheetCollection> {

    String sheetName; // unique id; don't use ordering
    int ordering; // this may change !
    AlignType alignType;
    List<SpreadSheetSeries> series;

    Map<String, Integer> map;

    public SpreadSheetCollection(@Nonnull String sheetName, int ordering, @Nonnull AlignType alignType, @Nonnull List<SpreadSheetSeries> series) {
        this(sheetName, ordering, alignType, series, new HashMap<>());
    }

    private SpreadSheetCollection(String sheetName, int ordering, AlignType alignType, List<SpreadSheetSeries> series, Map<String, Integer> map) {
        this.sheetName = sheetName;
        this.ordering = ordering;
        this.alignType = alignType;
        this.series = series;
        this.map = map;
    }

    @Override
    public int compareTo(SpreadSheetCollection o) {
        int result = Integer.compare(ordering, o.ordering);
        if (result != 0) {
            return result;
        }
        return sheetName.compareTo(o.sheetName);
    }
}
