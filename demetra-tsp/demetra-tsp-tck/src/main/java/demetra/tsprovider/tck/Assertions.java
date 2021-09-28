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

import demetra.tsprovider.*;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Entry point for assertions of different data types. Each method in this class
 * is a static factory for the type-specific assertion objects.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public class Assertions {

    /**
     * Creates a new instance of <code>{@link DataSetAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static DataSetAssert assertThat(@NonNull DataSet actual) {
        return new DataSetAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DataSourceAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static DataSourceAssert assertThat(@NonNull DataSource actual) {
        return new DataSourceAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DataSourceLoaderAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static DataSourceLoaderAssert assertThat(@NonNull DataSourceLoader actual) {
        return new DataSourceLoaderAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DataSourceProviderAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static DataSourceProviderAssert assertThat(@NonNull DataSourceProvider actual) {
        return new DataSourceProviderAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link FileBeanAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static FileBeanAssert assertThat(@NonNull FileBean actual) {
        return new FileBeanAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link FileLoaderAssert}</code>.
     *
     * @param actual the actual value.
     * @return the created assertion object.
     */
    @NonNull
    public static FileLoaderAssert assertThat(@NonNull FileLoader actual) {
        return new FileLoaderAssert(actual);
    }

    /**
     * Creates a new <code>{@link Assertions}</code>.
     */
    protected Assertions() {
        // empty
    }
}
