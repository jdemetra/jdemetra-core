package demetra.timeseries;

import _util.MockedTsProvider;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static demetra.timeseries.TsInformationType.BaseInformation;
import static demetra.timeseries.TsInformationType.Data;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class TsCollectionTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.of((Ts) null));

        assertThatNullPointerException()
                .isThrownBy(() -> TsCollection.of((Iterable<Ts>) null));

        Ts ts = Ts.of(TsData.empty("abc"));
        TsCollection col = TsCollection.of(ts);

        assertThat(TsCollection.of(col)).isEqualTo(col);
    }

    @Test
    public void testToTsCollection() {
        assertThat(Stream.<Ts>empty().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.EMPTY);

        List<Ts> list = IntStream.range(0, 100).mapToObj(i -> Ts.builder().name("ts" + i).build()).collect(Collectors.toList());

        assertThat(list.stream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(list));

        assertThat(list.parallelStream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(list));
    }

    @Test
    public void testLoadOfProvided() {
        TsCollection provided = factory.makeTsCollection(colMoniker, BaseInformation);

        assertThatNullPointerException()
                .isThrownBy(() -> provided.load(null, factory));

        assertThatNullPointerException()
                .isThrownBy(() -> provided.load(Data, null));

        for (TsInformationType info : TsInformationType.values()) {
            if (provided.getType().encompass(info)) {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided collection can be modified by 'load' if old type encompasses new one")
                        .isSameAs(provided);
            } else {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided collection must not be modified by 'load' if old type doesn't not encompass new one")
                        .isNotSameAs(provided);
            }
        }
    }

    @Test
    public void testLoadOfAnonymous() {
        TsCollection anonymous = TsCollection.of(Ts.of(TsData.empty("abc")));

        assertThatNullPointerException()
                .isThrownBy(() -> anonymous.load(null, factory));

        for (TsInformationType info : TsInformationType.values()) {
            assertThatNullPointerException()
                    .isThrownBy(() -> anonymous.load(info, null));
            assertThat(anonymous.load(info, factory))
                    .describedAs("Anonymous collection must not be modified by 'load'")
                    .isEqualTo(anonymous);
        }
    }

    @Test
    public void testToList() {
        TsCollection col = Stream.of(
                Ts.builder().name("t1").build(),
                Ts.builder().name("t2").build()
        ).collect(TsCollection.toTsCollection());

        assertThat(col.toList()).containsExactlyElementsOf(col.getItems());
    }

    private final TsMoniker colMoniker = TsMoniker.of(MockedTsProvider.NAME, "0");

    private final List<TsProvider> providers = Collections.singletonList(
            MockedTsProvider
                    .builder()
                    .tsCollection(TsCollection.builder().moniker(colMoniker).build())
                    .build()
    );

    private final TsFactory factory = TsFactory.of(providers);
}
