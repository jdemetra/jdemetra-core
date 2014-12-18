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

package ec.tss.tsproviders.sdmx;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.sdmx.engine.CunningPlanFactory;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.Params;
import java.io.File;

/**
 *
 * @author Philippe Charles
 */
public class SdmxBean implements IFileBean, IDataSourceBean {

    static final IParam<DataSource, File> X_FILE = Params.onFile(new File(""), "url");
    static final IParam<DataSource, String> X_FACTORY = Params.onString(CunningPlanFactory.NAME, "factory");
    //
    File file;
    String factory;

    public SdmxBean() {
        this.file = X_FILE.defaultValue();
        this.factory = X_FACTORY.defaultValue();
    }

    public SdmxBean(DataSource dataSource) {
        this.file = X_FILE.get(dataSource);
        this.factory = X_FACTORY.get(dataSource);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }
    //</editor-fold>

    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        X_FILE.set(builder, file);
        X_FACTORY.set(builder, factory);
        return builder.build();
    }

    @Deprecated
    public DataSource toDataSource() {
        return toDataSource(SdmxProvider.SOURCE, SdmxProvider.VERSION);
    }

    @Deprecated
    public static SdmxBean from(DataSource dataSource) {
        return new SdmxBean(dataSource);
    }
}
