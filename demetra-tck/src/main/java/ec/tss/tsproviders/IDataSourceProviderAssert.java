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
package ec.tss.tsproviders;

import ec.tss.ITsProviderAssert;
import ec.tss.TsMoniker;
import static ec.tss.tsproviders.Utils.BAD_DATA_SET;
import static ec.tss.tsproviders.Utils.BAD_DATA_SOURCE;
import static ec.tss.tsproviders.Utils.BAD_MONIKER;
import static ec.tss.tsproviders.Utils.NULL_DATA_SET;
import static ec.tss.tsproviders.Utils.NULL_DATA_SOURCE;
import static ec.tss.tsproviders.Utils.NULL_IO_EXCEPTION;
import static ec.tss.tsproviders.Utils.NULL_MONIKER;
import java.util.Optional;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.internal.Iterables;
import org.assertj.core.util.Objects;
import static ec.tss.tsproviders.Utils.throwDescription;

/**
 * {@link IDataSourceProvider} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class IDataSourceProviderAssert extends AbstractAssert<IDataSourceProviderAssert, IDataSourceProvider> {

    /**
     * Creates a new <code>{@link IDataSourceProviderAssert}</code> to make
     * assertions on actual IDataSourceProvider.
     *
     * @param actual the IDataSourceProvider we want to make assertions on.
     */
    public IDataSourceProviderAssert(@NonNull IDataSourceProvider actual) {
        super(actual, IDataSourceProviderAssert.class);
    }

    /**
     * An entry point for IDataSourceProviderAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myIDataSourceProvider)</code> and get specific assertion
     * with code completion.
     *
     * @param actual the IDataSourceProvider we want to make assertions on.
     * @return a new <code>{@link IDataSourceProviderAssert}</code>
     */
    @NonNull
    public static IDataSourceProviderAssert assertThat(@NonNull IDataSourceProvider actual) {
        return new IDataSourceProviderAssert(actual);
    }

    /**
     * Verifies that the actual IDataSourceProvider's asyncMode is equal to the
     * given one.
     *
     * @param asyncMode the given asyncMode to compare the actual
     * IDataSourceProvider's asyncMode to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceProvider's asyncMode is
     * not equal to the given one.
     */
    @NonNull
    public IDataSourceProviderAssert hasAsyncMode(ec.tss.TsAsyncMode asyncMode) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting asyncMode of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        ec.tss.TsAsyncMode actualAsyncMode = actual.getAsyncMode();
        if (!Objects.areEqual(actualAsyncMode, asyncMode)) {
            failWithMessage(assertjErrorMessage, actual, asyncMode, actualAsyncMode);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider's dataSources contains the
     * given DataSource elements.
     *
     * @param dataSources the given elements that should be contained in actual
     * IDataSourceProvider's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceProvider's dataSources
     * does not contain all given DataSource elements.
     */
    @NonNull
    public IDataSourceProviderAssert hasDataSources(DataSource... dataSources) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // check that given DataSource varargs is not null.
        if (dataSources == null) {
            failWithMessage("Expecting dataSources parameter not to be null.");
        }

        // check with standard error message, to set another message call: info.overridingErrorMessage("my error message");
        Iterables.instance().assertContains(info, actual.getDataSources(), dataSources);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider's dataSources contains
     * <b>only</b> the given DataSource elements and nothing else in whatever
     * order.
     *
     * @param dataSources the given elements that should be contained in actual
     * IDataSourceProvider's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceProvider's dataSources
     * does not contain all given DataSource elements.
     */
    @NonNull
    public IDataSourceProviderAssert hasOnlyDataSources(DataSource... dataSources) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // check that given DataSource varargs is not null.
        if (dataSources == null) {
            failWithMessage("Expecting dataSources parameter not to be null.");
        }

        // check with standard error message, to set another message call: info.overridingErrorMessage("my error message");
        Iterables.instance().assertContainsOnly(info, actual.getDataSources(), dataSources);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider's dataSources does not
     * contain the given DataSource elements.
     *
     * @param dataSources the given elements that should not be in actual
     * IDataSourceProvider's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceProvider's dataSources
     * contains any given DataSource elements.
     */
    @NonNull
    public IDataSourceProviderAssert doesNotHaveDataSources(DataSource... dataSources) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // check that given DataSource varargs is not null.
        if (dataSources == null) {
            failWithMessage("Expecting dataSources parameter not to be null.");
        }

        // check with standard error message (use overridingErrorMessage before contains to set your own message).
        Iterables.instance().assertDoesNotContain(info, actual.getDataSources(), dataSources);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider has no dataSources.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceProvider's dataSources is
     * not empty.
     */
    @NonNull
    public IDataSourceProviderAssert hasNoDataSources() {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // we override the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting :\n  <%s>\nnot to have dataSources but had :\n  <%s>";

        // check
        if (actual.getDataSources().iterator().hasNext()) {
            failWithMessage(assertjErrorMessage, actual, actual.getDataSources());
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider's displayName is equal to
     * the given one.
     *
     * @param displayName the given displayName to compare the actual
     * IDataSourceProvider's displayName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceProvider's displayName
     * is not equal to the given one.
     */
    @NonNull
    public IDataSourceProviderAssert hasDisplayName(String displayName) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting displayName of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualDisplayName = actual.getDisplayName();
        if (!Objects.areEqual(actualDisplayName, displayName)) {
            failWithMessage(assertjErrorMessage, actual, displayName, actualDisplayName);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider's source is equal to the
     * given one.
     *
     * @param source the given source to compare the actual
     * IDataSourceProvider's source to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceProvider's source is
     * not equal to the given one.
     */
    @NonNull
    public IDataSourceProviderAssert hasSource(String source) {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting source of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualSource = actual.getSource();
        if (!Objects.areEqual(actualSource, source)) {
            failWithMessage(assertjErrorMessage, actual, source, actualSource);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider is available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceProvider is not
     * available.
     */
    @NonNull
    public IDataSourceProviderAssert isAvailable() {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // check
        if (!actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IDataSourceProvider is available but is not.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider is not available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceProvider is available.
     */
    @NonNull
    public IDataSourceProviderAssert isNotAvailable() {
        // check that actual IDataSourceProvider we want to make assertions on is not null.
        isNotNull();

        // check
        if (actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IDataSourceProvider is not available but is.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceProvider is compliant with the API.
     *
     * @param <P>
     * @param factory
     * @param sampler
     * @throws AssertionError - if the actual IDataSourceProvider is not
     * compliant.
     */
    public static <P extends IDataSourceProvider> void assertCompliance(@NonNull Supplier<P> factory, @NonNull Sampler sampler) {
        ITsProviderAssert.assertCompliance(factory, sampler);
        SoftAssertions s = new SoftAssertions();
        checkGetDisplayName(s, factory, sampler);
        checkGetDataSources(s, factory, sampler);
        checkChildrenOfDataSource(s, factory, sampler);
        checkChildrenOfDataSet(s, factory, sampler);
        checkGetDisplayNameOfDataSource(s, factory, sampler);
        checkGetDisplayNameOfDataSet(s, factory, sampler);
        checkGetDisplayNodeName(s, factory, sampler);
        checkGetDisplayNameOfException(s, factory, sampler);
        checkAddDataSourceListener(s, factory, sampler);
        checkRemoveDataSourceListener(s, factory, sampler);
        checkToMonikerOfDataSOurce(s, factory, sampler);
        checkToMonikerOfDataSet(s, factory, sampler);
        checkToDataSetOfMoniker(s, factory, sampler);
        checkToDataSourceOfMoniker(s, factory, sampler);
        s.assertAll();
    }

    public interface Sampler<P extends IDataSourceProvider> extends ITsProviderAssert.Sampler<P> {

        @Override
        public default Optional<TsMoniker> tsMoniker(P p) {
            return tsDataSet(p).map(p::toMoniker);
        }

        @Override
        public default Optional<TsMoniker> tsCollectionMoniker(P p) {
            return tsCollectionDataSet(p).map(p::toMoniker);
        }

        @NonNull
        Optional<DataSource> dataSource(@NonNull P p);

        @NonNull
        Optional<DataSet> tsDataSet(@NonNull P p);

        @NonNull
        Optional<DataSet> tsCollectionDataSet(@NonNull P p);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static <P extends IDataSourceProvider> void checkGetDisplayName(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getDisplayName()).isNotEmpty();
        }
    }

    private static <P extends IDataSourceProvider> void checkGetDataSources(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getDataSources()).isNotNull();
        }
    }

    private static <P extends IDataSourceProvider> void checkChildrenOfDataSource(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.children(NULL_DATA_SOURCE))
                    .as(throwDescription(p, "children(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.children(BAD_DATA_SOURCE))
                    .as(throwDescription(p, "children(invalidDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkChildrenOfDataSet(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.children(NULL_DATA_SET))
                    .as(throwDescription(p, "children(nullDataSet)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.children(BAD_DATA_SET))
                    .as(throwDescription(p, "children(invalidDataSet)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkGetDisplayNameOfDataSource(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getDisplayName(NULL_DATA_SOURCE))
                    .as(throwDescription(p, "getDisplayName(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.getDisplayName(BAD_DATA_SOURCE))
                    .as(throwDescription(p, "getDisplayName(invalidDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkGetDisplayNameOfDataSet(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getDisplayName(NULL_DATA_SET))
                    .as(throwDescription(p, "getDisplayName(nullDataSet)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.getDisplayName(BAD_DATA_SET))
                    .as(throwDescription(p, "getDisplayName(invalidDataSet)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkGetDisplayNodeName(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getDisplayNodeName(NULL_DATA_SET))
                    .as(throwDescription(p, "getDisplayNodeName(nullDataSet)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.getDisplayNodeName(BAD_DATA_SET))
                    .as(throwDescription(p, "getDisplayNodeName(invalidDataSet)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkGetDisplayNameOfException(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.getDisplayName(NULL_IO_EXCEPTION))
                    .as(throwDescription(p, "getDisplayName(nullIOException)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkAddDataSourceListener(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.addDataSourceListener(null))
                    .as(throwDescription(p, "addDataSourceListener(nullListener)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkRemoveDataSourceListener(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.removeDataSourceListener(null))
                    .as(throwDescription(p, "removeDataSourceListener(nullListener)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkToMonikerOfDataSOurce(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.toMoniker(NULL_DATA_SOURCE))
                    .as(throwDescription(p, "toMoniker(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.toMoniker(BAD_DATA_SOURCE))
                    .as(throwDescription(p, "toMoniker(invalidDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkToMonikerOfDataSet(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.toMoniker(NULL_DATA_SET))
                    .as(throwDescription(p, "toMoniker(nullDataSet)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.toMoniker(BAD_DATA_SET))
                    .as(throwDescription(p, "toMoniker(invalidDataSet)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkToDataSetOfMoniker(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.toDataSet(NULL_MONIKER))
                    .as(throwDescription(p, "toDataSet(nullMoniker)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.toDataSet(BAD_MONIKER))
                    .as(throwDescription(p, "toDataSet(invalidMoniker)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private static <P extends IDataSourceProvider> void checkToDataSourceOfMoniker(SoftAssertions s, Supplier<P> factory, Sampler sampler) {
        try (P p = factory.get()) {
            s.assertThatThrownBy(() -> p.toDataSource(NULL_MONIKER))
                    .as(throwDescription(p, "toDataSource(nullMoniker)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.toDataSource(BAD_MONIKER))
                    .as(throwDescription(p, "toDataSource(invalidMoniker)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
    //</editor-fold>
}
