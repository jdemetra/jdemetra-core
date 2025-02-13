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

package ec.tss.tsproviders.common.xml;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceBean;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.Params;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Philippe Charles
 */
public class XmlBean implements IFileBean, IDataSourceBean {

    static final IParam<DataSource, File> X_FILE = Params.onFile(java.nio.file.Paths.get("").toFile(), "file");
    static final IParam<DataSource, Charset> X_CHARSET = Params.onCharset(StandardCharsets.UTF_8, "charset");
    static final String EXT = "xml";
    private File file;
    private Charset charset;

    public XmlBean() {
        this.file = X_FILE.defaultValue();
        this.charset = X_CHARSET.defaultValue();
    }

    public XmlBean(DataSource dataSource) {
        this.file = X_FILE.get(dataSource);
        this.charset = X_CHARSET.get(dataSource);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        // ensure that we have the xml extension...
        this.file = java.nio.file.Paths.get(Paths.changeExtension(file.getPath(), EXT)).toFile();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
    //</editor-fold>

    @Override
    public DataSource toDataSource(String providerName, String version) {
        DataSource.Builder builder = DataSource.builder(providerName, version);
        X_FILE.set(builder, file);
        X_CHARSET.set(builder, charset);
        return builder.build();
    }

    @Deprecated
    public DataSource toDataSource() {
        return toDataSource(XmlProvider.SOURCE, XmlProvider.VERSION);
    }

    @Deprecated
    public static XmlBean from(DataSource dataSource) {
        return new XmlBean(dataSource);
    }
}
