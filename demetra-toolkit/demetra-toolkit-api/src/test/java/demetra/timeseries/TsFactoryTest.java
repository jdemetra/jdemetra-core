package demetra.timeseries;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
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
        List<String> errors = new ArrayList<>();
        TsFactory x = TsFactory
                .builder()
                .providers(() -> providers)
                .onIOException((msg, ex) -> errors.add(ex.getMessage()))
                .build();

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTs(null, All));

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTs(TsMoniker.NULL, null));

        assertThat(x.makeTs(TsMoniker.of(MockedTsProvider.NAME, ""), All))
                .isEqualTo(Ts.builder().moniker(TsMoniker.of(MockedTsProvider.NAME, "")).type(All).build());
        assertThat(errors).isEmpty();

        assertThat(x.makeTs(TsMoniker.NULL, All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Provider not found"));
        assertThat(errors).isEmpty();

        assertThat(x.makeTs(TsMoniker.of(FailingTsProvider.NAME, ""), All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Boom"));
        assertThat(errors)
                .hasSize(1)
                .contains("Boom", atIndex(0));
    }

    @Test
    public void testMakeTsCollection() {
        List<String> errors = new ArrayList<>();
        TsFactory x = TsFactory
                .builder()
                .providers(() -> providers)
                .onIOException((msg, ex) -> errors.add(ex.getMessage()))
                .build();

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTsCollection(null, All));

        assertThatNullPointerException()
                .isThrownBy(() -> x.makeTsCollection(TsMoniker.NULL, null));

        assertThat(x.makeTsCollection(TsMoniker.of(MockedTsProvider.NAME, ""), All))
                .isEqualTo(TsCollection.builder().moniker(TsMoniker.of(MockedTsProvider.NAME, "")).type(All).build());
        assertThat(errors).isEmpty();

        assertThat(x.makeTsCollection(TsMoniker.NULL, All))
                .satisfies(o -> assertThat(((TsCollection) o).getEmptyCause()).isEqualTo("Provider not found"))
                .isEmpty();
        assertThat(errors).isEmpty();

        assertThat(x.makeTs(TsMoniker.of(FailingTsProvider.NAME, ""), All))
                .extracting(Ts::getData)
                .isEqualTo(TsData.empty("Boom"));
        assertThat(errors)
                .hasSize(1)
                .contains("Boom", atIndex(0));
    }

    private List<TsProvider> providers = Arrays.asList(
            new MockedTsProvider(),
            new FailingTsProvider()
    );

    private static class MockedTsProvider implements TsProvider {

        public static final String NAME = "Mocked";

        @Override
        public void clearCache() {
        }

        @Override
        public void close() {
        }

        @Override
        public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException {
            return TsCollection.builder().moniker(moniker).type(type).build();
        }

        @Override
        public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException {
            return Ts.builder().moniker(moniker).type(type).build();
        }

        @Override
        public @NonNull String getSource() {
            return NAME;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    private static class FailingTsProvider implements TsProvider {

        public static final String NAME = "Failing";

        @Override
        public void clearCache() {
        }

        @Override
        public void close() {
        }

        @Override
        public @NonNull TsCollection getTsCollection(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
            throw new IOException("Boom");
        }

        @Override
        public @NonNull Ts getTs(@NonNull TsMoniker moniker, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
            throw new IOException("Boom");
        }

        @Override
        public @NonNull String getSource() {
            return NAME;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}
