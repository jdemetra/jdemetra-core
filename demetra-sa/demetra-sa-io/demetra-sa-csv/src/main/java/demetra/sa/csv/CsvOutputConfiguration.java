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

package demetra.sa.csv;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public final class CsvOutputConfiguration implements Cloneable {
    
    public static final String NAME="series";

    public static final String[] defOutput = {"y", "t", "sa", "s", "i", "ycal"};
    private CsvLayout layout = CsvLayout.List;
    private File folder ;
    private String name=NAME;
    private String[] series;
    private boolean fullName;

    public CsvOutputConfiguration() {
        series = defOutput;
        fullName = true;
    }

    public CsvLayout getPresentation() {
        return layout;
    }

    public void setPresentation(CsvLayout value) {
        layout = value;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File value) {
        folder = value;
    }

     public String getFilePrefix() {
        return name;
    }
    public void setFilePrefix(String value) {
        name = value;
    }

    public List<String> getSeries() {
        return Arrays.asList(series);
    }

    public void setSeries(List<String> value) {
        series =  value.toArray(String[]::new);
    }

    public boolean isFullName() {
        return fullName;
    }

    public void setFullName(boolean fullName) {
        this.fullName = fullName;
    }

    @Override
    public CsvOutputConfiguration clone() {
        try {
            return (CsvOutputConfiguration) super.clone();
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
