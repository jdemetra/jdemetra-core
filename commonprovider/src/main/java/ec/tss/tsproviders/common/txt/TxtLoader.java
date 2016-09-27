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
package ec.tss.tsproviders.common.txt;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import ec.tss.tsproviders.common.txt.TxtBean.Delimiter;
import ec.tss.tsproviders.common.txt.TxtBean.TextQualifier;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.ObsGathering;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Parsers;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;

/**
 *
 * @author Philippe Charles
 */
final class TxtLoader {

    private TxtLoader() {
        // static class
    }

    public static TxtSource load(File realFile, TxtBean bean) throws IOException {
        try (Reader reader = Files.newBufferedReader(realFile.toPath(), bean.charset)) {
            return load(reader, bean);
        }
    }

    public static TxtSource load(InputStream inputStream, TxtBean bean) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, bean.charset)) {
            return load(reader, bean);
        }
    }

    static TxtSource load(Reader reader, TxtBean bean) throws IOException {
        try (CSVReader csvReader = new CSVReader(reader, toChar(bean.delimiter), toChar(bean.textQualifier), bean.skipLines)) {
            return load(csvReader, bean);
        }
    }

    static TxtSource load(CSVReader reader, TxtBean bean) throws IOException {
        int nbrRows = 0;
        int nbrUselessRows = 0;
        String[] titles = new String[0];
        Parsers.Parser<Date> dateParser = bean.dataFormat.dateParser().or(FALLBACK_PARSER.get());
        Parsers.Parser<Number> numberParser = bean.dataFormat.numberParser();
        GregorianCalendar cal = new GregorianCalendar();
        List<OptionalTsData.Builder2<Date>> dataCollectors = new ArrayList<>();

        String[] line;
        while ((line = reader.readNext()) != null) {
            if (nbrRows == 0) {
                titles = bean.headers ? line : generateHeaders(line.length);
                ObsGathering gathering = bean.cleanMissing
                        ? ObsGathering.excludingMissingValues(bean.frequency, bean.aggregationType)
                        : ObsGathering.includingMissingValues(bean.frequency, bean.aggregationType);
                for (int i = 1; i < titles.length; i++) {
                    dataCollectors.add(OptionalTsData.builderByDate(cal, gathering));
                }
            }
            if (!(nbrRows == 0 && bean.headers)) {
                Date period = line.length > 0 ? dateParser.parse(line[0]) : null;
                if (period != null) {
                    int max = Math.min(titles.length, line.length);
                    for (int i = 1; i < max; i++) {
                        dataCollectors.get(i - 1).add(period, numberParser.parse(line[i]));
                    }
                } else {
                    nbrUselessRows++;
                }
            }
            nbrRows++;
        }

        TxtSeries[] data = new TxtSeries[titles.length - 1];
        for (int i = 0; i < data.length; ++i) {
            data[i] = new TxtSeries(i, titles[i + 1], dataCollectors.get(i).build());
        }
        return new TxtSource(nbrRows, nbrUselessRows, Arrays.asList(data));
    }

    static String[] generateHeaders(int size) {
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = "Column " + i;
        }
        return result;
    }

    static char toChar(Delimiter delimiter) {
        switch (delimiter) {
            case COMMA:
                return ',';
            case SEMICOLON:
                return ';';
            case SPACE:
                return ' ';
            case TAB:
                return '\t';
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static char toChar(TextQualifier textQualifier) {
        switch (textQualifier) {
            case DOUBLE_QUOTE:
                return '"';
            case QUOTE:
                return '\'';
            case NONE:
                return CSVParser.DEFAULT_QUOTE_CHARACTER;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // needed by the use of SimpleDateFormat in the subparsers
    private static final ThreadLocal<Parsers.Parser<Date>> FALLBACK_PARSER = new ThreadLocal<Parsers.Parser<Date>>() {
        @Override
        protected Parsers.Parser<Date> initialValue() {
            IParser<Date>[] list = new IParser[FALLBACK_FORMATS.length];
            for (int i = 0; i < list.length; i++) {
                list[i] = new DataFormat(Locale.ROOT, FALLBACK_FORMATS[i], null).dateParser();
            }
            return Parsers.firstNotNull(list);
        }
    };
    // fallback formats; order matters!
    private static final String[] FALLBACK_FORMATS = {
        "yyyy-MM-dd",
        "yyyy MM dd",
        "yyyy.MM.dd",
        "yyyy-MMM-dd",
        "yyyy MMM dd",
        "yyyy.MMM.dd",
        "dd-MM-yyyy",
        "dd MM yyyy",
        "dd.MM.yyyy",
        "dd/MM/yyyy",
        "dd-MM-yy",
        "dd MM yy",
        "dd.MM.yy",
        "dd/MM/yy",
        "dd-MMM-yy",
        "dd MMM yy",
        "dd.MMM.yy",
        "dd/MMM/yy",
        "dd-MMM-yyyy",
        "dd MMM yyyy",
        "dd.MMM.yyyy",
        "dd/MMM/yyyy",
        "yyyy-MM-dd hh:mm:ss",
        "yyyy MM dd hh:mm:ss",
        "yyyy.MM.dd hh:mm:ss",
        "yyyy/MM/dd hh:mm:ss",
        "yyyy-MMM-dd hh:mm:ss",
        "yyyy MMM dd hh:mm:ss",
        "yyyy.MMM.dd hh:mm:ss",
        "yyyy/MMM/dd hh:mm:ss",
        "dd-MM-yyyy hh:mm:ss",
        "dd MM yyyy hh:mm:ss",
        "dd.MM.yyyy hh:mm:ss",
        "dd/MM/yyyy hh:mm:ss",
        "dd-MMM-yyyy hh:mm:ss",
        "dd MMM yyyy hh:mm:ss",
        "dd.MMM.yyyy hh:mm:ss",
        "dd/MMM/yyyy hh:mm:ss"};
}
