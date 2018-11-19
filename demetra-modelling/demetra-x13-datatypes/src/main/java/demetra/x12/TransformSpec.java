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

package demetra.x12;

import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TransformSpec {

    private TransformationType fn = TransformationType.None;
    private LengthOfPeriodType adjust = LengthOfPeriodType.None;
    private double aicdiff = DEF_AICDIFF;
    private double constant;
    //private double power_ = 1;
    public static final double DEF_AICDIFF = -2;

    public TransformSpec() {
    }
    
    public TransformSpec(TransformSpec other){
        this.fn=other.fn;
        this.aicdiff=other.aicdiff;
        this.adjust=other.adjust;
        this.constant=other.constant;
    }

    public void reset() {
        fn = TransformationType.None;
        adjust = LengthOfPeriodType.None;
        aicdiff = DEF_AICDIFF;
        constant = 0;
    }

    public TransformationType getFunction() {
        return fn;
    }

    public void setFunction(TransformationType value) {
        fn = value;
    }

    public LengthOfPeriodType getAdjust() {
        return adjust;
    }

    public void setAdjust(LengthOfPeriodType value) {
        adjust = value;
    }

    public double getAICDiff() {
        return aicdiff;
    }

    public void setAICDiff(double value) {
        aicdiff = value;
    }

    public double getConst() {
        return constant;
    }

    public void setConst(double value) {
        constant = value;
    }

//    public double getPower() {
//        switch (m_function) {
//            case BoxCox:
//                return m_power;
//            case Log:
//                return 0;
//            case Sqrt:
//                return 0.5;
//            default:
//                return 1;
//        }
//    }
//
//    public void setPower(double value) {
//        m_power = value;
//    }
    public boolean isDefault() {
        if (adjust != LengthOfPeriodType.None) {
            return false;
        }
        if (aicdiff != DEF_AICDIFF) {
            return false;
        }
        if (constant != 0) {
            return false;
        }
        if (fn != TransformationType.None) {
            return false;
        }
        return true;
    }

    @Override
    public TransformSpec clone() {
        try {
            TransformSpec spec = (TransformSpec) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.fn);
        hash = 79 * hash + Objects.hashCode(this.adjust);
        hash = 79 * hash + Double.hashCode(this.aicdiff);
        hash = 79 * hash + Double.hashCode(this.constant);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TransformSpec && equals((TransformSpec) obj));
    }

    private boolean equals(TransformSpec other) {
        return adjust == other.adjust && aicdiff == other.aicdiff && constant == other.constant
                && fn == other.fn; // && power_ == other.power_;
    }
    public static final String FN = "function",
            ADJUST = "adjust",
            //            UNITS = "units",
            AICDIFF = "aicdiff",
            //POWER = "power",
            CONST = "const";
    private static final String[] DICTIONARY = new String[]{
        FN, ADJUST,
        //         UNITS, POWER,
        AICDIFF, CONST
    };

}
