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
public class SeatsDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    protected static final double BAD = .005, UNC = .05;
    
    private double bad_ = BAD;
    private double uncertain_ = UNC;
    private boolean enabled_ = true;

    public SeatsDiagnosticsConfiguration() {
    }

    public void check() {
        if (bad_ > uncertain_ || uncertain_ >1 || bad_ <0)
            throw new BaseException("Invalid settings in Seats diagnostics");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
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

    @Override
    public SeatsDiagnosticsConfiguration clone() {
        try {
            return (SeatsDiagnosticsConfiguration) super.clone();
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

    private EnhancedPropertyDescriptor badDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("bad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, BAD_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(VARCOV_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor uncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("uncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, UNC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(VARCOV_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, BAD_ID = 2, UNC_ID = 3;
    private static final String ENABLED_DESC = "Enable or not the diagnostic",
            BAD_DESC = "Bad",
            UNC_DESC = "Uncertain";
    private static final String VARCOV_CATEGORY = "Variance/covariance tests";
}
