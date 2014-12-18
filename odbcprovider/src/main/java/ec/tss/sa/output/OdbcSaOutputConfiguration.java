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
public class OdbcSaOutputConfiguration implements IPropertyDescriptors, Cloneable {
    private String dsn_ = "SAResults";
    private boolean orig_ = true, sa_ = true, seas_ = true, trend_ = true, irr_ = true, cal_ = true, model_ = true;

    public OdbcSaOutputConfiguration() {

    }

    public String getDSN() {
        return dsn_;
    }
    public void setDSN(String value) {
        dsn_ = value;
    }

    public boolean isSaveOriginal() {
        return orig_;
    }
    public void setSaveOriginal(boolean value) {
        orig_ = value;
    }

    public boolean isSaveCalendar() {
        return cal_;
    }
    public void setSaveCalendar(boolean value) {
        cal_ = value;
    }

    public boolean isSaveSa() {
        return sa_;
    }
    public void setSaveSa(boolean value) {
        sa_ = value;
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

    public boolean isSaveModel() {
        return model_;
    }
    public void setSaveModel(boolean value) {
        model_ = value;
    }
    
    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        List<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = dsnDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = origDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = calDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = saDesc();
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
        desc = modelDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    private static final int DSN_ID = 1, ORIG_ID = 2, CAL_ID = 3, SA_ID = 4, SEAS_ID = 5, TREND_ID = 6, IRR_ID = 7, MODEL_ID = 8;

    protected EnhancedPropertyDescriptor dsnDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("dsn", this.getClass(), "getDSN", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DSN_ID);
            desc.setDisplayName("DSN");
            desc.setShortDescription(DSN_DESC);
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

    protected EnhancedPropertyDescriptor calDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveCalendar", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, CAL_ID);
            desc.setDisplayName(CAL_DESC);
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

    protected EnhancedPropertyDescriptor modelDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("saveModel", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, MODEL_ID);
            desc.setDisplayName(MODEL_DESC);
            //edesc.setReadOnly(true);
            return edesc;
        }
        catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final String DSN_DESC = "Defines the DSN",
            ORIG_DESC = "Save original series",
            CAL_DESC = "Save calendar effects",
            SA_DESC = "Save sa series",
            SEAS_DESC = "Save seasonal component",
            TREND_DESC = "Save trend",
            IRR_DESC = "Save irregular component",
            MODEL_DESC = "Save model";

    @Override
    public String getDisplayName() {
        return "Misc";
    }

    @Override
    public Object clone() {
        OdbcSaOutputConfiguration clone = new OdbcSaOutputConfiguration();
        clone.setDSN(dsn_);
        clone.setSaveOriginal(orig_);
        clone.setSaveCalendar(cal_);
        clone.setSaveSa(sa_);
        clone.setSaveSeas(seas_);
        clone.setSaveTrend(trend_);
        clone.setSaveIrregular(irr_);
        clone.setSaveModel(model_);
        return clone;
    }
}
