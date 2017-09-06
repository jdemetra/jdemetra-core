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
package internal.spreadsheet.grid;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class GridSheet {

    @lombok.NonNull
    private String sheetName; // unique id; don't use ordering

    private int ordering; // this may change !

    @lombok.NonNull
    private GridType gridType;

    @lombok.NonNull
    private List<GridSeries> ranges;

    @Nullable
    public GridSeries getSeriesByName(@Nonnull String name) {
        for (GridSeries o : ranges) {
            if (o.getSeriesName().equals(name)) {
                return o;
            }
        }
        return null;
    }
}
