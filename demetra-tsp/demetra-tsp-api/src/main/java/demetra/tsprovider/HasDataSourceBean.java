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
package demetra.tsprovider;

import internal.tsprovider.InternalTsProvider;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the ability to convert a DataSource from/to a bean. Note that the
 * implementations must be thread-safe.
 *
 * @param <T> the type of the bean
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataSourceBean<T> {

    /**
     * Creates a provider-specific bean that can be used to configure a
     * DataSource.
     *
     * @return a non-null bean.
     */
    @NonNull
    T newBean();

    /**
     * Creates a DataSource whose configuration is determined by the specified
     * bean.
     *
     * @param bean
     * @return a non-null DataSource.
     * @throws IllegalArgumentException if the bean doesn't belong to this
     *                                  provider.
     */
    @NonNull
    DataSource encodeBean(@NonNull Object bean) throws IllegalArgumentException;

    /**
     * Creates a bean by reading the parameters of the specified DataSource.
     *
     * @param dataSource
     * @return a non-null bean.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     */
    @NonNull
    T decodeBean(@NonNull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Creates a new instance of HasDataSourceBean using a versioned param.
     *
     * @param <T>          the type of the bean
     * @param providerName a non-null provider name
     * @param param        a non-null param
     * @param version      a non-null version
     * @return a non-null instance
     */
    @NonNull
    static <T> HasDataSourceBean<T> of(@NonNull String providerName, DataSource.@NonNull Converter<T> param, @NonNull String version) {
        return new InternalTsProvider.DataSourceBeanSupport<>(providerName, param, version);
    }
}
