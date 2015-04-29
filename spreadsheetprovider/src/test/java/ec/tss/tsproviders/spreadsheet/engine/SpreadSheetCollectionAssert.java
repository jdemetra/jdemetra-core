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
package ec.tss.tsproviders.spreadsheet.engine;

import ec.tstoolkit.timeseries.simplets.TsData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.iterable.Extractor;

/**
 *
 * @author Philippe Charles
 */
final class SpreadSheetCollectionAssert extends AbstractAssert<SpreadSheetCollectionAssert, SpreadSheetCollection> {

    public static SpreadSheetCollectionAssert assertThat(SpreadSheetCollection actual) {
        return new SpreadSheetCollectionAssert(actual);
    }

    public SpreadSheetCollectionAssert(SpreadSheetCollection actual) {
        super(actual, SpreadSheetCollectionAssert.class);
    }

    public SpreadSheetCollectionAssert hasSheetName(String name) {
        isNotNull();
        if (!actual.sheetName.equals(name)) {
            failWithMessage("Expected sheet's name to be <%s> but was <%s>", name, actual.sheetName);
        }
        return this;
    }

    public SpreadSheetCollectionAssert hasOrdering(int ordering) {
        isNotNull();
        if (actual.ordering != ordering) {
            failWithMessage("Expected ordering to be <%s> but was <%s>", ordering, actual.ordering);
        }
        return this;
    }

    public SpreadSheetCollectionAssert hasAlignType(SpreadSheetCollection.AlignType alignType) {
        isNotNull();
        if (!actual.alignType.equals(alignType)) {
            failWithMessage("Expected alignType to be <%s> but was <%s>", alignType, actual.alignType);
        }
        return this;
    }

    public SpreadSheetCollectionAssert containsExactly(TsData... data) {
        Assertions.assertThat(actual.series)
                .extracting(ToData.INSTANCE)
                .containsExactly(data);
        return this;
    }

    private enum ToData implements Extractor<SpreadSheetSeries, TsData> {

        INSTANCE;

        @Override
        public TsData extract(SpreadSheetSeries input) {
            return input.data.get();
        }
    }
}
