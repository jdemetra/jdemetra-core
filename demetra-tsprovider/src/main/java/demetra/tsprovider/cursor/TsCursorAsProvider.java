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
package demetra.tsprovider.cursor;

import demetra.tsprovider.TsCollection;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.TsMoniker;
import demetra.tsprovider.TsProvider;
import demetra.tsprovider.util.DataSourcePreconditions;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public final class TsCursorAsProvider implements TsProvider {

    /**
     * Creates a new instance of TsFiller using cursors.
     *
     * @param providerName
     * @param hdc a non-null ts cursor support
     * @param hdm a non-null data moniker support
     * @param cacheCleaner
     * @return a non-null instance
     */
    @Nonnull
    public static TsCursorAsProvider of(@Nonnull String providerName, @Nonnull HasTsCursor hdc, @Nonnull HasDataMoniker hdm, @Nonnull Runnable cacheCleaner) {
        return new TsCursorAsProvider(providerName, hdc, hdm, cacheCleaner);
    }

    private final String providerName;
    private final HasTsCursor htc;
    private final HasDataMoniker hdm;
    private final Runnable cacheCleaner;

    private TsCursorAsProvider(String providerName, HasTsCursor htc, HasDataMoniker hdm, Runnable cacheCleaner) {
        this.providerName = Objects.requireNonNull(providerName, "providerName");
        this.htc = Objects.requireNonNull(htc, "HasTsCursor");
        this.hdm = Objects.requireNonNull(hdm, "HasDataMoniker");
        this.cacheCleaner = Objects.requireNonNull(cacheCleaner, "cacheCleaner");
    }

    @Override
    public void clearCache() {
        cacheCleaner.run();
    }

    @Override
    public void close() {
        clearCache();
    }

    @Override
    public TsCollection getTsCollection(TsMoniker moniker, TsInformationType type) throws IOException, IllegalArgumentException {
        DataSourcePreconditions.checkProvider(getSource(), moniker);
        TsCollection.Builder result = TsCollection.builder().moniker(moniker).type(type).name("");
        fillCollection(result);
        return result.build();
    }

    @Override
    public Ts getTs(TsMoniker moniker, TsInformationType type) throws IOException, IllegalArgumentException {
        DataSourcePreconditions.checkProvider(getSource(), moniker);
        Ts.Builder result = Ts.builder().moniker(moniker).type(type);
        fillSeries(result);
        return result.build();
    }

    @Override
    public String getSource() {
        return providerName;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * Fills a collection info according to its request.
     *
     * @param info the collection info to fill
     * @throws java.io.IOException
     */
    private void fillCollection(TsCollection.Builder info) throws IOException {
        DataSource dataSource = toDataSource(info);
        if (isCollection(dataSource)) {
            fill(info, dataSource);
            return;
        }

        DataSet dataSet = toDataSet(info);
        if (isCollection(dataSet)) {
            fill(info, dataSet);
            return;
        }

        throw new IllegalArgumentException("Invalid moniker");
    }

    /**
     * Fills a time series info according to its request.
     *
     * @param info the time series info to fill
     * @throws java.io.IOException
     */
    private void fillSeries(Ts.Builder info) throws IOException {
        DataSet dataSet = toDataSet(info);
        if (isSeries(dataSet)) {
            fill(info, dataSet);
            return;
        }

        throw new IllegalArgumentException("Invalid moniker");
    }

    private static boolean isCollection(DataSource dataSource) {
        return dataSource != null;
    }

    private static boolean isCollection(DataSet dataSet) {
        return dataSet != null && DataSet.Kind.COLLECTION.equals(dataSet.getKind());
    }

    private static boolean isSeries(DataSet dataSet) {
        return dataSet != null && DataSet.Kind.SERIES.equals(dataSet.getKind());
    }

    private DataSource toDataSource(TsCollection.Builder info) {
        return hdm.toDataSource(info.getMoniker());
    }

    private DataSet toDataSet(TsCollection.Builder info) {
        return hdm.toDataSet(info.getMoniker());
    }

    private DataSet toDataSet(Ts.Builder info) {
        return hdm.toDataSet(info.getMoniker());
    }

    private void fill(TsCollection.Builder info, DataSource dataSource) throws IOException {
        try (TsCursor<DataSet> cursor = htc.getData(dataSource, info.getType())) {
            fill(info, cursor);
        }
    }

    private void fill(TsCollection.Builder info, DataSet dataSet) throws IOException {
        try (TsCursor<DataSet> cursor = htc.getData(dataSet, info.getType())) {
            fill(info, cursor);
        }
    }

    private void fill(Ts.Builder info, DataSet dataSet) throws IOException {
        try (TsCursor<DataSet> cursor = htc.getData(dataSet, info.getType())) {
            if (cursor.nextSeries()) {
                info.name(cursor.getSeriesLabel());
                fill(info, cursor);
            } else {
                throw new IOException("Missing time series");
            }
        }
    }

    private void fill(TsCollection.Builder info, TsCursor<DataSet> cursor) throws IOException {
        if (info.getType().encompass(TsInformationType.MetaData)) {
            info.metaData(cursor.getMetaData());
        }
        Ts.Builder item = Ts.builder();
        while (cursor.nextSeries()) {
            item.name(cursor.getSeriesLabel())
                    .moniker(hdm.toMoniker(cursor.getSeriesId()))
                    .type(info.getType());
            fill(item, cursor);
            info.item(item.build());
        }
    }

    private void fill(Ts.Builder info, TsCursor<DataSet> cursor) throws IOException {
        info.clearMetaData();
        if (info.getType().encompass(TsInformationType.MetaData)) {
            info.metaData(cursor.getSeriesMetaData());
        }
        if (info.getType().encompass(TsInformationType.Data)) {
            info.data(cursor.getSeriesData());
        }
    }
}
