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
package ec.tss.tsproviders.spreadsheet.engine;

import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection.AlignType;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class SpreadSheetSeries implements Comparable<SpreadSheetSeries> {

    public final String seriesName;
    public final int ordering;
    public final AlignType alignType;
    public final OptionalTsData data;

    public SpreadSheetSeries(@Nonnull String seriesName, int ordering, @Nonnull AlignType alignType, @Nonnull OptionalTsData data) {
        this.seriesName = seriesName;
        this.ordering = ordering;
        this.alignType = alignType;
        this.data = data;
    }

    @Override
    public int compareTo(SpreadSheetSeries o) {
        int result = Integer.compare(ordering, o.ordering);
        if (result != 0) {
            return result;
        }
        return seriesName.compareTo(o.seriesName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordering, seriesName);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SpreadSheetSeries && equals((SpreadSheetSeries) obj));
    }

    private boolean equals(SpreadSheetSeries that) {
        return this.ordering == that.ordering && this.seriesName.equals(that.seriesName);
    }
}
