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
package ec.tss.tsproviders.common.tsw;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.utils.IParam;
import static ec.tss.tsproviders.utils.Params.onFile;
import java.io.File;

/**
 *
 * @author Mats Maggi
 */
public class TswBean implements IFileBean, IDataSourceBean {
    static final IParam<DataSource, File> FOLDER = onFile(new File(""), "file");
    
    File folder;
    
    public TswBean() {
        folder = FOLDER.defaultValue();
    }
    
    public TswBean(DataSource dataSource) {
        folder = FOLDER.get(dataSource);
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public File getFile() {
        return folder;
    }

    @Override
    public void setFile(File file) {
        this.folder = file;
    }
    //</editor-fold>
    
    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        FOLDER.set(builder, folder);
        return builder.build();
    }
}
