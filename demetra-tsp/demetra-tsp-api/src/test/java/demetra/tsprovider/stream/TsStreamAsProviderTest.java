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

import _util.tsproviders.FailingTsCursorSupport;
import demetra.timeseries.*;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataMoniker;
import internal.timeseries.util.TsDataBuilderUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static demetra.timeseries.TsInformationType.*;
import static demetra.tsprovider.DataSet.Kind.COLLECTION;
import static demetra.tsprovider.DataSet.Kind.SERIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class TsStreamAsProviderTest {

    private final String provider = "MyProvider";
    private final HasDataMoniker monikers = HasDataMoniker.usingUri(provider);
    private final HasTsStream badCursor = new FailingTsCursorSupport(provider, "boom");

    private final Map<String, String> customMeta = Map.of("hello", "world");

    private final Runnable doNothing = () -> {
    };

    private final TsMoniker goodSource;
    private final TsMoniker goodCollection;
    private final TsMoniker goodSeries;
    private final TsMoniker leaf1;
    private final TsMoniker leaf2;
    private final TsMoniker leaf3;
    private final Ts s1;
    private final Ts s2;
    private final Ts s3;
    private final HasTsStream goodCursor;

    {
        DataSource ds = DataSource.of(provider, "");
        goodSource = monikers.toMoniker(ds);
        goodCollection = monikers.toMoniker(DataSet.of(ds, COLLECTION, "id", "node"));
        goodSeries = monikers.toMoniker(DataSet.of(ds, SERIES, "id", "leaf3"));
        leaf1 = monikers.toMoniker(DataSet.of(ds, SERIES, "id", "node.leaf1"));
        leaf2 = monikers.toMoniker(DataSet.of(ds, SERIES, "id", "node.leaf2"));
        leaf3 = monikers.toMoniker(DataSet.of(ds, SERIES, "id", "leaf3"));
        s1 = Ts.builder().moniker(leaf1).type(All).name("node.leaf1").data(TsData.empty("No data available")).build();
        s2 = Ts.builder().moniker(leaf2).type(All).name("node.leaf2").data(TsData.random(TsUnit.MONTH, 2)).build();
        s3 = Ts.builder().moniker(leaf3).type(All).name("leaf3").data(TsData.random(TsUnit.MONTH, 3)).meta(customMeta).build();
        goodCursor = new HasTsStream() {
            @Override
            public Stream<DataSetTs> getData(DataSource dataSource, TsInformationType type) throws IllegalArgumentException, IOException {
                if (dataSource.equals(ds)) {
                    DataSet.Builder b = DataSet.builder(dataSource, SERIES);
                    Function<Ts, DataSet> toDataSet = o -> {
                        b.parameter("id", o.getName());
                        return b.build();
                    };
                    return Stream.of(s1, s2, s3)
                            .map(ts -> new DataSetTs(
                                    toDataSet.apply(ts),
                                    ts.getName(),
                                    type.encompass(MetaData) ? ts.getMeta() : Collections.emptyMap(),
                                    type.encompass(Data) ? ts.getData() : DataSetTs.DATA_NOT_REQUESTED)
                            );
                }
                throw new IOException();
            }

            @Override
            public Stream<DataSetTs> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
                return getData(dataSet.getDataSource(), type)
                        .filter(o -> o.getId().getParameter("id").startsWith(dataSet.getParameter("id")));
            }
        };
    }

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        HasTsStream noOpCursor = HasTsStream.noOp(provider);

        assertThatThrownBy(() -> TsStreamAsProvider.of(provider, null, monikers, doNothing))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HasTsCursor");
        assertThatThrownBy(() -> TsStreamAsProvider.of(provider, noOpCursor, null, doNothing))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HasDataMoniker");
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTsCollection(null, TsInformationType.All)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.getTs(null, TsInformationType.All)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testCollectionFillSource() throws IOException {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThat(p.getTsCollection(goodSource, None)).isEqualTo(TsCollection.builder()
                .moniker(goodSource).type(None)
                .item(s1.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build())
                .item(s2.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build())
                .item(s3.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build())
                .build());

        assertThat(p.getTsCollection(goodSource, MetaData)).isEqualTo(TsCollection.builder()
                .moniker(goodSource).type(MetaData)
                .item(s1.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build())
                .item(s2.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build())
                .item(s3.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build())
                .build());

        assertThat(p.getTsCollection(goodSource, All)).isEqualTo(TsCollection.builder()
                .moniker(goodSource).type(All)
                .item(s1).item(s2).item(s3)
                .build());
    }

    @Test
    public void testCollectionFillSet() throws IOException {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThat(p.getTsCollection(goodCollection, None)).isEqualTo(TsCollection.builder()
                .moniker(goodCollection).type(None)
                .item(s1.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build())
                .item(s2.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build())
                .build());

        assertThat(p.getTsCollection(goodCollection, MetaData)).isEqualTo(TsCollection.builder()
                .moniker(goodCollection).type(MetaData)
                .item(s1.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build())
                .item(s2.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build())
                .build());

        assertThat(p.getTsCollection(goodCollection, All)).isEqualTo(TsCollection.builder()
                .moniker(goodCollection).type(All)
                .item(s1).item(s2)
                .build());
    }

    @Test
    public void testCollectionExSource() {
        TsProvider p = TsStreamAsProvider.of(provider, badCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTsCollection(goodSource, All)).isInstanceOf(IOException.class);
    }

    @Test
    public void testCollectionExSet() {
        TsProvider p = TsStreamAsProvider.of(provider, badCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTsCollection(goodCollection, All)).isInstanceOf(IOException.class);
    }

    @Test
    public void testCollectionInvalid() {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTsCollection(goodSeries, All)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSeriesFill() throws IOException {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThat(p.getTs(goodSeries, None)).isEqualTo(s3.toBuilder().type(None).data(TsDataBuilderUtil.NO_DATA).clearMeta().build());

        assertThat(p.getTs(goodSeries, MetaData)).isEqualTo(s3.toBuilder().type(MetaData).data(TsDataBuilderUtil.NO_DATA).build());

        assertThat(p.getTs(goodSeries, All)).isEqualTo(s3);
    }

    @Test
    public void testSeriesEx() {
        TsProvider p = TsStreamAsProvider.of(provider, badCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTs(goodSeries, All)).isInstanceOf(IOException.class);
    }

    @Test
    public void testSeriesInvalid() {
        TsProvider p = TsStreamAsProvider.of(provider, goodCursor, monikers, doNothing);

        assertThatThrownBy(() -> p.getTs(goodSource, All)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> p.getTs(goodCollection, All)).isInstanceOf(IllegalArgumentException.class);
    }
}
