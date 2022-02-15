/*
 * Copyright 2017 National Bank of Belgium
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
package internal.sql;

import demetra.tsprovider.cube.TableAsCubeConnection.AllSeriesCursor;
import demetra.tsprovider.cube.TableAsCubeConnection.AllSeriesWithDataCursor;
import demetra.tsprovider.cube.TableAsCubeConnection.ChildrenCursor;
import demetra.tsprovider.cube.TableAsCubeConnection.SeriesCursor;
import demetra.tsprovider.cube.TableAsCubeConnection.SeriesWithDataCursor;
import java.sql.ResultSet;
import java.util.Date;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SqlTableAsCubeUtil {

    public AllSeriesCursor allSeriesCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<String> toLabel) {
        return new ResultSetAllSeriesCursor(rs, closeable, toDimValues, toLabel);
    }

    public AllSeriesWithDataCursor<Date> allSeriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel) {
        return new ResultSetAllSeriesWithDataCursor(rs, closeable, toDimValues, toPeriod, toValue, toLabel);
    }

    public SeriesCursor seriesCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String> toLabel) {
        return new ResultSetSeriesCursor(rs, closeable, toLabel);
    }

    public SeriesWithDataCursor<Date> seriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel) {
        return new ResultSetSeriesWithDataCursor(rs, closeable, toPeriod, toValue, toLabel);
    }

    public ChildrenCursor childrenCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String> toChild) {
        return new ResultSetChildrenCursor(rs, closeable, toChild);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @lombok.RequiredArgsConstructor
    private static final class ResultSetAllSeriesCursor implements AllSeriesCursor {

        private final ResultSet rs;
        @lombok.experimental.Delegate
        private final AutoCloseable closeable;

        private final ResultSetFunc<String[]> toDimValues;
        private final ResultSetFunc<String> toLabel;

        @lombok.Getter
        private String[] dimValues = null;

        @lombok.Getter
        private String labelOrNull = null;

        @Override
        public boolean nextRow() throws Exception {
            if (rs.next()) {
                dimValues = toDimValues.applyWithSql(rs);
                labelOrNull = toLabel.applyWithSql(rs);
                return true;
            }
            return false;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ResultSetAllSeriesWithDataCursor implements AllSeriesWithDataCursor<Date> {

        private final ResultSet rs;
        @lombok.experimental.Delegate
        private final AutoCloseable closeable;

        private final ResultSetFunc<String[]> toDimValues;
        private final ResultSetFunc<Date> toPeriod;
        private final ResultSetFunc<Number> toValue;
        private final ResultSetFunc<String> toLabel;

        @lombok.Getter
        private String[] dimValues = null;

        @lombok.Getter
        private java.util.Date periodOrNull = null;

        @lombok.Getter
        private Number valueOrNull = null;

        @lombok.Getter
        private String labelOrNull = null;

        @Override
        public boolean nextRow() throws Exception {
            if (rs.next()) {
                dimValues = toDimValues.applyWithSql(rs);
                periodOrNull = toPeriod.applyWithSql(rs);
                valueOrNull = periodOrNull != null ? toValue.applyWithSql(rs) : null;
                labelOrNull = toLabel.applyWithSql(rs);
                return true;
            }
            return false;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ResultSetSeriesCursor implements SeriesCursor {

        private final ResultSet rs;
        @lombok.experimental.Delegate
        private final AutoCloseable closeable;

        private final ResultSetFunc<String> toLabel;

        @lombok.Getter
        private String labelOrNull = null;

        @Override
        public boolean nextRow() throws Exception {
            if (rs.next()) {
                labelOrNull = toLabel.applyWithSql(rs);
                return true;
            }
            return false;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ResultSetSeriesWithDataCursor implements SeriesWithDataCursor<Date> {

        private final ResultSet rs;
        @lombok.experimental.Delegate
        private final AutoCloseable closeable;

        private final ResultSetFunc<Date> toPeriod;
        private final ResultSetFunc<Number> toValue;
        private final ResultSetFunc<String> toLabel;

        @lombok.Getter
        private java.util.Date periodOrNull = null;

        @lombok.Getter
        private Number valueOrNull = null;

        @lombok.Getter
        private String labelOrNull = null;

        @Override
        public boolean nextRow() throws Exception {
            if (rs.next()) {
                periodOrNull = toPeriod.applyWithSql(rs);
                valueOrNull = periodOrNull != null ? toValue.applyWithSql(rs) : null;
                labelOrNull = toLabel.applyWithSql(rs);
                return true;
            }
            return false;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ResultSetChildrenCursor implements ChildrenCursor {

        private final ResultSet rs;
        @lombok.experimental.Delegate
        private final AutoCloseable closeable;

        private final ResultSetFunc<String> toChild;

        @lombok.Getter
        private String child = null;

        @Override
        public boolean nextRow() throws Exception {
            if (rs.next()) {
                child = toChild.applyWithSql(rs);
                if (child == null) {
                    child = NULL_VALUE;
                }
                return true;
            }
            return false;
        }

        private static final String NULL_VALUE = "";
    }
    //</editor-fold>
}
