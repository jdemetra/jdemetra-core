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
package ec.tss.tsproviders.cursor;

import data.Data;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import static ec.tss.TsInformationType.All;
import static ec.tss.TsInformationType.None;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import static ec.tss.tsproviders.DataSet.Kind.COLLECTION;
import static ec.tss.tsproviders.DataSet.Kind.SERIES;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.HasDataMoniker;
import static ec.tss.tsproviders.cursor.IdTsSupport.metaOf;
import ec.tss.tsproviders.utils.TsFiller;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.LinearId;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class TsCursorAsFillerTest {

    private final String provider = "MyProvider";
    private final Logger logger = NOPLogger.NOP_LOGGER;
    private final HasDataMoniker monikers = HasDataMoniker.usingUri(provider);
    private final HasTsCursor badCursor = new FailingTsCursorSupport(provider, "boom");

    private final MetaData customMeta = metaOf("hello", "world");

    private final IdTsSupport goodCursor = IdTsSupport.builder()
            .add(new LinearId("node", "leaf1"))
            .add(new LinearId("node", "leaf2"), Data.M1)
            .add(new LinearId("leaf3"), Data.M2, customMeta)
            .build();

    private final TsMoniker goodSource;
    private final TsMoniker goodCollection;
    private final TsMoniker goodSeries;
    private final TsMoniker leaf1;
    private final TsMoniker leaf2;
    private final TsMoniker leaf3;

    {
        DataSource ds = DataSource.builder(provider, "").build();
        goodSource = monikers.toMoniker(ds);
        goodCollection = monikers.toMoniker(DataSet.builder(ds, COLLECTION).put("id", "node").build());
        goodSeries = monikers.toMoniker(DataSet.builder(ds, SERIES).put("id", "leaf3").build());
        leaf1 = monikers.toMoniker(DataSet.builder(ds, SERIES).put("id", "node.leaf1").build());
        leaf2 = monikers.toMoniker(DataSet.builder(ds, SERIES).put("id", "node.leaf2").build());
        leaf3 = monikers.toMoniker(DataSet.builder(ds, SERIES).put("id", "leaf3").build());
    }

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        HasTsCursor noOpCursor = new NoOpTsCursorSupport(provider);
        HasDataDisplayName nameSupport = HasDataDisplayName.usingUri(provider);

        assertThatThrownBy(() -> TsCursorAsFiller.of(null, noOpCursor, monikers, nameSupport))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Logger");
        assertThatThrownBy(() -> TsCursorAsFiller.of(logger, null, monikers, nameSupport))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HasTsCursor");
        assertThatThrownBy(() -> TsCursorAsFiller.of(logger, noOpCursor, null, nameSupport))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HasDataMoniker");
        assertThatThrownBy(() -> TsCursorAsFiller.of(logger, noOpCursor, monikers, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("HasDataDisplayName");
    }

    @Test
    @SuppressWarnings("null")
    public void testNulls() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThatThrownBy(() -> filler.fillCollection(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> filler.fillSeries(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testCollectionFillSource() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThat(new TsCollectionInformation(goodSource, None)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodSource, None, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, None, null, null, null),
                            seriesInfo("node.leaf2", leaf2, None, null, null, null),
                            seriesInfo("leaf3", leaf3, None, null, null, null)
                    );
        });

        assertThat(new TsCollectionInformation(goodSource, TsInformationType.MetaData)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodSource, TsInformationType.MetaData, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, TsInformationType.MetaData, null, null, null),
                            seriesInfo("node.leaf2", leaf2, TsInformationType.MetaData, null, null, null),
                            seriesInfo("leaf3", leaf3, TsInformationType.MetaData, null, null, customMeta)
                    );
        });

        assertThat(new TsCollectionInformation(goodSource, All)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodSource, All, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, All, null, "No data available", null),
                            seriesInfo("node.leaf2", leaf2, All, Data.M1, null, null),
                            seriesInfo("leaf3", leaf3, All, Data.M2, null, customMeta)
                    );
        });
    }

    @Test
    public void testCollectionFillSet() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThat(new TsCollectionInformation(goodCollection, None)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodCollection, None, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, None, null, null, null),
                            seriesInfo("node.leaf2", leaf2, None, null, null, null)
                    );
        });

        assertThat(new TsCollectionInformation(goodCollection, TsInformationType.MetaData)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodCollection, TsInformationType.MetaData, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, TsInformationType.MetaData, null, null, null),
                            seriesInfo("node.leaf2", leaf2, TsInformationType.MetaData, null, null, null)
                    );
        });

        assertThat(new TsCollectionInformation(goodCollection, All)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isTrue();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodCollection, All, null, null), "items");
            assertThat(info.items)
                    .usingFieldByFieldElementComparator()
                    .containsExactly(
                            seriesInfo("node.leaf1", leaf1, All, null, "No data available", null),
                            seriesInfo("node.leaf2", leaf2, All, Data.M1, null, null)
                    );
        });
    }

    @Test
    public void testCollectionExSource() {
        TsFiller filler = TsCursorAsFiller.of(logger, badCursor, monikers, goodCursor);

        assertThat(new TsCollectionInformation(goodSource, All)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isFalse();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodSource, All, "boom", null), "items");
            assertThat(info.items).isEmpty();
        });
    }

    @Test
    public void testCollectionExSet() {
        TsFiller filler = TsCursorAsFiller.of(logger, badCursor, monikers, goodCursor);

        assertThat(new TsCollectionInformation(goodCollection, All)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isFalse();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodCollection, All, "boom", null), "items");
            assertThat(info.items).isEmpty();
        });
    }

    @Test
    public void testCollectionInvalid() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThat(new TsCollectionInformation(goodSeries, All)).satisfies(info -> {
            assertThat(filler.fillCollection(info)).isFalse();
            assertThat(info).isEqualToIgnoringGivenFields(colInfo(null, goodSeries, All, "Invalid moniker", null), "items");
            assertThat(info.items).isEmpty();
        });
    }

    @Test
    public void testSeriesFill() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThat(new TsInformation(null, goodSeries, None)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isTrue();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo("leaf3", goodSeries, None, null, null, null));
        });

        assertThat(new TsInformation(null, goodSeries, TsInformationType.MetaData)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isTrue();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo("leaf3", goodSeries, TsInformationType.MetaData, null, null, customMeta));
        });

        assertThat(new TsInformation(null, goodSeries, All)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isTrue();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo("leaf3", goodSeries, All, Data.M2, null, customMeta));
        });
    }

    @Test
    public void testSeriesEx() {
        TsFiller filler = TsCursorAsFiller.of(logger, badCursor, monikers, goodCursor);

        assertThat(new TsInformation(null, goodSeries, All)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isFalse();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo(null, goodSeries, All, null, "boom", null));
        });
    }

    @Test
    public void testSeriesInvalid() {
        TsFiller filler = TsCursorAsFiller.of(logger, goodCursor, monikers, goodCursor);

        assertThat(new TsInformation(null, goodSource, All)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isFalse();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo(null, goodSource, All, null, "Invalid moniker", null));
        });

        assertThat(new TsInformation(null, goodCollection, All)).satisfies(info -> {
            assertThat(filler.fillSeries(info)).isFalse();
            assertThat(info).isEqualToComparingFieldByField(seriesInfo(null, goodCollection, All, null, "Invalid moniker", null));
        });
    }

    private static TsCollectionInformation colInfo(String name, TsMoniker moniker, TsInformationType type, String invalidDataCause, MetaData meta) {
        TsCollectionInformation result = new TsCollectionInformation(moniker, type);
        result.name = name;
        result.invalidDataCause = invalidDataCause;
        result.metaData = meta;
        return result;
    }

    private static TsInformation seriesInfo(String name, TsMoniker moniker, TsInformationType type, TsData data, String invalidDataCause, MetaData meta) {
        TsInformation result = new TsInformation(name, moniker, type);
        result.data = data;
        result.invalidDataCause = invalidDataCause;
        result.metaData = meta;
        return result;
    }
}
