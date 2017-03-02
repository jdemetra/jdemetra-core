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
package ec.tss.sa.output;

import ec.tss.sa.SaManager;
import ec.tstoolkit.utilities.Jdk6;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvMatrixOutputConfiguration extends BasicConfiguration implements Cloneable {

    public static final String NAME = "demetra_m";

    private String[] items_;
    private File folder_;
    private String name_ = NAME;
    private boolean fullName_;

    public CsvMatrixOutputConfiguration() {
        List<String> details = allDetails(true, SaManager.instance.getProcessors(), SaManager.instance.getDiagnostics());
        items_ = details.toArray(new String[details.size()]);
        fullName_ = true;
    }

    public File getFolder() {
        return folder_;
    }

    public void setFolder(File value) {
        folder_ = value;
    }

    public String getFileName() {
        return name_;
    }

    public void setFileName(String value) {
        name_ = value;
    }

    public List<String> getItems() {
        return Arrays.asList(items_);
    }

    public void setItems(List<String> value) {
        items_ = Jdk6.Collections.toArray(value, String.class);
    }

    public boolean isFullName() {
        return fullName_;
    }

    public void setFullName(boolean fullName) {
        this.fullName_ = fullName;
    }

    @Override
    public CsvMatrixOutputConfiguration clone() {
        try {
            CsvMatrixOutputConfiguration clone = (CsvMatrixOutputConfiguration) super.clone();
            return clone;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
