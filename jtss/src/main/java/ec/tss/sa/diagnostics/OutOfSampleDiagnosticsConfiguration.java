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
 * @author Jean Palate
 */
public class OutOfSampleDiagnosticsConfiguration implements Cloneable, IPropertyDescriptors {

    public static final double BAD = .01, UNC = .1, LENGTH=1.5;

    private double bad_ = BAD;
    private double uncertain_ = UNC;
    private boolean menabled_ = true, venabled_=true;
    private double length_= LENGTH;

    @Override
    public OutOfSampleDiagnosticsConfiguration clone() {
        try {
            return (OutOfSampleDiagnosticsConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
  
    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> descs = new ArrayList<>();
        EnhancedPropertyDescriptor desc = menabledDesc();
        if (desc != null) {
            descs.add(desc);
        }
        desc = venabledDesc();
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
        desc = lenDesc();
        if (desc != null) {
            descs.add(desc);
        }
        return descs;
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }

    public void check() {
        if (bad_ > uncertain_ || uncertain_ < 0)
                throw new BaseException("Invalid settings in "+NAME);
    }

    public boolean isEnabled() {
        return menabled_ || venabled_;
    }

    public void setEnabled(boolean value) {
        menabled_=value;
        venabled_=value;
    }

    public boolean isMeanTestEnabled() {
        return menabled_;
    }

    public void setMeanTestEnabled(boolean value) {
        menabled_ = value;
    }

    public boolean isMSETestEnabled() {
        return menabled_;
    }

    public void setMSETestEnabled(boolean value) {
        menabled_ = value;
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
    
    public double getForecastingLength(){
        return length_;
    }
    
    public void setForecastingLength(double len){
        length_=len;
    }

    private EnhancedPropertyDescriptor menabledDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("meanTestEnabled", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, MENABLED_ID);
            desc.setShortDescription(MENABLED_DESC);
            edesc.setCategory(APPEARANCE_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor venabledDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("MSETestEnabled", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, VENABLED_ID);
            desc.setShortDescription(VENABLED_DESC);
            edesc.setCategory(APPEARANCE_CATEGORY);
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
            edesc.setCategory(TEST_CATEGORY);
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
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor lenDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("forecastingLength", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, LEN_ID);
            desc.setShortDescription(LEN_DESC);
            edesc.setCategory(TEST_CATEGORY);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private static final int MENABLED_ID = 1, VENABLED_ID = 2, BAD_ID = 3, UNC_ID = 4, LEN_ID=5;
    private static final String
            MENABLED_DESC = "Mean test enabled",
            VENABLED_DESC = "MSE test enabled",
            UNC_DESC = "Uncertain",
            BAD_DESC = "Bad",
            LEN_DESC = "Forecasting length";
    private static final String
            APPEARANCE_CATEGORY = "Appearance",
            TEST_CATEGORY = "Test options";
    
    public static String NAME="Out-of-Sample test";
}

