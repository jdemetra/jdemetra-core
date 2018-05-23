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

import demetra.tsprovider.DataSourceProvider;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceListener;
import ec.tss.tsproviders.IDataSourceProvider;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public class FromDataSourceProvider<T extends DataSourceProvider> extends FromTsProvider<T> implements IDataSourceProvider {

    public FromDataSourceProvider(T delegate) {
        super(delegate);
    }

    @Override
    public void reload(DataSource dataSource) throws IllegalArgumentException {
        getDelegate().reload(TsConverter.toDataSource(dataSource));
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public List<DataSource> getDataSources() {
        return getDelegate().getDataSources().stream().map(TsConverter::fromDataSource).collect(Collectors.toList());
    }

    @Override
    public void addDataSourceListener(IDataSourceListener listener) {
        getDelegate().addDataSourceListener(new ToDataSourceListener(listener));
    }

    @Override
    public void removeDataSourceListener(IDataSourceListener listener) {
        getDelegate().removeDataSourceListener(new ToDataSourceListener(listener));
    }

    @Override
    public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
        return getDelegate().children(TsConverter.toDataSource(dataSource)).stream().map(TsConverter::fromDataSet).collect(Collectors.toList());
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        return getDelegate().children(TsConverter.toDataSet(parent)).stream().map(TsConverter::fromDataSet).collect(Collectors.toList());
    }

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().getDisplayName(TsConverter.toDataSource(dataSource));
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        return getDelegate().getDisplayName(TsConverter.toDataSet(dataSet));
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        return getDelegate().getDisplayNodeName(TsConverter.toDataSet(dataSet));
    }

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        return TsConverter.fromTsMoniker(getDelegate().toMoniker(TsConverter.toDataSource(dataSource)));
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        return TsConverter.fromTsMoniker(getDelegate().toMoniker(TsConverter.toDataSet(dataSet)));
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
        return TsConverter.fromDataSource(getDelegate().toDataSource(TsConverter.toTsMoniker(moniker)));
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
        return TsConverter.fromDataSet(getDelegate().toDataSet(TsConverter.toTsMoniker(moniker)));
    }
}
