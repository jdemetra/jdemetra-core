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

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.legacy.InvalidMonikerException;
import ec.tss.tsproviders.utils.IParser;
import ec.tss.tsproviders.utils.Parsers;

/**
 *
 * @author Philippe Charles
 */
final class TxtLegacy {

    private TxtLegacy() {
        // static class
    }

    static DataSource newDataSource(FileDataSourceId id) {
        return id.fill(new TxtBean()).toDataSource(TxtProvider.SOURCE, TxtProvider.VERSION);
    }

    static Parsers.Parser<DataSource> dataSourceParser() {
        return new Parsers.Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                FileDataSourceId id = FileDataSourceId.parse(input);
                return id != null ? newDataSource(id) : null;
            }
        };
    }

    static Parsers.Parser<DataSet> dataSetParser() {
        final IParser<DataSource> tmp = dataSourceParser();
        return new Parsers.Parser<DataSet>() {
            @Override
            public DataSet parse(CharSequence input) throws NullPointerException {
                TxtId id = TxtId.parse(input.toString());
                if (id == null) {
                    return null;
                }
                DataSource dataSource = tmp.parse(id.getFileName());
                if (dataSource == null) {
                    return null;
                }
                if (!id.isSeries()) {
                    return DataSet.of(dataSource, DataSet.Kind.COLLECTION);
                }
                return DataSet.builder(dataSource, DataSet.Kind.SERIES)
                        .put(TxtProvider.Z_SERIESINDEX, id.getIndexSeries())
                        .build();
            }
        };
    }

    /**
     *
     * @author Demortier Jeremy
     */
    @Deprecated
    private static class TxtId {

        public static final String SEP = "@";
        private String shortFile_;
        private int indexSeries_ = -1;

        public String getFileName() {
            return shortFile_;
        }

        public int getIndexSeries() {
            return indexSeries_;
        }

        public static TxtId collection(String sfile) {
            TxtId id = new TxtId();
            id.shortFile_ = sfile;
            return id;
        }

        public static TxtId series(String sfile, int pos) {
            TxtId id = new TxtId();
            id.shortFile_ = sfile;
            id.indexSeries_ = pos;
            return id;
        }

        public static TxtId parse(String monikerId) throws InvalidMonikerException {
            String[] parts = monikerId.split(SEP);

            if (parts.length > 2) {
                return null;
            }

            try {
                TxtId id = new TxtId();

                // No break on purpose : a moniker with x parts has indeed all parts from 0 to x-1
                switch (parts.length) {
                    case 2:
                        id.indexSeries_ = Integer.parseInt(parts[1]);
                    case 1:
                        id.shortFile_ = parts[0];
                }
                return id;
            } catch (NumberFormatException err) {
                return null;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(shortFile_);
            if (isSeries()) {
                builder.append(SEP).append(indexSeries_);
            }
            return builder.toString();
        }

        public boolean isSeries() {
            return indexSeries_ >= 0;
        }
    }
}
