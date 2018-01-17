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

import demetra.tsprovider.grid.GridLayout;
import internal.spreadsheet.grid.SheetData;
import demetra.timeseries.TsData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 *
 * @author Philippe Charles
 */
final class SpreadSheetCollectionAssert extends AbstractAssert<SpreadSheetCollectionAssert, SheetData> {

    public static SpreadSheetCollectionAssert assertThat(SheetData actual) {
        return new SpreadSheetCollectionAssert(actual);
    }

    public SpreadSheetCollectionAssert(SheetData actual) {
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

    public SpreadSheetCollectionAssert hasLayout(GridLayout layout) {
        isNotNull();
        if (!actual.getData().getLayout().equals(layout)) {
            failWithMessage("Expected alignType to be <%s> but was <%s>", layout, actual.getData().getItems());
        }
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(TsData... data) {
        Assertions.assertThat(actual.getData().getItems())
                .extracting(o -> o.getData().get())
                .containsExactly(data);
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(String... names) {
        Assertions.assertThat(actual.getData().getItems())
                .extracting(o -> o.getName())
                .containsExactly(names);
        return this;
    }
}
