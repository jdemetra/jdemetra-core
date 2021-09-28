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
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * {@link DataSet} specific assertions.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class DataSetAssert extends AbstractAssert<DataSetAssert, DataSet> {

    /**
     * Creates a new <code>{@link DataSetAssert}</code> to make assertions on
     * actual DataSet.
     *
     * @param actual the DataSet we want to make assertions on.
     */
    public DataSetAssert(@NonNull DataSet actual) {
        super(actual, DataSetAssert.class);
    }

    /**
     * An entry point for DataSetAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myDataSet)</code> and get specific assertion with code
     * completion.
     *
     * @param actual the DataSet we want to make assertions on.
     * @return a new <code>{@link DataSetAssert}</code>
     */
    @NonNull
    public static DataSetAssert assertThat(@NonNull DataSet actual) {
        return new DataSetAssert(actual);
    }

    /**
     * Verifies that the actual DataSet's dataSource is equal to the given one.
     *
     * @param dataSource the given dataSource to compare the actual DataSet's
     *                   dataSource to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSet's dataSource is not equal
     *                        to the given one.
     */
    @NonNull
    public DataSetAssert hasDataSource(DataSource dataSource) {
        // check that actual DataSet we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting dataSource of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        DataSource actualDataSource = actual.getDataSource();
        if (!Objects.areEqual(actualDataSource, dataSource)) {
            failWithMessage(assertjErrorMessage, actual, dataSource, actualDataSource);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual DataSet's kind is equal to the given one.
     *
     * @param kind the given kind to compare the actual DataSet's kind to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSet's kind is not equal to the
     *                        given one.
     */
    @NonNull
    public DataSetAssert hasKind(DataSet.Kind kind) {
        // check that actual DataSet we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting kind of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        DataSet.Kind actualKind = actual.getKind();
        if (!Objects.areEqual(actualKind, kind)) {
            failWithMessage(assertjErrorMessage, actual, kind, actualKind);
        }

        // return the current assertion for method chaining
        return this;
    }

    /**
     * Verifies that the actual DataSet's params is equal to the given one.
     *
     * @param params the given params to compare the actual DataSet's params to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DataSet's params is not equal to
     *                        the given one.
     */
    @NonNull
    public DataSetAssert hasParams(java.util.SortedMap params) {
        // check that actual DataSet we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting params of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.SortedMap actualParams = actual.getParameters();
        if (!Objects.areEqual(actualParams, params)) {
            failWithMessage(assertjErrorMessage, actual, params, actualParams);
        }

        // return the current assertion for method chaining
        return this;
    }
}
