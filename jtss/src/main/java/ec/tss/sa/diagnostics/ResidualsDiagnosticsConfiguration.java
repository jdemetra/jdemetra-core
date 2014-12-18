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
public class ResidualsDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {
    public static final double NBAD = .01, NUNC = .1,
        TDSEV = .001, TDBAD = .01, TDUNC = .1,
        SSEV = .001, SBAD = .01, SUNC = .1;

    private double normalityBad_ = NBAD;
    private double normalityUncertain_ = NUNC;
    private double specTDSevere_ = TDSEV;
    private double specTDBad_ = TDBAD;
    private double specTDUncertain_ = TDUNC;
    private double specSeasSevere_ = SSEV;
    private double specSeasBad_ = SBAD;
    private double specSeasUncertain_ = SUNC;
    private boolean enabled_ = true;

    public ResidualsDiagnosticsConfiguration() {
    }

    @Override
    public ResidualsDiagnosticsConfiguration clone() {
        try {
            return (ResidualsDiagnosticsConfiguration) super.clone();
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
        desc = niiduncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = niidbadDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = spectduncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = spectdbadDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = spectdsevDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = specsuncDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = specsbadDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = specssevDesc();
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
        if (specTDSevere_ > specTDBad_ || specTDBad_ > specTDUncertain_ || specTDUncertain_ > 1 || specTDSevere_ <= 0)
            throw new BaseException("Invalid settings in td periodogram diagnostics");
        if (specSeasSevere_ > specSeasBad_ || specSeasBad_ > specSeasUncertain_ || specSeasUncertain_ > 1 || specSeasSevere_ <= 0)
            throw new BaseException("Invalid settings in seas periodogram diagnostics");
        if (normalityBad_ > normalityUncertain_ || normalityUncertain_ > 1 || normalityBad_ <= 0)
            throw new BaseException("Invalid settings in normality diagnostics");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public double getNIIDUncertain() {
        return normalityUncertain_;
    }

    public void setNIIDUncertain(double value) {
        normalityUncertain_ = value;
    }

    public double getNIIDBad() {
        return normalityBad_;
    }

    public void setNIIDBad(double value) {
        normalityBad_ = value;
    }

    public double getSpecTDUncertain() {
        return specTDUncertain_;
    }

    public void setSpecTDUncertain(double value) {
        specTDUncertain_ = value;
    }

    public double getSpecTDBad() {
        return specTDBad_;
    }

    public void setSpecTDBad(double value) {
        specTDBad_ = value;
    }

    public double getSpecTDSevere() {
        return specTDSevere_;
    }

    public void setSpecTDSevere(double value) {
        specTDSevere_ = value;
    }

    public double getSpecSeasUncertain() {
        return specSeasUncertain_;
    }

    public void setSpecSeasUncertain(double value) {
        specSeasUncertain_ = value;
    }

    public double getSpecSeasBad() {
        return specSeasBad_;
    }

    public void setSpecSeasBad(double value) {
        specSeasBad_ = value;
    }

    public double getSpecSeasSevere() {
        return specSeasSevere_;
    }

    public void setSpecSeasSevere(double value) {
        specSeasSevere_ = value;
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
    
    private EnhancedPropertyDescriptor niiduncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("NIIDUncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, NIIDUNC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(NIDDTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor niidbadDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("NIIDBad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, NIIDBAD_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(NIDDTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor spectduncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecTDUncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECTDUNC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(SPECTDTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor spectdbadDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecTDBad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECTDBAD_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(SPECTDTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor spectdsevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecTDSevere", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECTDSEV_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(SPECTDTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor specsuncDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecSeasUncertain", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECSUNC_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(UNC_DESC);
            edesc.setCategory(SPECSTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor specsbadDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecSeasBad", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECSBAD_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(BAD_DESC);
            edesc.setCategory(SPECSTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor specssevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("SpecSeasSevere", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPECSSEV_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(SEV_DESC);
            edesc.setCategory(SPECSTEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, NIIDUNC_ID = 2, NIIDBAD_ID = 3, SPECTDUNC_ID = 4,
            SPECTDBAD_ID = 5, SPECTDSEV_ID = 6, SPECSUNC_ID = 7, SPECSBAD_ID = 8, SPECSSEV_ID = 9;
    private static final String
            ENABLED_DESC = "Enabled",
            UNC_DESC = "Uncertain",
            BAD_DESC = "Bad",
            SEV_DESC = "Severe";
    private static final String
            APPEARANCE_CATEGORY = "Appearance",
            NIDDTEST_CATEGORY = "NIID test",
            SPECTDTEST_CATEGORY = "Spectral test on trading days frequencies",
            SPECSTEST_CATEGORY = "Spectral test on seasonal frequencies";
}
