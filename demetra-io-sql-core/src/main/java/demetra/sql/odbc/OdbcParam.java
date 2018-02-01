/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.sql.odbc;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.ObsGathering;
import static demetra.tsprovider.util.Params.onInteger;
import static demetra.tsprovider.util.Params.onLong;
import static demetra.tsprovider.util.Params.onObsFormat;
import static demetra.tsprovider.util.Params.onObsGathering;
import static demetra.tsprovider.util.Params.onString;
import static demetra.tsprovider.util.Params.onStringList;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
interface OdbcParam extends IParam<DataSource, OdbcBean> {

    String getVersion();

    @Nonnull
    IParam<DataSet, CubeId> getIdParam(@Nonnull DataSource dataSource);

    static final class V1 implements OdbcParam {

        private final Splitter dimensionSplitter = Splitter.on(',').trimResults().omitEmptyStrings();
        private final Joiner dimensionJoiner = Joiner.on(',');

        private final IParam<DataSource, String> dsn = onString("", "dbName");
        private final IParam<DataSource, String> table = onString("", "tableName");
        private final IParam<DataSource, List<String>> dimColumns = onStringList(ImmutableList.of(), "dimColumns", o -> dimensionSplitter.splitToList(o).stream(), o -> dimensionJoiner.join(o.iterator()));
        private final IParam<DataSource, String> periodColumn = onString("", "periodColumn");
        private final IParam<DataSource, String> valueColumn = onString("", "valueColumn");
        private final IParam<DataSource, ObsFormat> dataFormat = onObsFormat(ObsFormat.DEFAULT, "locale", "datePattern", "numberPattern");
        private final IParam<DataSource, String> versionColumn = onString("", "versionColumn");
        private final IParam<DataSource, String> labelColumn = onString("", "labelColumn");
        private final IParam<DataSource, ObsGathering> obsGathering = onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final IParam<DataSource, Long> cacheTtl = onLong(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), "cacheTtl");
        private final IParam<DataSource, Integer> cacheDepth = onInteger(1, "cacheDepth");

        @Override
        public String getVersion() {
            return "20111201";
        }

        @Override
        public OdbcBean defaultValue() {
            OdbcBean result = new OdbcBean();
            result.setDsn(dsn.defaultValue());
            result.setTable(table.defaultValue());
            result.setDimColumns(dimColumns.defaultValue());
            result.setPeriodColumn(periodColumn.defaultValue());
            result.setValueColumn(valueColumn.defaultValue());
            result.setObsFormat(dataFormat.defaultValue());
            result.setVersionColumn(versionColumn.defaultValue());
            result.setLabelColumn(labelColumn.defaultValue());
            result.setObsGathering(obsGathering.defaultValue());
            result.setCacheTtl(Duration.ofMillis(cacheTtl.defaultValue()));
            result.setCacheDepth(cacheDepth.defaultValue());
            return result;
        }

        @Override
        public OdbcBean get(DataSource dataSource) {
            OdbcBean result = new OdbcBean();
            result.setDsn(dsn.get(dataSource));
            result.setTable(table.get(dataSource));
            result.setDimColumns(dimColumns.get(dataSource));
            result.setPeriodColumn(periodColumn.get(dataSource));
            result.setValueColumn(valueColumn.get(dataSource));
            result.setObsFormat(dataFormat.get(dataSource));
            result.setVersionColumn(versionColumn.get(dataSource));
            result.setLabelColumn(labelColumn.get(dataSource));
            result.setObsGathering(obsGathering.get(dataSource));
            result.setCacheTtl(Duration.ofMillis(cacheTtl.get(dataSource)));
            result.setCacheDepth(cacheDepth.get(dataSource));
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, OdbcBean value) {
            dsn.set(builder, value.getDsn());
            table.set(builder, value.getTable());
            dimColumns.set(builder, value.getDimColumns());
            periodColumn.set(builder, value.getPeriodColumn());
            valueColumn.set(builder, value.getValueColumn());
            dataFormat.set(builder, value.getObsFormat());
            versionColumn.set(builder, value.getVersionColumn());
            labelColumn.set(builder, value.getLabelColumn());
            obsGathering.set(builder, value.getObsGathering());
            cacheTtl.set(builder, value.getCacheTtl().toMillis());
            cacheDepth.set(builder, value.getCacheDepth());
        }

        @Override
        public IParam<DataSet, CubeId> getIdParam(DataSource dataSource) {
            return CubeSupport.idByName(CubeId.root(dimColumns.get(dataSource)));
        }
    }
}
