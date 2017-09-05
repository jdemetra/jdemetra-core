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
package internal.spreadsheet;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.legacy.InvalidMonikerException;
import java.io.File;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SpreadSheetLegacy {

    public interface Converter {

        DataSource toSource(File file);

        DataSet toCollection(DataSource dataSource, String sheetName);

        DataSet toSeries(DataSource dataSource, String sheetName, String seriesName);

        DataSet toSeries(DataSource dataSource, String sheetName, int seriesIndex);
    }

    public DataSource parseLegacyDataSource(CharSequence input, Converter converter) {
        FileDataSourceId id = FileDataSourceId.parse(input);
        return id != null ? converter.toSource(new File(id.getFile())) : null;
    }

    public DataSet parseLegacyDataSet(CharSequence input, Converter converter) {
        try {
            SpreadSheetId id = SpreadSheetId.parse(input.toString());
            DataSource dataSource = parseLegacyDataSource(id.getFileName(), converter);
            if (dataSource == null) {
                return null;
            }
            if (id.isCollection()) {
                return converter.toCollection(dataSource, id.getSheetName());
            }
            return id.getIndexSeries() >= 0
                    ? converter.toSeries(dataSource, id.getSheetName(), id.getIndexSeries())
                    : converter.toSeries(dataSource, id.getSheetName(), id.getSeriesName());
        } catch (Exception ex) {
            return null;
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
}
