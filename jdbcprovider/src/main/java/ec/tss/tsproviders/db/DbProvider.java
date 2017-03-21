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
package ec.tss.tsproviders.db;

import com.google.common.base.Joiner;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.*;
import ec.tss.tsproviders.utils.AbstractDataSourceLoader;
import ec.tss.tsproviders.utils.IConfig.Builder;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Arrays2;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

/**
 * @author Demortier Jeremy
 * @author Philippe Charles
 * @param <BEAN>
 */
public abstract class DbProvider<BEAN extends DbBean> extends AbstractDataSourceLoader<DbAccessor<BEAN>, BEAN> {

    private static final IParam<DataSet, Dims> DIM_MAP = new DimsParam();

    public DbProvider(Logger logger, String providerName, TsAsyncMode asyncMode) {
        super(logger, providerName, asyncMode);
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        DbBean bean = decodeBean(dataSource);
        String options = TsFrequency.Undefined == bean.getFrequency() ? "" : OptionalTsData.Builder.toString(bean.getFrequency(), bean.getAggregationType());
        return String.format("%s ~ %s \u00BB %s %s", bean.getDbName(), bean.getTableName(), bean.getValueColumn(), options);
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        support.check(dataSet);
        Dims dims = DIM_MAP.get(dataSet);
        return dims.dimValues.length > 0 ? Joiner.on(", ").join(dims.dimValues) : "All";
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) {
        support.check(dataSet);
        Dims dims = DIM_MAP.get(dataSet);
        return dims.dimValues.length > 0 ? dims.dimValues[dims.dimValues.length - 1] : "All";
    }

    @Override
    public String getDisplayName(IOException ex) throws IllegalArgumentException {
        return ex instanceof ChildrenException ? ex.getCause().getMessage() : super.getDisplayName(ex);
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        support.check(dataSource);
        DbAccessor<BEAN> accessor = getAccessor(dataSource);
        Dims dims = new Dims(DbBean.getDimArray(dataSource));

        DataSet fake = DataSet.of(dataSource, DataSet.Kind.SERIES);
        // special case: we return a fake dataset if no dimColumns
        if (dims.dimColumns.length == 0) {
            Exception ex = accessor.testDbBean();
            if (ex != null) {
                throw new ChildrenException("Cannot list DataSource children", ex);
            }
            return Collections.singletonList(fake);
        }

        try {
            return children(fake, dims, accessor.getChildren(dims.dimValues));
        } catch (Exception ex) {
            throw new ChildrenException("Cannot list DataSource children", ex);
        }
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        support.check(parent, DataSet.Kind.COLLECTION);
        DbAccessor<BEAN> accessor = getAccessor(parent.getDataSource());
        Dims dims = DIM_MAP.get(parent);

        try {
            return children(parent, dims, accessor.getChildren(dims.dimValues));
        } catch (Exception ex) {
            throw new ChildrenException("Cannot list DataSet children", ex);
        }
    }

    @Nonnull
    private List<DataSet> children(@Nonnull DataSet parent, @Nonnull Dims dims, @Nonnull List<String> values) {
        if (values.isEmpty()) {
            return Collections.emptyList();
        }
        DataSet[] children = new DataSet[values.size()];
        DataSet.Builder builder = parent.toBuilder(dims.hasGrandChildren() ? DataSet.Kind.COLLECTION : DataSet.Kind.SERIES);
        String childDimColumn = dims.childDimColumn();
        for (int i = 0; i < children.length; i++) {
            builder.put(childDimColumn, values.get(i));
            children[i] = builder.build();
        }
        return Arrays.asList(children);
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSource dataSource) throws IOException {
        DbAccessor<BEAN> acc = getAccessor(dataSource);
        try {
            if (info.type.intValue() >= TsInformationType.Data.intValue()) {
                info.type = TsInformationType.All;
                info.items.addAll(getAllWithData(dataSource, acc.getAllSeriesWithData()));
            } else {
                info.items.addAll(getAll(dataSource, acc.getAllSeries(), info.type));
            }
        } catch (Exception ex) {
            throw new IOException("Cannot retrieve DataSource data", ex);
        }
    }

    @Override
    protected void fillCollection(TsCollectionInformation info, DataSet dataSet) throws IOException {
        DbAccessor<BEAN> acc = getAccessor(dataSet.getDataSource());
        Dims dims = DIM_MAP.get(dataSet);
        try {
            if (info.type.intValue() >= TsInformationType.Data.intValue()) {
                info.type = TsInformationType.All;
                info.items.addAll(getAllWithData(dataSet.getDataSource(), acc.getAllSeriesWithData(dims.dimValues)));
            } else {
                info.items.addAll(getAll(dataSet.getDataSource(), acc.getAllSeries(dims.dimValues), info.type));
            }
        } catch (Exception ex) {
            throw new IOException("Cannot retrieve DataSet data", ex);
        }
    }

    @Override
    protected void fillSeries(TsInformation info, DataSet dataSet) throws IOException {
        if (info.type.intValue() >= TsInformationType.Data.intValue()) {
            DbAccessor<BEAN> acc = getAccessor(dataSet.getDataSource());
            Dims dims = DIM_MAP.get(dataSet);
            info.name = getDisplayName(dataSet);
            info.type = TsInformationType.All;
            try {
                support.fillSeries(info, acc.getSeriesWithData(dims.dimValues).getData(), true);
            } catch (Exception ex) {
                throw new IOException("Cannot retrieve DataSet data", ex);
            }
        }
    }

    @Nonnull
    private List<TsInformation> getAll(@Nonnull DataSource dataSource, @Nonnull List<DbSetId> list, @Nonnull TsInformationType type) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        TsInformation[] result = new TsInformation[list.size()];
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (int j = 0; j < result.length; j++) {
            DbSetId o = list.get(j);
            for (int i = 0; i < o.getLevel(); i++) {
                builder.put(o.getColumn(i), o.getValue(i));
            }
            result[j] = newTsInformation(builder.build(), type);
        }
        return Arrays.asList(result);
    }

    @Nonnull
    private List<TsInformation> getAllWithData(@Nonnull DataSource dataSource, @Nonnull List<DbSeries> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        TsInformation[] result = new TsInformation[list.size()];
        DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.SERIES);
        for (int j = 0; j < result.length; j++) {
            DbSeries o = list.get(j);
            for (int i = 0; i < o.getId().getLevel(); i++) {
                builder.put(o.getId().getColumn(i), o.getId().getValue(i));
            }
            result[j] = support.fillSeries(newTsInformation(builder.build(), TsInformationType.All), o.getData(), true);
            builder.clear();
        }
        return Arrays.asList(result);
    }

    @Nonnull
    private DbAccessor<BEAN> getAccessor(@Nonnull DataSource dataSource) throws IOException {
        return support.getValue(cache, dataSource);
    }

    private static final class Dims {

        final String[] dimColumns;
        final String[] dimValues;

        public Dims(@Nonnull String[] dimColumns) {
            this(dimColumns, Arrays2.EMPTY_STRING_ARRAY);
        }

        public Dims(@Nonnull String[] dimColumns, @Nonnull String[] dimValues) {
            // dimColumns.length >= dimValues.length
            this.dimColumns = dimColumns;
            this.dimValues = dimValues;
        }

        boolean hasGrandChildren() {
            return dimValues.length < dimColumns.length - 1;
        }

        String childDimColumn() {
            return dimColumns[dimValues.length];
        }
    }

    private static final class DimsParam implements IParam<DataSet, Dims> {

        @Override
        public Dims defaultValue() {
            return new Dims(Arrays2.EMPTY_STRING_ARRAY, Arrays2.EMPTY_STRING_ARRAY);
        }

        @Override
        public Dims get(DataSet dataSet) {
            String[] dimColumns = DbBean.getDimArray(dataSet.getDataSource());
            int length = dimColumns.length;
            while (length > 0 && dataSet.get(dimColumns[length - 1]) == null) {
                length--;
            }
            String[] dimValues = new String[length];
            for (int i = 0; i < length; i++) {
                dimValues[i] = dataSet.get(dimColumns[i]);
            }
            return new Dims(dimColumns, dimValues);
        }

        @Override
        public void set(Builder<?, DataSet> builder, Dims value) {
            if (value != null) {
                for (int i = 0; i < value.dimValues.length; i++) {
                    builder.put(value.dimColumns[i], value.dimValues[i]);
                }
            }
        }
    }

    private static final class ChildrenException extends IOException {

        public ChildrenException(@Nonnull String message, @Nonnull Throwable cause) {
            super(message, cause);
        }
    }
}
