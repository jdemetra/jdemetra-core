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
public class TxtOutputConfiguration extends BasicConfiguration implements Cloneable {

    public static final String[] defOutput = {"y", "t", "sa", "s", "i", "ycal"};
    private File folder_;
    private String[] series_;

    public TxtOutputConfiguration() {
        series_ = defOutput;
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

    @Override
    public TxtOutputConfiguration clone() {
        try {
            return (TxtOutputConfiguration) super.clone();
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
