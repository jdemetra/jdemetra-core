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

import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceListener;
import ec.tss.tsproviders.IDataSourceListener;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public class ToDataSourceListener implements DataSourceListener {

    private final IDataSourceListener delegate;

    @Override
    public void opened(@NonNull DataSource dataSource) {
        delegate.opened(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public void closed(@NonNull DataSource dataSource) {
        delegate.closed(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public void changed(@NonNull DataSource dataSource) {
        delegate.changed(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public void allClosed(@NonNull String providerName) {
        delegate.allClosed(providerName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ToDataSourceListener && this.delegate.equals(((ToDataSourceListener) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
