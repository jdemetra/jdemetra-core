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
public class SpectralDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    public static final double SENSITIVITY = 6.0 / 52;
    public static final int LENGTH = 8;

    private boolean enabled_ = false;
    private double sens_ = SENSITIVITY;
    private int length_ = LENGTH;
    private boolean strict_ = false;

    public SpectralDiagnosticsConfiguration() {
    }

    public void check() {
        if (sens_ < 3.0 / 52)
            throw new BaseException("Value is too low (should be grater than 3/52)");
        if (length_ != 0 && length_ < 6)
            throw new BaseException("Value is too low (should be 0 or greater than 5)");
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    public double getSensitivity() {
        return sens_;
    }

    public void setSensitivity(double value) {
        sens_ = value;
    }

    public int getLength() {
        return length_;
    }

    public void setLength(int value) {
        length_ = value;
    }

    public boolean isStrict() {
        return strict_;
    }

    public void setStrict(boolean value) {
        strict_ = value;
    }

    @Override
    public SpectralDiagnosticsConfiguration clone() {
        try {
            return (SpectralDiagnosticsConfiguration) super.clone();
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
        desc = sensDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = lengthDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = strictDesc();
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
            edesc.setCategory(APPEARANCE_CATEGORY);
            desc.setShortDescription(ENABLED_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor sensDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Sensitivity", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SENS_ID);
            edesc.setCategory(SPECTRAL_CATEGORY);
            desc.setShortDescription(SENS_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor lengthDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Length", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LENGTH_ID);
            edesc.setCategory(SPECTRAL_CATEGORY);
            desc.setShortDescription(LENGTH_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor strictDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Strict", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, STRICT_ID);
            edesc.setCategory(SPECTRAL_CATEGORY);
            desc.setShortDescription(STRICT_DESC);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int ENABLED_ID = 1, SENS_ID = 2, LENGTH_ID = 3, STRICT_ID = 4;
    private static final String ENABLED_DESC = "Enable or not the diagnostic",
            SENS_DESC = "Threshold for the identification of peaks (see X12 documentation; default = 6/52).",
            LENGTH_DESC = "Number of years considered in the spectral analysis (end of the series). 0 for the complete series.",
            STRICT_DESC = "Control that spectral peaks appear on both SA and irregular series. If strict is true, a severe diagnostic is generated when only one series contains a peak. Otherwise, both series must contain a peak.";
    private static final String
            APPEARANCE_CATEGORY = "Appearance",
            SPECTRAL_CATEGORY = "Spectral test";
}
