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

import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.descriptors.IPropertyDescriptors;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class TxtSaOutputConfiguration implements IPropertyDescriptors, Cloneable {

    private String folder_ = ".";
    private boolean orig_ = true, sa_ = true, seas_ = true, trend_ = false, irr_ = false, yc_ = false;

    public String getFolder() {
        return folder_;
    }

    public void setFolder(String value) {
        folder_ = value;
    }

    public boolean isSaveOriginal() {
        return orig_;
    }

    public void setSaveOriginal(boolean value) {
        orig_ = value;
    }

    public boolean isSaveSa() {
        return sa_;
    }

    public void setSaveSa(boolean value) {
        sa_ = value;
    }

    public boolean isSaveYc() {
        return yc_;
    }

    public void setSaveYc(boolean value) {
        yc_ = value;
    }

    public boolean isSaveSeas() {
        return seas_;
    }

    public void setSaveSeas(boolean value) {
        seas_ = value;
    }

    public boolean isSaveTrend() {
        return trend_;
    }

    public void setSaveTrend(boolean value) {
        trend_ = value;
    }

    public boolean isSaveIrregular() {
        return irr_;
    }

    public void setSaveIrregular(boolean value) {
        irr_ = value;
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = folderDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = origDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = saDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = ycDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = seasDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = trendDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = irrDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }
    private static final int FOLDER_ID = 1, ORIG_ID = 2, SA_ID = 3, YC_ID = 4, SEAS_ID = 5, TREND_ID = 6, IRR_ID = 7;

    protected EnhancedPropertyDescriptor folderDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("folder", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FOLDER_ID);
            desc.setDisplayName("Folder");
            desc.setShortDescription(FOLDER_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor origDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveOriginal", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ORIG_ID);
            desc.setDisplayName(ORIG_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor saDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveSa", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SA_ID);
            desc.setDisplayName(SA_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor ycDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveYc", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, YC_ID);
            desc.setDisplayName(YC_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor seasDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveSeas", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEAS_ID);
            desc.setDisplayName(SEAS_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor trendDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveTrend", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            desc.setDisplayName(TREND_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    protected EnhancedPropertyDescriptor irrDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveIrregular", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, IRR_ID);
            desc.setDisplayName(IRR_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }
    private static final String FOLDER_DESC = "Defines the folder that will contain the results",
            ORIG_DESC = "Save original series",
            SA_DESC = "Save sa series",
            YC_DESC = "Save calendar corrected series",
            SEAS_DESC = "Save seasonal component",
            TREND_DESC = "Save trend",
            IRR_DESC = "Save irregular component";

    @Override
    public String getDisplayName() {
        return "Misc";
    }

    @Override
    public Object clone() {
        TxtSaOutputConfiguration clone;
        try {
            clone = (TxtSaOutputConfiguration) super.clone();
            return clone;
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
