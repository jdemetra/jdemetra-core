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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TransformSpec implements Cloneable, InformationSetSerializable {

    public static final String SPAN = "span",
            FN = "function",
            FCT = "fct",
            UNITS = "units",
            PRELIMINARYCHECK = "preliminarycheck";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, FN), String.class);
        dic.put(InformationSet.item(prefix, FCT), Double.class);
        dic.put(InformationSet.item(prefix, UNITS), Boolean.class);
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
    }

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private double fct_ = DEF_FCT;
    private boolean units_;
    private boolean preliminaryCheck_ = true;
    private DefaultTransformationType fn_ = DefaultTransformationType.None;
    public static final double DEF_FCT = 0.95;

    public TransformSpec() {
    }

    public void reset() {
        span_ = new TsPeriodSelector();
        fct_ = DEF_FCT;
        units_ = false;
        preliminaryCheck_ = true;
        fn_ = DefaultTransformationType.None;
    }

    public TsPeriodSelector getSpan() {
        return span_;
    }

    public void setSpan(TsPeriodSelector value) {
        if (value == null) {
            span_.all();
        } else {
            span_ = value;
        }
    }

    public DefaultTransformationType getFunction() {
        return fn_;
    }

    public void setFunction(DefaultTransformationType fn) {
        fn_ = fn;
    }

    public double getFct() {
        return fct_;
    }

    public void setFct(double value) {
        fct_ = value;
    }

    public boolean isUnits() {
        return units_;
    }

    public void setUnits(boolean value) {
        units_ = value;
    }
    
    public boolean isPreliminaryCheck() {
        return preliminaryCheck_;
    }
    
    public void setPreliminaryCheck(boolean value) {
        preliminaryCheck_ = value;
    }

    public boolean isDefault() {
        return units_ == false && fn_ == DefaultTransformationType.None
                && fct_ == DEF_FCT && (span_ == null || span_.getType() == PeriodSelectorType.All)
                && preliminaryCheck_;
    }

    @Override
    public TransformSpec clone() {
        try {
            TransformSpec spec = (TransformSpec) super.clone();
            spec.span_ = span_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(TransformSpec other) {
        if (other == null) {
            return isDefault();
        }
        return fct_ == other.fct_ && fn_ == other.fn_ && units_ == other.units_
                && Objects.equals(span_, other.span_) && preliminaryCheck_ == other.preliminaryCheck_;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TransformSpec && equals((TransformSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.span_);
        hash = 61 * hash + Jdk6.Double.hashCode(this.fct_);
        hash = 61 * hash + (this.units_ ? 1 : 0);
        hash = 61 * hash + Objects.hashCode(this.fn_);
        return hash;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        if (verbose || fn_ != DefaultTransformationType.None) {
            info.add(FN, fn_.name());
        }
        if (verbose || fct_ != DEF_FCT) {
            info.add(FCT, fct_);
        }
        if (verbose || units_) {
            info.add(UNITS, units_);
        }
        if (verbose || !preliminaryCheck_) {
            info.add(PRELIMINARYCHECK, preliminaryCheck_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            TsPeriodSelector span = info.get(SPAN, TsPeriodSelector.class);
            if (span != null) {
                span_ = span;
            }
            String fn = info.get(FN, String.class);
            if (fn != null) {
                fn_ = DefaultTransformationType.valueOf(fn);
            }
            Double fct = info.get(FCT, Double.class);
            if (fct != null) {
                fct_ = fct;
            }
            Boolean units = info.get(UNITS, Boolean.class);
            if (units != null) {
                units_ = units;
            }
            Boolean preliminaryChecks = info.get(PRELIMINARYCHECK, Boolean.class);
            if (preliminaryChecks != null) {
                preliminaryCheck_ = preliminaryChecks;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
