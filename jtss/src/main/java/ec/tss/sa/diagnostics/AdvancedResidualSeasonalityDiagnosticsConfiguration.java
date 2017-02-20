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
public class AdvancedResidualSeasonalityDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    public static final double SEV = .001, BAD = .01, UNC = .05;

    private double sev_ = SEV, bad_ = BAD, unc_ = UNC;
    private boolean enabled_ = true;
    private boolean qs = false;
    private boolean ftest = true;
    private int flast = 8;
    private int qslast = 0;

    @Override
    public AdvancedResidualSeasonalityDiagnosticsConfiguration clone() {
        try {
            return (AdvancedResidualSeasonalityDiagnosticsConfiguration) super.clone();
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
        desc = fDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = qsDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = flenDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = qslenDesc();
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

    private EnhancedPropertyDescriptor fDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("FTest", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FTEST_ID);
            desc.setShortDescription(FTEST_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor qsDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("QsTest", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, QSTEST_ID);
            desc.setShortDescription(QSTEST_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor flenDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("FTestLastYears", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FLAST_ID);
            desc.setShortDescription(FLAST_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor qslenDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("QsTestLastYears", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, QSLAST_ID);
            desc.setShortDescription(QSLAST_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, UNC_ID = 2, BAD_ID = 3, SEV_ID = 4, FTEST_ID = 5, QSTEST_ID = 6,
            FLAST_ID = 8, QSLAST_ID = 9;
    private static final String ENABLED_DESC = "Enabled",
            FTEST_DESC = "F-Test (seasonal dummies)",
            QSLAST_DESC = "Time span (in years) for QS-Test",
            FLAST_DESC = "Time span (in years) for F-Test",
            QSTEST_DESC = "QS(Ljung-Box)-Test",
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
     * @return the qs
     */
    public boolean isQsTest() {
        return qs;
    }

    /**
     * @param qs the qs to set
     */
    public void setQsTest(boolean qs) {
        this.qs = qs;
    }

    /**
     * @return the ftest
     */
    public boolean isFTest() {
        return ftest;
    }

    /**
     * @param ftest the ftest to set
     */
    public void setFTest(boolean ftest) {
        this.ftest = ftest;
    }

    /**
     * @return the flast
     */
    public int getFTestLastYears() {
        return flast;
    }

    /**
     * @param flast the flast to set
     */
    public void setFTestLastYears(int flast) {
        this.flast = flast;
    }

    /**
     * @return the qslast
     */
    public int getQsTestLastYears() {
        return qslast;
    }

    /**
     * @param qslast the qslast to set
     */
    public void setQsTestLastYears(int qslast) {
        this.qslast = qslast;
    }

    public void check() {
        if (sev_ > bad_ || bad_ > unc_ || unc_ > 1 || sev_ <= 0) {
            throw new BaseException("Invalid settings in thresholds");
        }
        if (qslast < 0 || flast < 0) {
            throw new BaseException("Invalid settings in spans");
        }
    }

}
