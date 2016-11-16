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

package ec.tss.tsproviders.common.xml;

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
final class XmlLegacy {

    private XmlLegacy() {
        // static class
    }

    static DataSource newDataSource(FileDataSourceId id) {
        return id.fill(new XmlBean()).toDataSource(XmlProvider.SOURCE, XmlProvider.VERSION);
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
                XmlId id = XmlId.parse(input.toString());
                if (id == null) {
                    return null;
                }
                DataSource dataSource = tmp.parse(id.getFileName());
                if (dataSource == null) {
                    return null;
                }
                if (!id.isSeries()) {
                    DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
                    XmlProvider.Y_COLLECTIONINDEX.set(builder, id.getIndexCollection());
                    return builder.build();
                } else {
                    DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
                    XmlProvider.Y_COLLECTIONINDEX.set(builder, id.getIndexCollection());
                    XmlProvider.Z_SERIESINDEX.set(builder, id.getIndexSeries());
                    return builder.build();
                }
            }
        };
    }

    /**
     *
     * @author Demortier Jeremy
     */
    private static class XmlId {

        public static final String SEP = "@";
        private String shortFile_;
        private int indexCollection_ = -1;
        private int indexSeries_ = -1;

        public String getFileName() {
            return shortFile_;
        }

        public int getIndexCollection() {
            return indexCollection_;
        }

        public int getIndexSeries() {
            return indexSeries_;
        }

        public static XmlId collection(String sfile, int pos) {
            XmlId id = new XmlId();
            id.shortFile_ = sfile;
            id.indexCollection_ = pos;
            return id;
        }

        public static XmlId series(String sfile, int cpos, int spos) {
            XmlId id = new XmlId();
            id.shortFile_ = sfile;
            id.indexCollection_ = cpos;
            id.indexSeries_ = spos;
            return id;
        }

        public static XmlId parse(String monikerId) throws InvalidMonikerException {
            String[] parts = monikerId.split(SEP);

            if (parts.length > 3) {
                return null;
            }

            try {
                XmlId id = new XmlId();

                // No break on purpose : a moniker with x parts has indeed all parts from 0 to x-1
                switch (parts.length) {
                    case 3:
                        id.indexSeries_ = Integer.parseInt(parts[2]);
                    case 2:
                        id.indexCollection_ = Integer.parseInt(parts[1]);
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
            builder.append(shortFile_).append(SEP).append(indexCollection_);
            if (isSeries()) {
                builder.append(SEP).append(indexSeries_);
            }
            return builder.toString();
        }

        public boolean isCollection() {
            return -1 == indexSeries_;
        }

        public boolean isSeries() {
            return indexSeries_ >= 0;
        }
    }
}
