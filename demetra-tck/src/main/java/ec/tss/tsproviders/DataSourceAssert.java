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

import javax.annotation.Nonnull;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;

/**
 * {@link DataSource} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class DataSourceAssert extends AbstractAssert<DataSourceAssert, DataSource> {

    /**
     * Creates a new <code>{@link DataSourceAssert}</code> to make assertions on
     * actual DataSource.
     *
     * @param actual the DataSource we want to make assertions on.
     */
    public DataSourceAssert(@Nonnull DataSource actual) {
        super(actual, DataSourceAssert.class);
    }

    /**
     * An entry point for DataSourceAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myDataSource)</code> and get specific assertion with
     * code completion.
     *
     * @param actual the DataSource we want to make assertions on.
     * @return a new <code>{@link DataSourceAssert}</code>
     */
    @Nonnull
    public static DataSourceAssert assertThat(@Nonnull DataSource actual) {
        return new DataSourceAssert(actual);
    }

    /**
     * Verifies that the actual DataSource's params is equal to the given one.
     *
     * @param params the given params to compare the actual DataSource's params
     * to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSource's params is not equal
     * to the given one.
     */
    @Nonnull
    public DataSourceAssert hasParams(java.util.SortedMap params) {
        // check that actual DataSource we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting params of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.SortedMap actualParams = actual.getParams();
        if (!Objects.areEqual(actualParams, params)) {
            failWithMessage(assertjErrorMessage, actual, params, actualParams);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual DataSource's providerName is equal to the given
     * one.
     *
     * @param providerName the given providerName to compare the actual
     * DataSource's providerName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSource's providerName is not
     * equal to the given one.
     */
    @Nonnull
    public DataSourceAssert hasProviderName(String providerName) {
        // check that actual DataSource we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting providerName of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualProviderName = actual.getProviderName();
        if (!Objects.areEqual(actualProviderName, providerName)) {
            failWithMessage(assertjErrorMessage, actual, providerName, actualProviderName);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual DataSource's version is equal to the given one.
     *
     * @param version the given version to compare the actual DataSource's
     * version to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSource's version is not equal
     * to the given one.
     */
    @Nonnull
    public DataSourceAssert hasVersion(String version) {
        // check that actual DataSource we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting version of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualVersion = actual.getVersion();
        if (!Objects.areEqual(actualVersion, version)) {
            failWithMessage(assertjErrorMessage, actual, version, actualVersion);
        }

        // return the current assertion for method chaining
        return this;
    }
}
