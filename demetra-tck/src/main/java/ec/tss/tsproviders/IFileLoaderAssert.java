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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.internal.Iterables;
import org.assertj.core.util.Objects;

/**
 * {@link IFileLoader} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class IFileLoaderAssert extends AbstractAssert<IFileLoaderAssert, IFileLoader> {

    /**
     * Creates a new <code>{@link IFileLoaderAssert}</code> to make assertions
     * on actual IFileLoader.
     *
     * @param actual the IFileLoader we want to make assertions on.
     */
    public IFileLoaderAssert(@Nonnull IFileLoader actual) {
        super(actual, IFileLoaderAssert.class);
    }

    /**
     * An entry point for IFileLoaderAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myIFileLoader)</code> and get specific assertion with
     * code completion.
     *
     * @param actual the IFileLoader we want to make assertions on.
     * @return a new <code>{@link IFileLoaderAssert}</code>
     */
    @Nonnull
    public static IFileLoaderAssert assertThat(@Nonnull IFileLoader actual) {
        return new IFileLoaderAssert(actual);
    }

    /**
     * Verifies that the actual IFileLoader's asyncMode is equal to the given
     * one.
     *
     * @param asyncMode the given asyncMode to compare the actual IFileLoader's
     * asyncMode to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader's asyncMode is not
     * equal to the given one.
     */
    @Nonnull
    public IFileLoaderAssert hasAsyncMode(ec.tss.TsAsyncMode asyncMode) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader's dataSources contains the given
     * DataSource elements.
     *
     * @param dataSources the given elements that should be contained in actual
     * IFileLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's dataSources does not
     * contain all given DataSource elements.
     */
    @Nonnull
    public IFileLoaderAssert hasDataSources(DataSource... dataSources) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader's dataSources contains <b>only</b>
     * the given DataSource elements and nothing else in whatever order.
     *
     * @param dataSources the given elements that should be contained in actual
     * IFileLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's dataSources does not
     * contain all given DataSource elements.
     */
    @Nonnull
    public IFileLoaderAssert hasOnlyDataSources(DataSource... dataSources) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader's dataSources does not contain the
     * given DataSource elements.
     *
     * @param dataSources the given elements that should not be in actual
     * IFileLoader's dataSources.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's dataSources contains
     * any given DataSource elements.
     */
    @Nonnull
    public IFileLoaderAssert doesNotHaveDataSources(DataSource... dataSources) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader has no dataSources.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's dataSources is not
     * empty.
     */
    @Nonnull
    public IFileLoaderAssert hasNoDataSources() {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader's displayName is equal to the given
     * one.
     *
     * @param displayName the given displayName to compare the actual
     * IFileLoader's displayName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader's displayName is not
     * equal to the given one.
     */
    @Nonnull
    public IFileLoaderAssert hasDisplayName(String displayName) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader's fileDescription is equal to the
     * given one.
     *
     * @param fileDescription the given fileDescription to compare the actual
     * IFileLoader's fileDescription to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader's fileDescription is
     * not equal to the given one.
     */
    @Nonnull
    public IFileLoaderAssert hasFileDescription(String fileDescription) {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting fileDescription of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualFileDescription = actual.getFileDescription();
        if (!Objects.areEqual(actualFileDescription, fileDescription)) {
            failWithMessage(assertjErrorMessage, actual, fileDescription, actualFileDescription);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader's paths contains the given
     * java.io.File elements.
     *
     * @param paths the given elements that should be contained in actual
     * IFileLoader's paths.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's paths does not contain
     * all given java.io.File elements.
     */
    @Nonnull
    public IFileLoaderAssert hasPaths(java.io.File... paths) {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // check that given java.io.File varargs is not null.
        if (paths == null) {
            failWithMessage("Expecting paths parameter not to be null.");
        }

        // check with standard error message (use overridingErrorMessage before contains to set your own message).
        Assertions.assertThat(actual.getPaths()).contains(paths);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader's paths contains <b>only</b> the
     * given java.io.File elements and nothing else in whatever order.
     *
     * @param paths the given elements that should be contained in actual
     * IFileLoader's paths.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's paths does not contain
     * all given java.io.File elements and nothing else.
     */
    @Nonnull
    public IFileLoaderAssert hasOnlyPaths(java.io.File... paths) {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // check that given java.io.File varargs is not null.
        if (paths == null) {
            failWithMessage("Expecting paths parameter not to be null.");
        }

        // check with standard error message (use overridingErrorMessage before contains to set your own message).
        Assertions.assertThat(actual.getPaths()).containsOnly(paths);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader's paths does not contain the given
     * java.io.File elements.
     *
     * @param paths the given elements that should not be in actual
     * IFileLoader's paths.
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's paths contains any
     * given java.io.File elements.
     */
    @Nonnull
    public IFileLoaderAssert doesNotHavePaths(java.io.File... paths) {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // check that given java.io.File varargs is not null.
        if (paths == null) {
            failWithMessage("Expecting paths parameter not to be null.");
        }

        // check with standard error message (use overridingErrorMessage before contains to set your own message).
        Assertions.assertThat(actual.getPaths()).doesNotContain(paths);

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader has no paths.
     *
     * @return this assertion object.
     * @throws AssertionError if the actual IFileLoader's paths is not empty.
     */
    @Nonnull
    public IFileLoaderAssert hasNoPaths() {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // we override the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting :\n  <%s>\nnot to have paths but had :\n  <%s>";

        // check
        if (actual.getPaths().length > 0) {
            failWithMessage(assertjErrorMessage, actual, java.util.Arrays.toString(actual.getPaths()));
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader's source is equal to the given one.
     *
     * @param source the given source to compare the actual IFileLoader's source
     * to.
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader's source is not equal
     * to the given one.
     */
    @Nonnull
    public IFileLoaderAssert hasSource(String source) {
        // check that actual IFileLoader we want to make assertions on is not null.
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
     * Verifies that the actual IFileLoader is available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader is not available.
     */
    @Nonnull
    public IFileLoaderAssert isAvailable() {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // check
        if (!actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IFileLoader is available but is not.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader is not available.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual IFileLoader is available.
     */
    @Nonnull
    public IFileLoaderAssert isNotAvailable() {
        // check that actual IFileLoader we want to make assertions on is not null.
        isNotNull();

        // check
        if (actual.isAvailable()) {
            failWithMessage("\nExpecting that actual IFileLoader is not available but is.");
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual IFileLoader is equivalent to another
     * IFileLoader.
     *
     * @param <X>
     * @param expected
     * @param toDataSource
     * @return this assertion object.
     * @throws IOException
     * @throws AssertionError - if the actual IFileLoader is not equivalent.
     */
    @Nonnull
    public <X extends IFileLoader> IFileLoaderAssert isEquivalentTo(
            @Nonnull X expected,
            @Nonnull Function<? super X, DataSource> toDataSource) throws IOException {
        isNotNull();
        SoftAssertions s = new SoftAssertions();
        Utils.assertFileLoaderEquivalence(s, actual, expected, toDataSource.apply(expected));
        s.assertAll();
        return this;
    }

    /**
     * Verifies that the actual IFileLoader is compliant with the API.
     *
     * @param <P>
     * @param factory
     * @param sampler
     * @throws AssertionError - if the actual IFileLoader is not compliant.
     */
    public static <P extends IFileLoader> void assertCompliance(@Nonnull Supplier<P> factory, @Nonnull Sampler<P> sampler) {
        IDataSourceLoaderAssert.assertCompliance(factory, sampler);
        SoftAssertions soft = new SoftAssertions();
        checkNewBean(soft, factory, sampler);
        checkDecodeBean(soft, factory, sampler);
        checkAccept(soft, factory, sampler);
        checkGetFileDescription(soft, factory, sampler);
        checkPath(soft, factory, sampler);
        soft.assertAll();
    }

    public interface Sampler<P extends IFileLoader> extends IDataSourceLoaderAssert.Sampler<P> {

        @Nonnull
        default File validFile(P p) {
            return bean(p).getFile();
        }

        @Override
        public IFileBean bean(P p);
    }

    public static File urlAsFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static <P extends IFileLoader> void checkNewBean(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.newBean())
                    .as("#newBean() cannot return null")
                    .isNotNull()
                    .as("Subsequent calls to #newBean() must return equivalent beans")
                    .isEqualToComparingFieldByField(p.newBean());
        }
    }

    private static <P extends IFileLoader> void checkDecodeBean(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            sampler.dataSource(p).ifPresent(o -> {
                s.assertThat(p.decodeBean(o).getFile())
                        .as("#decodeBean(DataSource) must return a IFileBean that contains a non-null file")
                        .isNotNull();
            });
        }
    }

    private static <P extends IFileLoader> void checkAccept(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.accept(sampler.validFile(p)))
                    .as("Valid file must be accepted by provider")
                    .isTrue();
        }
    }

    private static <P extends IFileLoader> void checkGetFileDescription(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            s.assertThat(p.getFileDescription())
                    .as("File description cannot be empty")
                    .isNotEmpty();
        }
    }

    private static <P extends IFileLoader> void checkPath(SoftAssertions s, Supplier<P> factory, Sampler<P> sampler) {
        try (P p = factory.get()) {
            File[] files = new File[]{new File("hello"), new File("world")};
            p.setPaths(files);
            s.assertThat(p.getPaths()).containsExactly(files);
            p.setPaths(null);
            s.assertThat(p.getPaths()).isEmpty();
        }
    }
    //</editor-fold>
}
