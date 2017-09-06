/*
 * Copyright 2015 National Bank of Belgium
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

import internal.spreadsheet.grid.GridType;
import internal.spreadsheet.grid.GridSheet;
import demetra.timeseries.simplets.TsData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 *
 * @author Philippe Charles
 */
final class SpreadSheetCollectionAssert extends AbstractAssert<SpreadSheetCollectionAssert, GridSheet> {

    public static SpreadSheetCollectionAssert assertThat(GridSheet actual) {
        return new SpreadSheetCollectionAssert(actual);
    }

    public SpreadSheetCollectionAssert(GridSheet actual) {
        super(actual, SpreadSheetCollectionAssert.class);
    }

    public SpreadSheetCollectionAssert hasSheetName(String name) {
        isNotNull();
        if (!actual.getSheetName().equals(name)) {
            failWithMessage("Expected sheet's name to be <%s> but was <%s>", name, actual.getSheetName());
        }
        return this;
    }

    public SpreadSheetCollectionAssert hasOrdering(int ordering) {
        isNotNull();
        if (actual.getOrdering() != ordering) {
            failWithMessage("Expected ordering to be <%s> but was <%s>", ordering, actual.getOrdering());
        }
        return this;
    }

    public SpreadSheetCollectionAssert hasGridType(GridType gridType) {
        isNotNull();
        if (!actual.getGridType().equals(gridType)) {
            failWithMessage("Expected alignType to be <%s> but was <%s>", gridType, actual.getGridType());
        }
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(TsData... data) {
        Assertions.assertThat(actual.getRanges())
                .extracting(o -> o.getData().get())
                .containsExactly(data);
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(String... names) {
        Assertions.assertThat(actual.getRanges())
                .extracting(o -> o.getSeriesName())
                .containsExactly(names);
        return this;
    }
}
