/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.bridge;

import demetra.tsprovider.DataSourceLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * @author Philippe Charles
 */
public class FromDataSourceLoader extends FromDataSourceProvider implements ec.tss.tsproviders.IDataSourceLoader {

    public FromDataSourceLoader(@NonNull DataSourceLoader<?> delegate) {
        super(delegate);
    }

    @Override
    public @NonNull DataSourceLoader<?> getDelegate() {
        return (DataSourceLoader<?>) super.getDelegate();
    }

    @Override
    public boolean open(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return getDelegate().open(TsConverter.toDataSource(dataSource));
    }

    @Override
    public boolean close(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return getDelegate().close(TsConverter.toDataSource(dataSource));
    }

    @Override
    public @NonNull Object newBean() {
        return getDelegate().newBean();
    }

    @Override
    public ec.tss.tsproviders.@NonNull DataSource encodeBean(@NonNull Object bean) throws IllegalArgumentException {
        Objects.requireNonNull(bean);
        return TsConverter.fromDataSource(getDelegate().encodeBean(bean));
    }

    @Override
    public @NonNull Object decodeBean(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return getDelegate().decodeBean(TsConverter.toDataSource(dataSource));
    }
}
