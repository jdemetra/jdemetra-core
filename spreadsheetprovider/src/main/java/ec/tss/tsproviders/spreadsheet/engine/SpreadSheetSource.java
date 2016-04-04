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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class SpreadSheetSource {

    @Deprecated
    @Nonnull
    public static SpreadSheetSource load(@Nonnull Book book, @Nonnull DataFormat df, @Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation, boolean clean) throws IOException {
        return Engine.parseSource(book, df, freq, aggregation, clean);
    }

    @Deprecated
    @Nonnull
    public static SpreadSheetSource load(@Nonnull Book book, @Nonnull Parsers.Parser<Date> dateParser, @Nonnull Parsers.Parser<Number> numberParser, @Nonnull TsFrequency freq, @Nonnull TsAggregationType aggregation, boolean clean) throws IOException {
        return Engine.parseSource(book, dateParser, numberParser, freq, aggregation, clean);
    }

    @Deprecated
    public static SpreadSheetSource load(Book book, CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency freq, TsAggregationType aggregation, boolean clean) throws IOException {
        return Engine.parseSource(book, toName, toDate, toNumber, freq, aggregation, clean);
    }

    public final ImmutableMap<String, SpreadSheetCollection> collections;
    public final String factoryName;

    @Deprecated
    public SpreadSheetSource(List<SpreadSheetCollection> list, String factoryName) {
        this.collections = Maps.uniqueIndex(list, ToSheetName.INSTANCE);
        this.factoryName = factoryName;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class ToSheetName implements Function<SpreadSheetCollection, String> {

        private static final ToSheetName INSTANCE = new ToSheetName();

        @Override
        public String apply(SpreadSheetCollection input) {
            return input != null ? input.sheetName : null;
        }
    }
    //</editor-fold>
}
