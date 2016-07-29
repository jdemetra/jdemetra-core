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
package ec.tss.tsproviders.common.tsw;

import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.DoubleList;
import ec.tstoolkit.utilities.IntList;
import ec.tstoolkit.utilities.Tokenizer;
import ec.tstoolkit.utilities.CheckedIterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
abstract class TswFactory {

    @Nonnull
    abstract public TswSource load(@Nonnull Path repository) throws IOException;

    @Nonnull
    abstract public List<TswSeries> loadFile(@Nonnull Path file) throws IOException;

    @Nonnull
    public static TswFactory getDefault() {
        return NewTswFactory.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static abstract class ATswFactory extends TswFactory {

        @Override
        public TswSource load(@Nonnull Path repository) throws IOException {
            List<TswSeries> result = new ArrayList<>();
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(repository, getFilter())) {
                for (Path file : ds) {
                    try {
                        result.addAll(loadFile(file));
                    } catch (MalformedInputException ex) {
                        // not an ASCII file
                    }
                }
            }
            return new TswSource(repository.toFile(), result);
        }

        @Nonnull
        abstract protected DirectoryStream.Filter<Path> getFilter();

        @Override
        public List<TswSeries> loadFile(Path file) throws IOException {
            String fileName = file.getFileName().toString();
            try (BufferedReader reader = Files.newBufferedReader(file, TSW_CHARSET)) {
                return loadFile(fileName, CheckedIterator.fromBufferedReader(reader));
            }
        }

        @Nonnull
        abstract protected List<TswSeries> loadFile(String fileName, CheckedIterator<String, IOException> iterator) throws IOException;
    }

    private enum TswFilters implements DirectoryStream.Filter<Path> {

        ANY_FILE {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return !Files.isDirectory(entry);
                    }
                },
        FILE_WITHOUT_EXTENSION {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return !Files.isDirectory(entry) && !entry.getFileName().toString().contains(".");
                    }
                }
    }

    @VisibleForTesting
    static final class OldTswFactory extends ATswFactory {

        @VisibleForTesting
        static final OldTswFactory INSTANCE = new OldTswFactory();

        @Override
        protected DirectoryStream.Filter<Path> getFilter() {
            return TswFilters.FILE_WITHOUT_EXTENSION;
        }

        @Override
        protected List<TswSeries> loadFile(String fileName, CheckedIterator<String, IOException> iterator) throws IOException {
            if (iterator.hasNext()) {
                TswSeries series = loadSeries(fileName, iterator);
                if (series != null) {
                    return Collections.singletonList(series);
                }
            }
            return Collections.emptyList();
        }
    }

    @VisibleForTesting
    static final class NewTswFactory extends ATswFactory {

        @VisibleForTesting
        static final NewTswFactory INSTANCE = new NewTswFactory();

        @Override
        protected DirectoryStream.Filter<Path> getFilter() {
            return TswFilters.ANY_FILE;
        }

        @Override
        protected List<TswSeries> loadFile(String fileName, CheckedIterator<String, IOException> iterator) throws IOException {
            List<TswSeries> result = new ArrayList<>();
            while (iterator.hasNext()) {
                TswSeries series = loadSeries(fileName, iterator);
                if (series != null) {
                    result.add(series);
                }
            }
            return result;
        }
    }

    @Nullable
    private static TswSeries loadSeries(@Nonnull String fileName, @Nonnull CheckedIterator<String, IOException> iterator) throws IOException {
        // Read first line (name)
        String name;
        if (!(iterator.hasNext() && (name = NameParser.INSTANCE.parse(iterator.next())) != null)) {
            return null;
        }

        // Read second line (domain)
        TsDomain domain;
        if (!(iterator.hasNext() && (domain = DomainParser.INSTANCE.parse(iterator.next())) != null)) {
            return null;
        }

        // Read remaining lines until all values are parsed
        DoubleList values = new DoubleList();
        Parsers.Parser<Number> valueParser = new ValueParser();
        while (iterator.hasNext() && values.size() < domain.getLength()) {
            Tokenizer tokenizer = new Tokenizer(iterator.next());
            while (tokenizer.hasNextToken()) {
                String token = tokenizer.nextToken();
                if (values.size() < domain.getLength()) {
                    Number value = valueParser.parse(token);
                    if (value == null) {
                        return new TswSeries(fileName, name, OptionalTsData.absent("Cannot parse value at line " + (values.size() + 2)));
                    }
                    double tmp = value.doubleValue();
                    values.add(tmp == TSW_NAN ? Double.NaN : tmp);
                }
            }
        }

        TsData result = new TsData(domain.getStart(), values.toArray(), false);
        return new TswSeries(fileName, name, OptionalTsData.present(result));
    }

    private static final Charset TSW_CHARSET = StandardCharsets.US_ASCII;
    private static final int TSW_NAN = -99999;

    private final static class NameParser extends Parsers.Parser<String> {

        private static final NameParser INSTANCE = new NameParser();

        @Override
        public String parse(CharSequence input) throws NullPointerException {
            String result = input.toString().trim();
            return result.isEmpty() ? null : result;
        }
    }

    private final static class DomainParser extends Parsers.Parser<TsDomain> {

        private static final DomainParser INSTANCE = new DomainParser();

        @Override
        public TsDomain parse(CharSequence input) throws NullPointerException {
            // domain : lines, year, period, frequency
            IntList params = parseIntegers(input.toString());

            // Checking params
            if (params == null || params.size() != 4) {
                return null;
            }
            int count = params.get(0);
            int year = params.get(1);
            int period = params.get(2);
            int freq = params.get(3);

            if (count < 0) {
                return null;
            }
            if (freq < 1 || freq > 12 || 12 % freq != 0) {
                return null;
            }
            if (period <= 0 || period > freq) {
                return null;
            }

            return new TsDomain(TsFrequency.valueOf(freq), year, period - 1, count);
        }

        @Nullable
        private static IntList parseIntegers(String input) {
            IntList result = new IntList();
            Tokenizer tokenizer = new Tokenizer(input);
            Parsers.Parser<Integer> paramParser = Parsers.intParser();
            while (tokenizer.hasNextToken()) {
                String token = tokenizer.nextToken();
                Integer tmp = paramParser.parse(token);
                if (tmp == null) {
                    return null;
                }
                result.add(tmp);
            }
            return result;
        }
    }

    private final static class ValueParser extends Parsers.FailSafeParser<Number> {

        private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.ROOT);

        @Override
        protected Number doParse(CharSequence input) throws Exception {
            String tmp = input.toString().trim();
            if (tmp.startsWith("+")) {
                tmp = tmp.substring(1);
            }
            return numberFormat.parse(tmp);
        }
    }
    //</editor-fold>
}
