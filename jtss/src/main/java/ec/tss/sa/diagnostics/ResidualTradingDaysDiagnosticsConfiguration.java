/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tss.sa.diagnostics;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.descriptors.EnhancedPropertyDescriptor;
import ec.tstoolkit.descriptors.IPropertyDescriptors;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class ResidualTradingDaysDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    /**
     * @return the ar
     */
    public boolean isArModelling() {
        return ar;
    }

    /**
     * @param ar the ar to set
     */
    public void setArModelling(boolean ar) {
        this.ar = ar;
    }

    public static final double SEV = .001, BAD = .01, UNC = .05;

    private double sev_ = SEV, bad_ = BAD, unc_ = UNC;
    private boolean enabled_ = true;
    private boolean ar=true;
    private int span = 8;

    @Override
    public ResidualTradingDaysDiagnosticsConfiguration clone() {
        try {
            return (ResidualTradingDaysDiagnosticsConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public double getSevereThreshold() {
        return sev_;
    }

    public double getBadThreshold() {
        return bad_;
    }

    public double getUncertainThreshold() {
        return unc_;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean enabled) {
        enabled_ = enabled;
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = enabledDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = arDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = spanDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = uncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = badDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sevDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private EnhancedPropertyDescriptor enabledDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("enabled", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ENABLED_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(ENABLED_DESC);
            edesc.setCategory(APPEARANCE_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor arDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("arModelling", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, AR_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(AR_DESC);
            edesc.setCategory(APPEARANCE_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor uncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("UncertainThreshold", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, UNC_ID);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(THRESHOLD_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor badDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("BadThreshold", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BAD_ID);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(THRESHOLD_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SevereThreshold", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEV_ID);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(THRESHOLD_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Span", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            desc.setShortDescription(SPAN_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, UNC_ID = 2, BAD_ID = 3, SEV_ID = 4, SPAN_ID = 5, AR_ID=6;
    private static final String ENABLED_DESC = "Enabled",
            AR_DESC = "Auto-regressive modelling",
            SPAN_DESC = "Time span (in years)",
             UNC_DESC = "Uncertain",
            BAD_DESC = "Bad",
            SEV_DESC = "Severe";
    private static final String TEST_CATEGORY = "Test", THRESHOLD_CATEGORY = "Threshold",
            APPEARANCE_CATEGORY = "Appearance";

    /**
     * @param sev_ the sev_ to set
     */
    public void setSevereThreshold(double sev_) {
        this.sev_ = sev_;
    }

    /**
     * @param bad_ the bad_ to set
     */
    public void setBadThreshold(double bad_) {
        this.bad_ = bad_;
    }

    /**
     * @param unc_ the unc_ to set
     */
    public void setUncertainThreshold(double unc_) {
        this.unc_ = unc_;
    }

    /**
     * @return the flast
     */
    public int getSpan() {
        return span;
    }

    /**
     * @param span the span to set
     */
    public void setSpan(int span) {
        this.span = span;
    }


    public void check() {
        if (sev_ > bad_ || bad_ > unc_ || unc_ > 1 || sev_ <= 0) {
            throw new BaseException("Invalid settings in thresholds");
        }
        if (span < 0) {
            throw new BaseException("Invalid settings in span");
        }
    }

}
