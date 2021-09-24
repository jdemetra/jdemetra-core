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
package demetra.tsprovider.tck;

import demetra.timeseries.*;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link TsProvider} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class TsProviderAssert extends AbstractAssert<TsProviderAssert, TsProvider> {

    /**
     * Creates a new <code>{@link TsProviderAssert}</code> to make assertions
     * on actual ITsProvider.
     *
     * @param actual the ITsProvider we want to make assertions on.
     */
    public TsProviderAssert(@NonNull TsProvider actual) {
        super(actual, TsProviderAssert.class);
    }

    public static <P extends TsProvider> void assertCompliance(@NonNull Supplier<P> factory, @NonNull Sampler<P> sampler) {
        SoftAssertions s = new SoftAssertions();
        checkClearCache(s, factory, sampler);
        checkDispose(s, factory, sampler);
        checkClose(s, factory, sampler);
        checkGetCollection(s, factory, sampler);
        checkGetSeries(s, factory, sampler);
        checkGetSource(s, factory, sampler);
        checkIsAvailable(s, factory, sampler);
        s.assertAll();
    }

    public interface Sampler<P extends TsProvider> {

        @NonNull
        default Optional<TsCollection.Builder> tsCollectionInformation(@NonNull P p) {
            return tsMoniker(p).map(TsProviderAssert::syncQueryForCollection);
        }

        @NonNull
        default Optional<Ts.Builder> tsInformation(@NonNull P p) {
            return tsMoniker(p).map(TsProviderAssert::syncQueryForSeries);
        }

        @NonNull
        Optional<TsMoniker> tsMoniker(@NonNull P p);

        @NonNull
        Optional<TsMoniker> tsCollectionMoniker(@NonNull P p);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static <P extends TsProvider> void checkClearCache(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends TsProvider> void checkDispose(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends TsProvider> void checkClose(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends TsProvider> void checkGetCollection(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getTsCollection(Utils.NULL_MONIKER, TsInformationType.All))
                    .as(Utils.throwDescription(p, "get(nullTsCollectionInformation)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
//            sampler.validTsCollectionInformation(p).ifPresent(o -> {
//                s.assertThat(p.get(o)).isTrue();
//            });
        }
    }

    private static <P extends TsProvider> void checkGetSeries(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getTs(Utils.NULL_MONIKER, TsInformationType.All))
                    .as(Utils.throwDescription(p, "get(nullTsInformation)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
//            sampler.validTsInformation(p).ifPresent(o -> {
//                s.assertThat(p.get(o)).isTrue();
//            });
        }
    }

    private static <P extends TsProvider> void checkGetSource(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getSource())
                    .as("Source cannot be empty")
                    .isNotEmpty();
        }
    }

    private static <P extends TsProvider> void checkIsAvailable(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static TsCollection.Builder syncQueryForCollection(TsMoniker o) {
        return TsCollection.builder().moniker(o).type(TsInformationType.All);
    }

    private static Ts.Builder syncQueryForSeries(TsMoniker o) {
        return Ts.builder().moniker(o).type(TsInformationType.All);
    }
    //</editor-fold>
}
