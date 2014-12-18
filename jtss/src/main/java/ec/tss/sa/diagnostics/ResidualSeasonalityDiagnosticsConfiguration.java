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
public class ResidualSeasonalityDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    public static final double SASEV = 0.01, SABAD = 0.05, SAUNC = 0.1, ISEV = 0.01, IBAD = 0.05, IUNC = 0.1, SA3SEV = 0.01, SA3BAD = 0.05, SA3UNC = 0.1;

    private double sASevere_ = SASEV;
    private double sABad_ = SABAD;
    private double sAUncertain_ = SAUNC;
    private double irrSevere_ = ISEV;
    private double irrBad_ = IBAD;
    private double irrUncertain_ = IUNC;
    private double sA3Severe_ = SA3SEV;
    private double sA3Bad_ = SA3BAD;
    private double sA3Uncertain_ = SA3UNC;
    private boolean enabled_ = true;

    public ResidualSeasonalityDiagnosticsConfiguration() {
    }

    @Override
    public ResidualSeasonalityDiagnosticsConfiguration clone() {
        try {
            return (ResidualSeasonalityDiagnosticsConfiguration) super.clone();
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
        desc = sauncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sabadDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sasevDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = irruncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = irrbadDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = irrsevDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sa3uncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sa3badDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = sa3sevDesc();
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
        if (sASevere_ > sABad_ || sABad_ > sAUncertain_ || sAUncertain_ > 1 || sASevere_ <= 0)
            throw new BaseException("Invalid settings");
        if (sA3Severe_ > sA3Bad_ || sA3Bad_ > sA3Uncertain_ || sA3Uncertain_ > 1 || sA3Severe_ <= 0)
            throw new BaseException("Invalid settings");
        if (irrSevere_ > irrBad_ || irrBad_ > irrUncertain_ || irrUncertain_ > 1 || irrSevere_ <= 0)
            throw new BaseException("Invalid settings");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public double getSAUncertain() {
        return sAUncertain_;
    }

    public void setSAUncertain(double value) {
        sAUncertain_ = value;
    }

    public double getSABad() {
        return sABad_;
    }

    public void setSABad(double value) {
        sABad_ = value;
    }

    public double getSASevere() {
        return sASevere_;
    }

    public void setSASevere(double value) {
        sASevere_ = value;
    }

    public double getIrrUncertain() {
        return irrUncertain_;
    }

    public void setIrrUncertain(double value) {
        irrUncertain_ = value;
    }

    public double getIrrBad() {
        return irrBad_;
    }

    public void setIrrBad(double value) {
        irrBad_ = value;
    }

    public double getIrrSevere() {
        return irrSevere_;
    }

    public void setIrrSevere(double value) {
        irrSevere_ = value;
    }

    public double getSA3Uncertain() {
        return sA3Uncertain_;
    }

    public void setSA3Uncertain(double value) {
        sA3Uncertain_ = value;
    }

    public double getSA3Bad() {
        return sA3Bad_;
    }

    public void setSA3Bad(double value) {
        sA3Bad_ = value;
    }

    public double getSA3Severe() {
        return sA3Severe_;
    }

    public void setSA3Severe(double value) {
        sA3Severe_ = value;
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

    private EnhancedPropertyDescriptor sauncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SAUncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SAUNC_ID);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(SA_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sabadDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SABad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SABAD_ID);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(SA_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sasevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SASevere", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SASEV_ID);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(SA_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor irruncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("IrrUncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, IRRUNC_ID);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(IRR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor irrbadDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("IrrBad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, IRRBAD_ID);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(IRR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor irrsevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("IrrSevere", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, IRRSEV_ID);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(IRR_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sa3uncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SA3Uncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SA3UNC_ID);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(SA3_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sa3badDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SA3Bad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SA3BAD_ID);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(SA_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sa3sevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SA3Severe", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SA3SEV_ID);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(SA3_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, SAUNC_ID = 2, SABAD_ID = 3, SASEV_ID = 4,
            IRRUNC_ID = 5, IRRBAD_ID = 6, IRRSEV_ID = 7, SA3UNC_ID = 8, SA3BAD_ID = 9, SA3SEV_ID = 10;
    private static final String
            ENABLED_DESC = "Enabled",
            UNC_DESC = "Uncertain",
            BAD_DESC = "Bad",
            SEV_DESC = "Severe";
    private static final String
            APPEARANCE_CATEGORY = "Appearance",
            SA_CATEGORY = "SA series (complete)",
            IRR_CATEGORY = "Irregular (complete)",
            SA3_CATEGORY = "SA series (last 3 years)";
}
