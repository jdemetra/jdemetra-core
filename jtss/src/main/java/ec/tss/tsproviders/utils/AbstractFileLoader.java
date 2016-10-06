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
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.IFileLoader;
import java.io.File;
import java.io.FileNotFoundException;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public abstract class AbstractFileLoader<DATA, BEAN extends IFileBean> extends AbstractDataSourceLoader<DATA, BEAN> implements IFileLoader {

    protected File[] paths;

    public AbstractFileLoader(Logger logger, String providerName, TsAsyncMode asyncMode) {
        super(logger, providerName, asyncMode);
        paths = new File[0];
    }

    @Override
    public abstract BEAN decodeBean(DataSource dataSource) throws IllegalArgumentException;

    @Override
    public File[] getPaths() {
        return paths.clone();
    }

    @Override
    public void setPaths(File[] paths) {
        this.paths = paths != null ? paths.clone() : new File[0];
        clearCache();
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        return decodeBean(dataSource).getFile().getPath();
    }

    protected File getRealFile(File file) throws FileNotFoundException {
        return DataSourceSupport.getRealFile(paths, file);
    }
}
