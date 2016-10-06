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
package ec.tss.tsproviders.utils;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataSourceBean;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Supporting class for {@link HasDataSourceBean}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 * @param <T> the type of the bean
 */
@ThreadSafe
public final class DataSourceBeanSupport<T> implements HasDataSourceBean<T> {

    /**
     * Creates a new instance of HasDataSourceBean using a versioned param.
     *
     * @param <T> the type of the bean
     * @param providerName a non-null provider name
     * @param param a non-null versioned param
     * @return a non-null instance
     */
    @Nonnull
    public static <T> HasDataSourceBean<T> of(@Nonnull String providerName, @Nonnull VersionedParam<DataSource, T> param) {
        return new DataSourceBeanSupport(providerName, param);
    }

    private final String providerName;
    private final VersionedParam<DataSource, T> param;

    private DataSourceBeanSupport(String providerName, VersionedParam<DataSource, T> param) {
        this.providerName = Objects.requireNonNull(providerName);
        this.param = Objects.requireNonNull(param);
    }

    //<editor-fold defaultstate="collapsed" desc="HasDataSourceBean">
    @Override
    public T newBean() {
        return param.defaultValue();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        Objects.requireNonNull(bean);
        try {
            IConfig.Builder<?, DataSource> builder = DataSource.builder(providerName, param.getVersion());
            param.set(builder, (T) bean);
            return builder.build();
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public T decodeBean(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        return param.get(dataSource);
    }
    //</editor-fold>
}
