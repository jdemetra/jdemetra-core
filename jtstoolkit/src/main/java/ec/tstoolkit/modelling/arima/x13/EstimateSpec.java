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

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class EstimateSpec implements Cloneable, InformationSetSerializable {

    public static final String SPAN = "span",
            TOL = "tol";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TOL), Double.class);
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
    }

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private double tol_ = DEF_TOL;
    public static final double DEF_TOL = 1e-7;

    public EstimateSpec() {
    }

    public void reset() {
        span_ = new TsPeriodSelector();
        tol_ = DEF_TOL;
    }

    public EstimateSpec(EstimateSpec spec) {
        tol_ = spec.tol_;
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

    public double getTol() {
        return tol_;
    }

    public void setTol(double value) {
        tol_ = value;
    }

    public boolean isDefault() {
        return tol_ == DEF_TOL && span_.getType() == PeriodSelectorType.All;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Jdk6.Double.hashCode(this.tol_);
        return hash;
    }

    @Override
    public EstimateSpec clone() {
        try {
            EstimateSpec spec = (EstimateSpec) super.clone();
            spec.span_ = span_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EstimateSpec && equals((EstimateSpec) obj));
    }

    private boolean equals(EstimateSpec other) {
        return other.tol_ == tol_ && Objects.equals(other.span_, span_);
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        if (verbose || tol_ != DEF_TOL) {
            info.add(TOL, tol_);
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
            Double tol = info.get(TOL, Double.class);
            if (tol != null) {
                tol_ = tol;
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
