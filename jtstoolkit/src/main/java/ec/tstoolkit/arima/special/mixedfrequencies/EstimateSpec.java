/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.design.Development;
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
@Development(status = Development.Status.Preliminary)
public class EstimateSpec implements Cloneable, InformationSetSerializable {

    public static enum Method {

        KalmanFilter,
        Matrix, 
        Cholesky;
        
        public boolean isMatrix(){
            return this != KalmanFilter;
        }
    }

    public static final String SPAN = "span",
            METHOD = "method",
            TOL = "tol";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, TOL), Double.class);
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
    }

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private Method method_ = Method.KalmanFilter;
    private double tol_ = DEF_TOL;
    public static final double DEF_TOL = 1e-9;
    public static final Method DEF_METHOD = Method.KalmanFilter;

    public EstimateSpec() {
    }

    public void reset() {
        span_ = new TsPeriodSelector();
        method_ = DEF_METHOD;
        tol_ = DEF_TOL;
    }

    public TsPeriodSelector getSpan() {
        return span_;
    }

    public void setSpan(TsPeriodSelector span) {
        if (span == null) {
            span_.all();
        } else {
            span_ = span;
        }
    }

    public Method getMethod() {
        return method_;
    }

    public void setMethod(Method value) {
        method_ = value;
    }

    public double getTol() {
        return tol_;
    }

    public void setTol(double value) {
        if (value <= 0 || value > 1e-2) {
            throw new IllegalArgumentException("Invalid Tol parameter");
        }
        tol_ = value;
    }

    public boolean isDefault() {
        return method_ == DEF_METHOD && tol_ == DEF_TOL && span_.getType() == PeriodSelectorType.All;
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
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.method_.hashCode());
        hash = 13 * hash + Jdk6.Double.hashCode(this.tol_);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EstimateSpec && equals((EstimateSpec) obj));
    }

    private boolean equals(EstimateSpec other) {
        return method_ == other.method_ && tol_ == other.tol_ && Objects.equals(other.span_, span_);
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
        if (verbose || method_ != DEF_METHOD) {
            info.add(METHOD, method_.name());
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
            String m = info.get(METHOD, String.class);
            if (m != null) {
                method_ = Method.valueOf(m);
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
