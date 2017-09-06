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

import demetra.tsprovider.FileLoader;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.IFileLoader;
import java.io.File;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public class FromFileLoader<T extends FileLoader> extends FromDataSourceLoader<T> implements IFileLoader {

    public FromFileLoader(T delegate) {
        super(delegate);
    }

    @Override
    public String getFileDescription() {
        return getDelegate().getFileDescription();
    }

    @Override
    public boolean accept(File pathname) {
        return getDelegate().accept(pathname);
    }

    @Override
    public void setPaths(File[] paths) {
        getDelegate().setPaths(paths);
    }

    @Override
    public File[] getPaths() {
        return getDelegate().getPaths();
    }

    @Override
    public IFileBean newBean() {
        return new FromFileBean(getDelegate().newBean());
    }

    @Override
    public IFileBean decodeBean(ec.tss.tsproviders.DataSource dataSource) throws IllegalArgumentException {
        return new FromFileBean(getDelegate().decodeBean(Converter.toDataSource(dataSource)));
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        try {
            return Converter.fromDataSource(getDelegate().encodeBean(((FromFileBean) bean).getDelegate()));
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
