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
package ec.tss;

import static ec.tss.Utils.NULL_MONIKER;
import static ec.tss.Utils.NULL_TS_COLLECTION_INFO;
import static ec.tss.Utils.NULL_TS_INFO;
import java.util.Optional;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import static ec.tss.Utils.throwDescription;

/**
 * {@link ITsProvider} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class ITsProviderAssert extends AbstractAssert<ITsProviderAssert, ITsProvider> {

    /**
     * Creates a new <code>{@link ITsProviderAssert}</code> to make assertions
     * on actual ITsProvider.
     *
     * @param actual the ITsProvider we want to make assertions on.
     */
    public ITsProviderAssert(@NonNull ITsProvider actual) {
        super(actual, ITsProviderAssert.class);
    }

    public static <P extends ITsProvider> void assertCompliance(@NonNull Supplier<P> factory, @NonNull Sampler<P> sampler) {
        SoftAssertions s = new SoftAssertions();
        checkClearCache(s, factory, sampler);
        checkDispose(s, factory, sampler);
        checkClose(s, factory, sampler);
        checkGetCollection(s, factory, sampler);
        checkGetSeries(s, factory, sampler);
        checkGetAsyncMode(s, factory, sampler);
        checkGetSource(s, factory, sampler);
        checkIsAvailable(s, factory, sampler);
        checkQuerySeries(s, factory, sampler);
        checkQueryCollection(s, factory, sampler);
        s.assertAll();
    }

    public interface Sampler<P extends ITsProvider> {

        @NonNull
        default Optional<TsCollectionInformation> tsCollectionInformation(@NonNull P p) {
            return tsMoniker(p).map(ITsProviderAssert::syncQueryForCollection);
        }

        @NonNull
        default Optional<TsInformation> tsInformation(@NonNull P p) {
            return tsMoniker(p).map(ITsProviderAssert::syncQueryForSeries);
        }

        @NonNull
        Optional<TsMoniker> tsMoniker(@NonNull P p);

        @NonNull
        Optional<TsMoniker> tsCollectionMoniker(@NonNull P p);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static <P extends ITsProvider> void checkClearCache(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends ITsProvider> void checkDispose(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends ITsProvider> void checkClose(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends ITsProvider> void checkGetCollection(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.get(NULL_TS_COLLECTION_INFO))
                    .as(throwDescription(p, "get(nullTsCollectionInformation)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
//            sampler.validTsCollectionInformation(p).ifPresent(o -> {
//                s.assertThat(p.get(o)).isTrue();
//            });
        }
    }

    private static <P extends ITsProvider> void checkGetSeries(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.get(NULL_TS_INFO))
                    .as(throwDescription(p, "get(nullTsInformation)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
//            sampler.validTsInformation(p).ifPresent(o -> {
//                s.assertThat(p.get(o)).isTrue();
//            });
        }
    }

    private static <P extends ITsProvider> void checkGetAsyncMode(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getAsyncMode())
                    .as("AsyncMode cannot be null")
                    .isNotNull();
        }
    }

    private static <P extends ITsProvider> void checkGetSource(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getSource())
                    .as("Source cannot be empty")
                    .isNotEmpty();
        }
    }

    private static <P extends ITsProvider> void checkIsAvailable(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
    }

    private static <P extends ITsProvider> void checkQuerySeries(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.queryTs(NULL_MONIKER, TsInformationType.All))
                    .as(throwDescription(p, "queryTs(nullMoniker, TsInformationType)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static <P extends ITsProvider> void checkQueryCollection(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.queryTsCollection(NULL_MONIKER, TsInformationType.All))
                    .as(throwDescription(p, "queryTsCollection(nullMoniker, TsInformationType)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static TsCollectionInformation syncQueryForCollection(TsMoniker o) {
        return new TsCollectionInformation(o, TsInformationType.All);
    }

    private static TsInformation syncQueryForSeries(TsMoniker o) {
        TsInformation result = new TsInformation();
        result.moniker = o;
        result.type = TsInformationType.All;
        return result;
    }
    //</editor-fold>
}
