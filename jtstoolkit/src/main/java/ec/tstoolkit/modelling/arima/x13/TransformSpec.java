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

package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TransformSpec implements Cloneable, InformationSetSerializable {

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
         dic.put(InformationSet.item(prefix, ADJUST), String.class);
         dic.put(InformationSet.item(prefix, AICDIFF), Double.class);
         dic.put(InformationSet.item(prefix, FN), String.class);
         dic.put(InformationSet.item(prefix, CONST), Double.class);
   }

    private DefaultTransformationType fn_ = DefaultTransformationType.None;
    private LengthOfPeriodType adjust_ = LengthOfPeriodType.None;
    private double aicdiff_ = DEF_AICDIFF;
    private double const_;
    //private double power_ = 1;
    public static final double DEF_AICDIFF = -2;

    public TransformSpec() {
    }

    public void reset() {
        fn_ = DefaultTransformationType.None;
        adjust_ = LengthOfPeriodType.None;
        aicdiff_ = DEF_AICDIFF;
        const_ = 0;
    }

    public DefaultTransformationType getFunction() {
        return fn_;
    }

    public void setFunction(DefaultTransformationType value) {
        fn_ = value;
    }

    public LengthOfPeriodType getAdjust() {
        return adjust_;
    }

    public void setAdjust(LengthOfPeriodType value) {
        adjust_ = value;
    }

    public double getAICDiff() {
        return aicdiff_;
    }

    public void setAICDiff(double value) {
        aicdiff_ = value;
    }

    public double getConst() {
        return const_;
    }

    public void setConst(double value) {
        const_ = value;
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
        if (adjust_ != LengthOfPeriodType.None) {
            return false;
        }
        if (aicdiff_ != DEF_AICDIFF) {
            return false;
        }
        if (const_ != 0) {
            return false;
        }
        if (fn_ != DefaultTransformationType.None) {
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
        hash = 79 * hash + Objects.hashCode(this.fn_);
        hash = 79 * hash + Objects.hashCode(this.adjust_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.aicdiff_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.const_);
//        hash = 79 * hash + (int) (Double.doubleToLongBits(this.power_) ^ (Double.doubleToLongBits(this.power_) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TransformSpec && equals((TransformSpec) obj));
    }

    private boolean equals(TransformSpec other) {
        return adjust_ == other.adjust_ && aicdiff_ == other.aicdiff_ && const_ == other.const_
                && fn_ == other.fn_; // && power_ == other.power_;
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

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || fn_ != DefaultTransformationType.None) {
            info.add(FN, fn_.name());
        }
        if (verbose || adjust_ != LengthOfPeriodType.None) {
            info.add(ADJUST, adjust_.name());
        }
        if (verbose || aicdiff_ != DEF_AICDIFF) {
            info.add(AICDIFF, aicdiff_);
        }
        if (verbose || const_ != 0) {
            info.add(CONST, const_);
        }
//        if (power_ != 1) {
//            info.add(POWER, aicdiff_);
//        }

        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            String fn = info.get(FN, String.class);
            if (fn != null) {
                fn_ = DefaultTransformationType.valueOf(fn);
            }
            String adjust = info.get(ADJUST, String.class);
            if (adjust != null) {
                adjust_ = LengthOfPeriodType.valueOf(adjust);
            }
            Double aic = info.get(AICDIFF, Double.class);
            if (aic != null) {
                aicdiff_ = aic;
            }
//            Double power = info.get(POWER, Double.class);
//            if (power != null) {
//                power_ = power;
//            }
            Double cnt = info.get(CONST, Double.class);
            if (cnt != null) {
                const_ = cnt;
            }
//            Boolean units = info.get(UNITS, Boolean.class);
//            if (units != null) {
//                units_ = units;
//            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
