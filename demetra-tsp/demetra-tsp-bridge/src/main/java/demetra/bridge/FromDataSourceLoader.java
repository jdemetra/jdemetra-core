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
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceLoader;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public class FromDataSourceLoader<T extends DataSourceLoader> extends FromDataSourceProvider<T> implements IDataSourceLoader {

    public FromDataSourceLoader(T delegate) {
        super(delegate);
    }

    @Override
    public boolean open(DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().open(TsConverter.toDataSource(dataSource));
    }

    @Override
    public boolean close(DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().close(TsConverter.toDataSource(dataSource));
    }

    @Override
    public Object newBean() {
        return getDelegate().newBean();
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        return TsConverter.fromDataSource(getDelegate().encodeBean(bean));
    }

    @Override
    public Object decodeBean(DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().decodeBean(TsConverter.toDataSource(dataSource));
    }
}
