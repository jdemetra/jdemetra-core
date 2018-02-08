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

import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.TableAsCubeAccessor.AllSeriesCursor;
import demetra.tsprovider.cube.TableAsCubeAccessor.AllSeriesWithDataCursor;
import demetra.tsprovider.cube.TableAsCubeAccessor.ChildrenCursor;
import demetra.tsprovider.cube.TableAsCubeAccessor.SeriesCursor;
import demetra.tsprovider.cube.TableAsCubeAccessor.SeriesWithDataCursor;
import demetra.tsprovider.cube.TableAsCubeAccessor.TableCursor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SqlTableAsCubeUtil {

    public final Collector<? super String, ?, String> LABEL_COLLECTOR = Collectors.joining(", ");

    public AllSeriesCursor allSeriesCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<String> toLabel, CubeId ref) {
        return new ResultSetAllSeriesCursor(rs, closeable, toDimValues, toLabel, ref);
    }

    public AllSeriesWithDataCursor<Date> allSeriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel, CubeId ref) {
        return new ResultSetAllSeriesWithDataCursor(rs, closeable, toDimValues, toPeriod, toValue, toLabel, ref);
    }

    public SeriesWithDataCursor<Date> seriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel, CubeId ref) {
        return new ResultSetSeriesWithDataCursor(rs, closeable, toPeriod, toValue, toLabel, ref);
    }

    public ChildrenCursor childrenCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String> toChild) {
        return new ResultSetChildrenCursor(rs, closeable, toChild);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static abstract class ResultSetTableCursor implements TableCursor {

        private final ResultSet rs;
        private final AutoCloseable closeable;
        private boolean closed;

        private ResultSetTableCursor(ResultSet rs, AutoCloseable closeable) {
            this.rs = rs;
            this.closeable = closeable;
            this.closed = false;
        }

        protected abstract void processRow(ResultSet rs) throws SQLException;

        @Override
        public boolean isClosed() throws Exception {
            // java.lang.AbstractMethodError if ResultSet#isClosed() is not implemented
            // return rs.isClosed();
            return closed;
        }

        @Override
        public boolean nextRow() throws Exception {
            boolean result = rs.next();
            if (result) {
                processRow(rs);
            }
            return result;
        }

        @Override
        public void close() throws Exception {
            closed = true;
            closeable.close();
        }
    }

    private static abstract class ResultSetSeriesCursor extends ResultSetTableCursor implements SeriesCursor {

        private ResultSetSeriesCursor(ResultSet rs, AutoCloseable closeable) {
            super(rs, closeable);
        }

        @Override
        public Map<String, String> getMetaData() throws Exception {
            return Collections.emptyMap();
        }
    }

    private static final class ResultSetAllSeriesCursor extends ResultSetSeriesCursor implements AllSeriesCursor {

        private final ResultSetFunc<String[]> toDimValues;
        private final ResultSetFunc<String> toLabel;
        private final CubeId ref;
        private String[] dimValues;
        private String label;

        private ResultSetAllSeriesCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<String> toLabel, CubeId ref) {
            super(rs, closeable);
            this.toDimValues = toDimValues;
            this.toLabel = toLabel;
            this.ref = ref;
            this.dimValues = null;
            this.label = null;
        }

        @Override
        public String getLabel() throws Exception {
            return label != null ? label : Stream.concat(ref.getDimensionValueStream(), Stream.of(dimValues)).collect(LABEL_COLLECTOR);
        }

        @Override
        public String[] getDimValues() throws Exception {
            return dimValues;
        }

        @Override
        protected void processRow(ResultSet rs) throws SQLException {
            dimValues = toDimValues.applyWithSql(rs);
            label = toLabel.applyWithSql(rs);
        }
    }

    private static final class ResultSetAllSeriesWithDataCursor extends ResultSetSeriesCursor implements AllSeriesWithDataCursor<Date> {

        private final ResultSetFunc<String[]> toDimValues;
        private final ResultSetFunc<Date> toPeriod;
        private final ResultSetFunc<Number> toValue;
        private final ResultSetFunc<String> toLabel;
        private final CubeId ref;
        private String[] dimValues;
        private java.util.Date period;
        private Number value;
        private String label;

        private ResultSetAllSeriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String[]> toDimValues, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel, CubeId ref) {
            super(rs, closeable);
            this.toDimValues = toDimValues;
            this.toPeriod = toPeriod;
            this.toValue = toValue;
            this.toLabel = toLabel;
            this.ref = ref;
            this.dimValues = null;
            this.period = null;
            this.value = null;
            this.label = null;
        }

        @Override
        public String getLabel() throws Exception {
            return label != null ? label : Stream.concat(ref.getDimensionValueStream(), Stream.of(dimValues)).collect(LABEL_COLLECTOR);
        }

        @Override
        public String[] getDimValues() throws Exception {
            return dimValues;
        }

        @Override
        public java.util.Date getPeriod() throws Exception {
            return period;
        }

        @Override
        public Number getValue() throws Exception {
            return value;
        }

        @Override
        protected void processRow(ResultSet rs) throws SQLException {
            dimValues = toDimValues.applyWithSql(rs);
            period = toPeriod.applyWithSql(rs);
            value = period != null ? toValue.applyWithSql(rs) : null;
            label = toLabel.applyWithSql(rs);
        }
    }

    private static final class ResultSetSeriesWithDataCursor extends ResultSetSeriesCursor implements SeriesWithDataCursor<Date> {

        private final ResultSetFunc<Date> toPeriod;
        private final ResultSetFunc<Number> toValue;
        private final ResultSetFunc<String> toLabel;
        private final CubeId ref;
        private java.util.Date period;
        private Number value;
        private String label;

        private ResultSetSeriesWithDataCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<Date> toPeriod, ResultSetFunc<Number> toValue, ResultSetFunc<String> toLabel, CubeId ref) {
            super(rs, closeable);
            this.toPeriod = toPeriod;
            this.toValue = toValue;
            this.toLabel = toLabel;
            this.ref = ref;
            this.period = null;
            this.value = null;
            this.label = null;
        }

        @Override
        public String getLabel() throws Exception {
            return label != null ? label : ref.getDimensionValueStream().collect(LABEL_COLLECTOR);
        }

        @Override
        public java.util.Date getPeriod() throws Exception {
            return period;
        }

        @Override
        public Number getValue() throws Exception {
            return value;
        }

        @Override
        protected void processRow(ResultSet rs) throws SQLException {
            period = toPeriod.applyWithSql(rs);
            value = period != null ? toValue.applyWithSql(rs) : null;
            label = toLabel.applyWithSql(rs);
        }
    }

    private static final class ResultSetChildrenCursor extends ResultSetTableCursor implements ChildrenCursor {

        private final ResultSetFunc<String> toChild;
        private String child;

        private ResultSetChildrenCursor(ResultSet rs, AutoCloseable closeable, ResultSetFunc<String> toChild) {
            super(rs, closeable);
            this.toChild = toChild;
            this.child = null;
        }

        @Override
        public String getChild() throws Exception {
            return child;
        }

        @Override
        protected void processRow(ResultSet rs) throws SQLException {
            child = toChild.applyWithSql(rs);
        }
    }
    //</editor-fold>
}
