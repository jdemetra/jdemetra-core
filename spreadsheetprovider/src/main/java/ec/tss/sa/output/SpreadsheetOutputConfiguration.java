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

import ec.tstoolkit.utilities.Jdk6;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SpreadsheetOutputConfiguration extends BasicConfiguration implements Cloneable {
 
    public static final String NAME="demetra";

    private boolean savemodel_;
    private boolean verticalorientation_;
    private SpreadsheetLayout layout_;
    private static final String[] defOutput = {"y", "t", "sa", "s", "i", "ycal"};
    private File folder_ ;
    private String name_=NAME;
    private String[] series_;
    private boolean fullName_;
    
    public enum SpreadsheetLayout {

        BySeries,
        ByComponent,
        OneSheet
    }

    public SpreadsheetOutputConfiguration() {
        series_ = defOutput;
        layout_ = SpreadsheetLayout.BySeries;
        verticalorientation_ = true;
        savemodel_ = false;
        fullName_ = true;
    }

    public File getFolder() {
        return folder_;
    }

    public void setFolder(File value) {
        folder_ = value;
    }

    public List<String> getSeries() {
        return Arrays.asList(series_);
    }

    public void setSeries(List<String> value) {
        series_ = Jdk6.Collections.toArray(value, String.class);
    }

    public boolean isSaveModel() {
        return savemodel_;
    }

    public void setSaveModel(boolean value) {
        savemodel_ = value;
    }

    public boolean isVerticalOrientation() {
        return verticalorientation_;
    }

    public void setVerticalOrientation(boolean value) {
        verticalorientation_ = value;
    }

    public SpreadsheetLayout getLayout() {
        return layout_;
    }

    public void setLayout(SpreadsheetLayout value) {
        layout_ = value;
    }

     public String getFileName() {
        return name_;
    }
    public void setFileName(String value) {
        name_ = value;
    }

    public boolean isFullName() {
        return fullName_;
    }

    public void setFullName(boolean fullName) {
        this.fullName_ = fullName;
    }

   @Override
    public SpreadsheetOutputConfiguration clone() {
        try {
            return (SpreadsheetOutputConfiguration) super.clone();
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
