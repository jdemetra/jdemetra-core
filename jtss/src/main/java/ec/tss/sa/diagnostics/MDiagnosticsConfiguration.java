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
public class MDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    protected static final double SEVERE = 2, BAD = 1;

    private double bad_ = BAD;
    private double severe_ = SEVERE;
    private boolean all_ = true;
    private boolean enabled_ = true;

    public MDiagnosticsConfiguration() {
    }

    @Override
    public MDiagnosticsConfiguration clone() {
        try {
            return (MDiagnosticsConfiguration) super.clone();
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
        desc = allDesc();
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
        return descs;
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void check() {
        if (bad_ > severe_ || bad_ <0 || severe_ > 3)
            throw new BaseException("Invalid settings in M-diagnostics");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public boolean isUseAll() {
        return all_;
    }

    public void setUseAll(boolean value) {
        all_ = value;
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

    private EnhancedPropertyDescriptor enabledDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("enabled", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ENABLED_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(ENABLED_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor allDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("UseAll", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ALL_ID);
            desc.setShortDescription(ALL_DESC);
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
            edesc.setCategory(THRESHOLDS_CATEGORY);
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
            edesc.setCategory(THRESHOLDS_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, ALL_ID = 2, SEV_ID = 3, BAD_ID = 4;
    private static final String
            ENABLED_DESC = "Enable or not the diagnostic",
            ALL_DESC = "Use all M-statistics",
            SEV_DESC = "Severe",
            BAD_DESC = "Bad";
    private static final String
            THRESHOLDS_CATEGORY = "Thresholds";
}
