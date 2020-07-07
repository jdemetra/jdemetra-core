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
package ec.tss.tsproviders.spreadsheet;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.TsProviders;
import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.legacy.InvalidMonikerException;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetCollection;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetSource;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.Parsers;
import ec.tss.tsproviders.utils.Parsers.FailSafeParser;
import ec.tss.tsproviders.utils.Parsers.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
final class SpreadSheetLegacy {

    private SpreadSheetLegacy() {
        // static class
    }

    static Parsers.@NonNull Parser<DataSource> legacyDataSourceParser() {
        return new Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                FileDataSourceId id = FileDataSourceId.parse(input);
                return id != null ? newDataSource(id) : null;
            }
        };
    }

    static Parsers.@NonNull Parser<DataSet> legacyDataSetParser() {
        final IParser<DataSource> tmp = legacyDataSourceParser();
        return new FailSafeParser<DataSet>() {
            @Override
            protected DataSet doParse(CharSequence input) throws Exception {
                SpreadSheetId id = SpreadSheetId.parse(input.toString());
                DataSource dataSource = tmp.parse(id.getFileName());
                if (dataSource == null) {
                    return null;
                }
                if (id.isCollection()) {
                    return DataSet.builder(dataSource, DataSet.Kind.COLLECTION)
                            .put(SpreadSheetProvider.Y_SHEETNAME, id.getSheetName())
                            .build();
                }
                String seriesName = searchSeriesName(dataSource, id);
                return DataSet.builder(dataSource, DataSet.Kind.SERIES)
                        .put(SpreadSheetProvider.Y_SHEETNAME, id.getSheetName())
                        .put(SpreadSheetProvider.Z_SERIESNAME, seriesName)
                        .build();
            }
        };
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static DataSource newDataSource(FileDataSourceId id) {
        return id.fill(new SpreadSheetBean()).toDataSource(SpreadSheetProvider.SOURCE, SpreadSheetProvider.VERSION);
    }

    private static String searchSeriesName(DataSource dataSource, SpreadSheetId id) {
        int sid = id.getIndexSeries();
        if (sid >= 0) {
            return SeriesNameResolver.INSTANCE.resolveName(dataSource, id.getSheetName(), id.getIndexSeries());
        }
        return id.getSeriesName();
    }

    private enum SeriesNameResolver {

        INSTANCE;

        public String resolveName(DataSource dataSource, String sheetName, int index) {
            SpreadSheetProvider tmp = TsProviders.lookup(SpreadSheetProvider.class, dataSource).get();
            SpreadSheetSource col;
            try {
                col = tmp.getSource(dataSource);
            } catch (Exception ex) {
                return null;
            }
            SpreadSheetCollection cur = SpreadSheetProvider.search(col, sheetName);
            if (cur == null) {
                return null;
            }
            return index < cur.series.size() ? cur.series.get(index).seriesName : null;
        }
    }

    private static final class SpreadSheetId {

        public static final String BSEP = "<<", ESEP = ">>";
        private String shortFile_;
        private String sheetName_;
        private String seriesName_;
        private int indexSeries_ = -1;

        public String getFileName() {
            return shortFile_;
        }

        public String getSheetName() {
            return sheetName_;
        }

        public int getIndexSeries() {
            return indexSeries_;
        }

        public String getSeriesName() {
            return seriesName_;
        }

        public static SpreadSheetId collection(String sfile, String name) {
            SpreadSheetId id = new SpreadSheetId();
            id.shortFile_ = sfile;
            id.sheetName_ = name;
            return id;
        }

        public static SpreadSheetId series(String sfile, String sheetName, int spos) {
            SpreadSheetId id = new SpreadSheetId();
            id.shortFile_ = sfile;
            id.sheetName_ = sheetName;
            id.indexSeries_ = spos;
            return id;
        }

        public static SpreadSheetId series(String sfile, String sheetName, String sname) {
            SpreadSheetId id = new SpreadSheetId();
            id.shortFile_ = sfile;
            id.sheetName_ = sheetName;
            id.seriesName_ = sname;
            return id;
        }

        public static SpreadSheetId parse(String monikerId) throws InvalidMonikerException {
            int beg = monikerId.indexOf(BSEP);
            if (beg != 0) {
                throw new InvalidMonikerException(monikerId);
            }
            beg += BSEP.length();
            int end = monikerId.indexOf(ESEP, beg);
            if (end < 0) {
                throw new InvalidMonikerException(monikerId);
            }
            String fname = monikerId.substring(beg, end);
            beg = end + ESEP.length();
            beg = monikerId.indexOf(BSEP, beg);
            if (beg < 0) {
                throw new InvalidMonikerException(monikerId);
            }
            beg += BSEP.length();
            end = monikerId.indexOf(ESEP, beg);
            if (end < 0) {
                throw new InvalidMonikerException(monikerId);
            }
            String sheetname = monikerId.substring(beg, end);

            beg = end + ESEP.length();
            if (beg < monikerId.length()) {
                beg = monikerId.indexOf(BSEP, beg);
                if (beg < 0) {
                    throw new InvalidMonikerException(monikerId);
                }

                beg += BSEP.length();
                end = monikerId.indexOf(ESEP, beg);
                if (end < 0) {
                    throw new InvalidMonikerException();
                }
                String s = monikerId.substring(beg, end);
                try {
                    int sid = Short.parseShort(s);
                    return series(fname, sheetname, sid);
                } catch (NumberFormatException err) {
                }
                return series(fname, sheetname, s);
            } else {
                return collection(fname, sheetname);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(BSEP).append(shortFile_).append(ESEP).append(BSEP).append(sheetName_).append(ESEP);
            if (isSeries()) {
                if (indexSeries_ >= 0) {
                    builder.append(BSEP).append(indexSeries_).append(ESEP);
                } else {
                    builder.append(BSEP).append(seriesName_).append(ESEP);
                }
            }
            return builder.toString();
        }

        public boolean isCollection() {
            return -1 == indexSeries_ && seriesName_ == null;
        }

        public boolean isSeries() {
            return indexSeries_ >= 0 || seriesName_ != null;
        }
    }
    //</editor-fold>
}
