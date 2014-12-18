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
 * @author Kristof Bayens
 */
public class OutliersDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {
    public static final double SEV = .10, BAD = .05, UNC = .03;

    private double severe_ = SEV;
    private double bad_ = BAD;
    private double uncertain_ = UNC;
    private boolean enabled_ = true;

    public OutliersDiagnosticsConfiguration() {
    }

    @Override
    public OutliersDiagnosticsConfiguration clone() {
        try {
            return (OutliersDiagnosticsConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = enabledDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sevDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = badDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = uncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void check() {
        if (severe_ < bad_ || bad_ < uncertain_ || uncertain_ < 0)
                throw new BaseException("Invalid settings in Annual totals diagnostics");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public double getSevere() {
        return severe_;
    }

    public void setSevere(double value) {
        severe_ = value;
    }

    public double getBad() {
        return bad_;
    }

    public void setBad(double value) {
        bad_ = value;
    }

    public double getUncertain() {
        return uncertain_;
    }

    public void setUncertain(double value) {
        uncertain_ = value;
    }

    private EnhancedPropertyDescriptor enabledDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("enabled", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ENABLED_ID);
            desc.setShortDescription(ENABLED_DESC);
            edesc.setCategory(APPEARANCE_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Severe", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEV_ID);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(RELNR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor badDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Bad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BAD_ID);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(RELNR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor uncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Uncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, UNC_ID);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(RELNR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, SEV_ID = 2, BAD_ID = 3, UNC_ID = 4;
    private static final String
            ENABLED_DESC = "Enabled",
            UNC_DESC = "Uncertain",
            BAD_DESC = "Bad",
            SEV_DESC = "Severe";
    private static final String
            APPEARANCE_CATEGORY = "Appearance",
            RELNR_CATEGORY = "Relative number of outliers";
}
