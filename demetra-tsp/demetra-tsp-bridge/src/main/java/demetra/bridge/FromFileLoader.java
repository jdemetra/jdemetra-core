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

import demetra.tsprovider.FileBean;
import demetra.tsprovider.FileLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public class FromFileLoader extends FromDataSourceLoader implements ec.tss.tsproviders.IFileLoader {

    public static ec.tss.tsproviders.@NonNull IFileLoader fromFileLoader(@NonNull FileLoader<? extends FileBean> delegate) {
        return delegate instanceof ToFileLoader
                ? ((ToFileLoader) delegate).getDelegate()
                : new FromFileLoader(delegate);
    }

    protected FromFileLoader(@NonNull FileLoader<? extends FileBean> delegate) {
        super(delegate);
    }

    @Override
    public @NonNull FileLoader<? extends FileBean> getDelegate() {
        return (FileLoader<? extends FileBean>) super.getDelegate();
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
    public void setPaths(@Nullable File[] paths) {
        getDelegate().setPaths(paths);
    }

    @Override
    public @NonNull File[] getPaths() {
        return getDelegate().getPaths();
    }

    @Override
    public ec.tss.tsproviders.@NonNull IFileBean newBean() {
        return FromFileBean.fromFileBean(getDelegate().newBean());
    }

    @Override
    public ec.tss.tsproviders.@NonNull IFileBean decodeBean(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return FromFileBean.fromFileBean(getDelegate().decodeBean(TsConverter.toDataSource(dataSource)));
    }

    @Override
    public ec.tss.tsproviders.@NonNull DataSource encodeBean(@NonNull Object bean) throws IllegalArgumentException {
        Objects.requireNonNull(bean);
        FromFileBean fileBean = getFileBean(bean);
        return TsConverter.fromDataSource(getDelegate().encodeBean(fileBean.getDelegate()));
    }

    private FromFileBean getFileBean(Object bean) throws IllegalArgumentException {
        try {
            return (FromFileBean) bean;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
