/*
 * Copyright 2013 National Bank of Belgium
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

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.spreadsheet.facade.Sheet;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
abstract class Engine {

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @VisibleForTesting
    static SpreadSheetSource parseSource(Book book, DataFormat df, TsFrequency freq, TsAggregationType aggregation, boolean clean) throws IOException {
        return parseSource(book, df.dateParser(), df.numberParser(), freq, aggregation, clean);
    }

    @VisibleForTesting
    static SpreadSheetSource parseSource(Book book, Parsers.Parser<Date> dateParser, Parsers.Parser<Number> numberParser, TsFrequency freq, TsAggregationType aggregation, boolean clean) throws IOException {
        CellParser<String> toName = CellParser.onStringType();
        CellParser<Date> toDate = CellParser.onDateType().or(CellParser.fromParser(dateParser));
        CellParser<Number> toNumber = CellParser.onNumberType().or(CellParser.fromParser(numberParser));

        return parseSource(book, toName, toDate, toNumber, freq, aggregation, clean);
    }

    @VisibleForTesting
    static SpreadSheetSource parseSource(Book book, CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) throws IOException {
        int sheetCount = book.getSheetCount();
        SpreadSheetCollection[] result = new SpreadSheetCollection[sheetCount];
        for (int i = 0; i < result.length; i++) {
            result[i] = parseCollection(book.getSheet(i), i, toName, toDate, toNumber, frequency, aggregationType, clean);
        }
        return new SpreadSheetSource(Arrays.asList(result), book.getFactoryName());
    }

    @VisibleForTesting
    static SpreadSheetCollection parseCollection(Sheet sheet, int ordering, CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) {
        switch (parseAlignType(sheet, toName, toDate)) {
            case VERTICAL:
                return loadVertically(SpreadSheetCollection.AlignType.VERTICAL, ordering, sheet, toName, toDate, toNumber, frequency, aggregationType, clean);
            case HORIZONTAL:
                return loadVertically(SpreadSheetCollection.AlignType.HORIZONTAL, ordering, sheet.inv(), toName, toDate, toNumber, frequency, aggregationType, clean);
            case UNKNOWN:
                return new SpreadSheetCollection(sheet.getName(), ordering, SpreadSheetCollection.AlignType.UNKNOWN, ImmutableList.<SpreadSheetSeries>of());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @VisibleForTesting
    static SpreadSheetCollection.AlignType parseAlignType(Sheet sheet, CellParser<String> toName, CellParser<Date> toDate) {
        if (sheet.getRowCount() < 2 || sheet.getColumnCount() < 2) {
            return SpreadSheetCollection.AlignType.UNKNOWN;
        }

        if (toName.tryParse(sheet, 0, 1).isPresent() && toDate.tryParse(sheet, 1, 0).isPresent()) {
            return SpreadSheetCollection.AlignType.VERTICAL;
        }

        if (toDate.tryParse(sheet, 0, 1).isPresent() && toName.tryParse(sheet, 1, 0).isPresent()) {
            return SpreadSheetCollection.AlignType.HORIZONTAL;
        }

        return SpreadSheetCollection.AlignType.UNKNOWN;
    }

    private static final int FIRST_DATA_ROW_IDX = 1;
    private static final int FIRST_DATA_COL_IDX = 1;
    private static final int DATE_COL_IDX = 0;
    private static final int NAME_ROW_IDX = 0;

    private static List<Date> getVerticalDates(Sheet sheet, CellParser<Date> toDate) {
        List<Date> result = new ArrayList<>();
        for (int rowIdx = FIRST_DATA_ROW_IDX; rowIdx < sheet.getRowCount(); rowIdx++) {
            Date date = toDate.parse(sheet, rowIdx, DATE_COL_IDX);
            if (date == null) {
                break;
            }
            result.add(date);
        }
        return result;
    }

    private static List<String> getHorizontalNames(Sheet sheet, CellParser<String> toName) {
        List<String> result = new ArrayList<>();
        for (int columnIdx = FIRST_DATA_COL_IDX; columnIdx < sheet.getColumnCount(); columnIdx++) {
            String name = toName.parse(sheet, NAME_ROW_IDX, columnIdx);
            if (name == null) {
                break;
            }
            result.add(name);
        }
        return result;
    }

    private static SpreadSheetCollection loadVertically(SpreadSheetCollection.AlignType alignType, int ordering, Sheet sheet, CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) {
        List<Date> dates = getVerticalDates(sheet, toDate);
        List<String> names = getHorizontalNames(sheet, toName);

        ImmutableList.Builder<SpreadSheetSeries> list = ImmutableList.builder();

        OptionalTsData.Builder data = new OptionalTsData.Builder(frequency, aggregationType, clean);
        for (int columnIdx = 0; columnIdx < names.size(); columnIdx++) {
            for (int rowIdx = 0; rowIdx < dates.size(); rowIdx++) {
                Number value = toNumber.parse(sheet, rowIdx + FIRST_DATA_ROW_IDX, columnIdx + FIRST_DATA_COL_IDX);
                data.add(dates.get(rowIdx), value);
            }
            list.add(new SpreadSheetSeries(names.get(columnIdx), columnIdx, alignType, data.build()));
            data.clear();
        }

        return new SpreadSheetCollection(sheet.getName(), ordering, alignType, list.build());
    }
    //</editor-fold>
}
