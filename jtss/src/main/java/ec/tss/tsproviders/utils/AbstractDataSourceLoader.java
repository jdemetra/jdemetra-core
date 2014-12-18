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

package ec.tss.tsproviders.utils;

import ec.tss.TsAsyncMode;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceLoader;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public abstract class AbstractDataSourceLoader<DATA, BEAN> extends AbstractDataSourceProvider<DATA> implements IDataSourceLoader {

    public AbstractDataSourceLoader(Logger logger, String providerName, TsAsyncMode asyncMode) {
        super(logger, providerName, asyncMode);
    }

    @Override
    protected DATA loadFromDataSource(DataSource key) throws Exception {
        return loadFromBean(decodeBean(key));
    }

    @Nonnull
    protected abstract DATA loadFromBean(@Nonnull BEAN bean) throws Exception;

    @Override
    public abstract BEAN newBean();

    @Override
    public abstract BEAN decodeBean(DataSource dataSource) throws IllegalArgumentException;

    @Override
    public boolean open(DataSource dataSource) {
        return support.open(dataSource);
    }

    @Override
    public boolean close(DataSource dataSource) {
        if (support.close(dataSource)) {
            cache.invalidate(dataSource);
            return true;
        }
        return false;
    }

    @Override
    public void closeAll() {
        support.closeAll();
    }
}
