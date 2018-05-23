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

import demetra.tsprovider.DataSourceListener;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceListener;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class FromDataSourceListener implements IDataSourceListener {

    private final DataSourceListener delegate;

    @Override
    public void opened(DataSource dataSource) {
        delegate.opened(TsConverter.toDataSource(dataSource));
    }

    @Override
    public void closed(DataSource dataSource) {
        delegate.closed(TsConverter.toDataSource(dataSource));
    }

    @Override
    public void changed(DataSource dataSource) {
        delegate.changed(TsConverter.toDataSource(dataSource));
    }

    @Override
    public void allClosed(String providerName) {
        delegate.allClosed(providerName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FromDataSourceListener && this.delegate.equals(((FromDataSourceListener) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
