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
package demetra.tsprovider.tck;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.util.TsProviders;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.internal.Iterables;
import org.assertj.core.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link DataSourceLoader} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class DataSourceLoaderAssert extends AbstractAssert<DataSourceLoaderAssert, DataSourceLoader> {

    /**
     * Creates a new <code>{@link DataSourceLoaderAssert}</code> to make
     * assertions on actual IDataSourceLoader.
     *
     * @param actual the IDataSourceLoader we want to make assertions on.
     */
    public DataSourceLoaderAssert(@NonNull DataSourceLoader actual) {
        super(actual, DataSourceLoaderAssert.class);
    }

    /**
     * An entry point for IDataSourceLoaderAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myIDataSourceLoader)</code> and get specific assertion
     * with code completion.
     *
     * @param actual the IDataSourceLoader we want to make assertions on.
     * @return a new <code>{@link DataSourceLoaderAssert}</code>
     */
    @NonNull
    public static DataSourceLoaderAssert assertThat(@NonNull DataSourceLoader actual) {
        return new DataSourceLoaderAssert(actual);
    }

    /**
     * Verifies that the actual IDataSourceLoader's dataSources contains the
     * given DataSource elements.
     *
     * @param dataSources the given elements that should be contained in actual
     *                    IDataSourceLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceLoader's dataSources does
     *                        not contain all given DataSource elements.
     */
    @NonNull
    public DataSourceLoaderAssert hasDataSources(DataSource... dataSources) {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader's dataSources contains
     * <b>only</b> the given DataSource elements and nothing else in whatever
     * order.
     *
     * @param dataSources the given elements that should be contained in actual
     *                    IDataSourceLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceLoader's dataSources does
     *                        not contain all given DataSource elements.
     */
    @NonNull
    public DataSourceLoaderAssert hasOnlyDataSources(DataSource... dataSources) {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader's dataSources does not contain
     * the given DataSource elements.
     *
     * @param dataSources the given elements that should not be in actual
     *                    IDataSourceLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceLoader's dataSources
     *                        contains any given DataSource elements.
     */
    @NonNull
    public DataSourceLoaderAssert doesNotHaveDataSources(DataSource... dataSources) {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader has no dataSources.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual IDataSourceLoader's dataSources is
     *                        not empty.
     */
    @NonNull
    public DataSourceLoaderAssert hasNoDataSources() {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader's displayName is equal to the
     * given one.
     *
     * @param displayName the given displayName to compare the actual
     *                    IDataSourceLoader's displayName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceLoader's displayName is
     *                        not equal to the given one.
     */
    @NonNull
    public DataSourceLoaderAssert hasDisplayName(String displayName) {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader's source is equal to the given
     * one.
     *
     * @param source the given source to compare the actual IDataSourceLoader's
     *               source to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceLoader's source is not
     *                        equal to the given one.
     */
    @NonNull
    public DataSourceLoaderAssert hasSource(String source) {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
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
     * Verifies that the actual IDataSourceLoader is available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceLoader is not
     *                        available.
     */
    @NonNull
    public DataSourceLoaderAssert isAvailable() {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
        isNotNull();

        // check
        if (!actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IDataSourceLoader is available but is not.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceLoader is not available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IDataSourceLoader is available.
     */
    @NonNull
    public DataSourceLoaderAssert isNotAvailable() {
        // check that actual IDataSourceLoader we want to make assertions on is not null.
        isNotNull();

        // check
        if (actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IDataSourceLoader is not available but is.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IDataSourceLoader is equivalent to another
     * IDataSourceLoader.
     *
     * @param <X>
     * @param expected
     * @param toDataSource
     * @return this assertion object.
     * @throws IOException
     * @throws AssertionError - if the actual IDataSourceLoader is not
     *                        equivalent.
     */
    @NonNull
    public <X extends DataSourceLoader> DataSourceLoaderAssert isEquivalentTo(
            @NonNull X expected,
            @NonNull Function<? super X, DataSource> toDataSource) throws IOException {
        isNotNull();
        SoftAssertions s = new SoftAssertions();
        Utils.assertDataSourceLoaderEquivalence(s, actual, expected, toDataSource.apply(expected));
        s.assertAll();
        return this;
    }

    /**
     * Verifies that the actual IDataSourceLoader is compliant with the API.
     *
     * @param <T>
     * @param factory
     * @param sampler
     * @throws AssertionError - if the actual IDataSourceLoader is not
     *                        compliant.
     */
    public static <T extends DataSourceLoader> void assertCompliance(@NonNull Supplier<T> factory, @NonNull Sampler<T> sampler) {
        DataSourceProviderAssert.assertCompliance(factory, sampler);
        SoftAssertions s = new SoftAssertions();
        checkOpen(s, factory, sampler);
        checkClose(s, factory, sampler);
        checkCloseAll(s, factory, sampler);
        checkNewBean(s, factory, sampler);
        checkDecodeBean(s, factory, sampler);
        checkEncodeBean(s, factory, sampler);
        s.assertAll();
    }

    public interface Sampler<P extends DataSourceLoader> extends DataSourceProviderAssert.Sampler<P> {

        @Override
        default Optional<DataSource> dataSource(P p) {
            return Optional.of(p.encodeBean(bean(p)));
        }

        @Override
        default Optional<DataSet> tsDataSet(P p) {
            return dataSource(p).map(target -> firstSeriesOrNull(p, target));
        }

        @Override
        default Optional<DataSet> tsCollectionDataSet(P p) {
            return dataSource(p).map(target -> firstCollectionOrNull(p, target));
        }

        @NonNull
        Object bean(@NonNull P p);
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static <T extends DataSourceLoader> void checkOpen(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
        try (T p = factory.get()) {
            s.assertThatThrownBy(() -> p.open(Utils.NULL_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "open(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.open(Utils.BAD_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "open(badDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
            sampler.dataSource(p).ifPresent(o -> {
                p.closeAll();
                s.assertThat(p.open(o)).isTrue();
                s.assertThat(p.open(o)).isFalse();
                s.assertThat(p.getDataSources()).contains(o);
            });
        }
    }

    private static <T extends DataSourceLoader> void checkClose(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
        try (T p = factory.get()) {
            s.assertThatThrownBy(() -> p.close(Utils.NULL_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "close(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.close(Utils.BAD_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "close(badDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
            sampler.dataSource(p).ifPresent(o -> {
                p.open(o);
                p.close(o);
                s.assertThat(p.getDataSources()).doesNotContain(o);
            });
        }
    }

    private static <T extends DataSourceLoader> void checkCloseAll(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
    }

    private static <T extends DataSourceLoader> void checkNewBean(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
        try (T p = factory.get()) {
            s.assertThat(p.newBean())
                    .isNotNull()
                    .usingRecursiveComparison()
                    .isEqualTo(p.newBean());
            s.assertThat(p.encodeBean(p.newBean())).isNotNull();
        }
    }

    private static <T extends DataSourceLoader> void checkDecodeBean(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
        try (T p = factory.get()) {
            s.assertThatThrownBy(() -> p.decodeBean(Utils.NULL_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "decodeBean(nullDataSource)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.decodeBean(Utils.BAD_DATA_SOURCE))
                    .as(Utils.throwDescription(p, "decodeBean(badDataSource)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
            sampler.dataSource(p).ifPresent(o -> {
                s.assertThat(p.decodeBean(o))
                        .as("#decodeBean(DataSource) must return a non-null bean")
                        .isNotNull();
            });
        }
    }

    private static <T extends DataSourceLoader> void checkEncodeBean(SoftAssertions s, Supplier<T> factory, Sampler<T> sampler) {
        try (T p = factory.get()) {
            s.assertThatThrownBy(() -> p.encodeBean(Utils.NULL_BEAN))
                    .as(Utils.throwDescription(p, "encodeBean(nullBean)", NullPointerException.class))
                    .isInstanceOf(NullPointerException.class);
            s.assertThatThrownBy(() -> p.encodeBean(Utils.BAD_BEAN))
                    .as(Utils.throwDescription(p, "encodeBean(badBean)", IllegalArgumentException.class))
                    .isInstanceOf(IllegalArgumentException.class);
            s.assertThat(p.encodeBean(sampler.bean(p)))
                    .as("#encodeBean(Object) must return a non-null DataSource")
                    .isNotNull();
        }
    }

    private static DataSet firstSeriesOrNull(DataSourceProvider p, DataSource target) {
        return TsProviders
                .getTreeTraverser(p, target)
                .breadthFirstStream()
                .filter(DataSet.class::isInstance)
                .map(DataSet.class::cast)
                .findFirst()
                .orElse(null);
    }

    private static DataSet firstCollectionOrNull(DataSourceProvider p, DataSource target) {
        return TsProviders
                .getTreeTraverser(p, target)
                .breadthFirstStream()
                .filter(DataSet.class::isInstance)
                .map(DataSet.class::cast)
                .findFirst()
                .orElse(null);
    }
    //</editor-fold>
}
