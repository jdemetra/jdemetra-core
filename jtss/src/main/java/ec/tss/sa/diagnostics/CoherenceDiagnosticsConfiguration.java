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
import ec.tstoolkit.timeseries.TsException;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class CoherenceDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    private static final double TOL = 1e-3, ERR = .5, SEV = .1, BAD = .05, UNC = .01;
    private static final int SHORT = 7;
    private double m_Tolerance = TOL;
    private double m_Error = ERR;
    private double m_Severe = SEV;
    private double m_Bad = BAD;
    private double m_Uncertain = UNC;
    private boolean m_Enabled = true;
    private int m_ShortSeries = SHORT;

    public CoherenceDiagnosticsConfiguration() {
    }

    public void check() {
        if (m_Error < m_Severe || m_Severe < m_Bad || m_Bad < m_Uncertain || m_Uncertain < 0) {
            throw new BaseException("Invalid settings in Annual totals diagnostics");
        }
    }

    public boolean isEnabled() {
        return m_Enabled;
    }

    public void setEnabled(boolean value) {
        m_Enabled = value;
    }

    public double getTolerance() {
        return m_Tolerance;
    }

    public void setTolerance(double value) {
        if (value <= 0 || value > 0.1) {
            throw new BaseException("Should be in ]0, 0.1]");
        }
        m_Tolerance = value;
    }

    public double getError() {
        return m_Error;
    }

    public void setError(double value) {
        m_Error = value;
    }

    public double getSevere() {
        return m_Severe;
    }

    public void setSevere(double value) {
        m_Severe = value;
    }

    public double getBad() {
        return m_Bad;
    }

    public void setBad(double value) {
        m_Bad = value;
    }

    public double getUncertain() {
        return m_Uncertain;
    }

    public void setUncertain(double value) {
        m_Uncertain = value;
    }

    public int getShortSeries() {
        return m_ShortSeries;
    }

    public void setShortSeries(int value) {
        if (value <= 3) {
            throw new TsException("Invalid value. Should be > 3");
        }
        m_ShortSeries = value;
    }

    @Override
    public CoherenceDiagnosticsConfiguration clone() {
        try {
            return (CoherenceDiagnosticsConfiguration) super.clone();
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
        desc = tolDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = errDesc();
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
        desc = shortDesc();
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

    private EnhancedPropertyDescriptor tolDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("tolerance", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TOL_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(TOL_DESC);
            edesc.setReadOnly(m_Enabled);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor errDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("error", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ERR_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(ERR_DESC);
            edesc.setReadOnly(m_Enabled);
            edesc.setCategory(ANNUAL_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sevDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("severe", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SEV_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(SEV_DESC);
            edesc.setReadOnly(m_Enabled);
            edesc.setCategory(ANNUAL_CATEGORY);
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
            edesc.setReadOnly(m_Enabled);
            edesc.setCategory(ANNUAL_CATEGORY);
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
            edesc.setReadOnly(m_Enabled);
            edesc.setCategory(ANNUAL_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor shortDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("shortSeries", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SHORT_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(SHORT_DESC);
            edesc.setReadOnly(m_Enabled);
            edesc.setCategory(MISC_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }
    private static final int ENABLED_ID = 1, TOL_ID = 2, ERR_ID = 3, SEV_ID = 4, BAD_ID = 5, UNC_ID = 6, SHORT_ID = 7;
    private static final String ENABLED_DESC = "Enable or not the diagnostic",
            TOL_DESC = "Tolerance",
            ERR_DESC = "Error",
            SEV_DESC = "Severe",
            BAD_DESC = "Bad",
            UNC_DESC = "Uncertain",
            SHORT_DESC = "Length of series considered as short (in years)";
    private static final String ANNUAL_CATEGORY = "Annual totals (lower bound)",
            MISC_CATEGORY = "Miscellaneous";
}
