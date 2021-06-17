package demetra.timeseries;

import _util.FailingTsProvider;
import _util.MockedTsProvider;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static demetra.timeseries.TsInformationType.All;
import static org.assertj.core.api.Assertions.*;

public class TsFactoryTest {

    @Test
    public void testFactories() {
        assertThatCode(() -> TsFactory.ofServiceLoader()).doesNotThrowAnyException();
    }

    @Test
    public void testMakeTs() {
        TsFactory x = TsFactory.of(providers);

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTs(null, All));

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTs(TsMoniker.NULL, (TsInformationType) null));

        assertThat(x.makeTs(TsMoniker.of(MockedTsProvider.NAME, ""), All))
                .isEqualTo(Ts.builder().moniker(TsMoniker.of(MockedTsProvider.NAME, "")).type(All).build());

        assertThat(x.makeTs(TsMoniker.NULL, All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Provider not found"));

        assertThat(x.makeTs(TsMoniker.of(FailingTsProvider.NAME, ""), All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Boom"));
    }

    @Test
    public void testMakeTsCollection() {
        TsFactory x = TsFactory.of(providers);

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTsCollection(null, All));

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTsCollection(TsMoniker.NULL, (TsInformationType) null));

        assertThat(x.makeTsCollection(TsMoniker.of(MockedTsProvider.NAME, ""), All))
                .isEqualTo(TsCollection.builder().moniker(TsMoniker.of(MockedTsProvider.NAME, "")).type(All).build());

        assertThat(x.makeTsCollection(TsMoniker.NULL, All))
                .satisfies(o -> assertThat(((TsCollection) o).getEmptyCause()).isEqualTo("Provider not found"))
                .isEmpty();

        assertThat(x.makeTs(TsMoniker.of(FailingTsProvider.NAME, ""), All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Boom"));
    }

    private List<TsProvider> providers = Arrays.asList(
            MockedTsProvider.builder().build(),
            new FailingTsProvider()
    );
}
