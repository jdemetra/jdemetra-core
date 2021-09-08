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
package demetra.tsprovider.stream;

import demetra.timeseries.*;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.util.DataSourcePreconditions;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@ThreadSafe
public final class TsStreamAsProvider implements TsProvider {

    /**
     * Creates a new instance of TsFiller using cursors.
     *
     * @param providerName
     * @param hdc          a non-null ts cursor support
     * @param hdm          a non-null data moniker support
     * @param cacheCleaner
     * @return a non-null instance
     */
    @NonNull
    public static TsStreamAsProvider of(@NonNull String providerName, @NonNull HasTsStream hdc, @NonNull HasDataMoniker hdm, @NonNull Runnable cacheCleaner) {
        return new TsStreamAsProvider(providerName, hdc, hdm, cacheCleaner);
    }

    private final String providerName;
    private final HasTsStream htc;
    private final HasDataMoniker hdm;
    private final Runnable cacheCleaner;

    private TsStreamAsProvider(String providerName, HasTsStream htc, HasDataMoniker hdm, Runnable cacheCleaner) {
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

        TsCollection.Builder result = TsCollection.builder().moniker(moniker).type(type);

        Optional<DataSource> dataSource = hdm.toDataSource(moniker);
        if (dataSource.isPresent()) {
            fill(result, dataSource.get(), type);
            return result.build();
        }

        Optional<DataSet> dataSet = hdm.toDataSet(moniker);
        if (dataSet.filter(TsStreamAsProvider::isCollection).isPresent()) {
            fill(result, dataSet.get(), type);
            return result.build();
        }

        throw new IllegalArgumentException("Invalid moniker");
    }

    @Override
    public Ts getTs(TsMoniker moniker, TsInformationType type) throws IOException, IllegalArgumentException {
        DataSourcePreconditions.checkProvider(getSource(), moniker);

        Ts.Builder result = Ts.builder().moniker(moniker).type(type);

        Optional<DataSet> dataSet = hdm.toDataSet(moniker);
        if (dataSet.filter(TsStreamAsProvider::isSeries).isPresent()) {
            fill(result, dataSet.get(), type);
            return result.build();
        }

        throw new IllegalArgumentException("Invalid moniker");
    }

    @Override
    public String getSource() {
        return providerName;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private static boolean isCollection(DataSet dataSet) {
        return DataSet.Kind.COLLECTION.equals(dataSet.getKind());
    }

    private static boolean isSeries(DataSet dataSet) {
        return DataSet.Kind.SERIES.equals(dataSet.getKind());
    }

    private void fill(TsCollection.Builder info, DataSource dataSource, TsInformationType type) throws IOException {
        try (Stream<DataSetTs> stream = htc.getData(dataSource, type)) {
            fill(info, stream, type);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private void fill(TsCollection.Builder info, DataSet dataSet, TsInformationType type) throws IOException {
        try (Stream<DataSetTs> stream = htc.getData(dataSet, type)) {
            fill(info, stream, type);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private void fill(Ts.Builder builder, DataSet dataSet, TsInformationType type) throws IOException {
        try (Stream<DataSetTs> stream = htc.getData(dataSet, type)) {
            DataSetTs single = stream.findFirst().orElseThrow(() -> new IOException("Missing time series"));
            fill(builder, single, type);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private void fill(TsCollection.Builder builder, Stream<DataSetTs> cursor, TsInformationType type) {
        if (type.encompass(TsInformationType.MetaData)) {
            // is there relevant meta ?
        }
        cursor.map(tsInfo -> {
            Ts.Builder item = Ts.builder();
            item.moniker(hdm.toMoniker(tsInfo.getId())).type(type);
            fill(item, tsInfo, type);
            return item.build();
        }).forEach(builder::item);
    }

    private void fill(Ts.Builder builder, DataSetTs tsInfo, TsInformationType type) {
        builder.clearMeta();
        builder.name(tsInfo.getLabel());
        if (type.encompass(TsInformationType.MetaData)) {
            builder.meta(tsInfo.getMeta());
        }
        if (type.encompass(TsInformationType.Data)) {
            builder.data(tsInfo.getData());
        }
    }
}
