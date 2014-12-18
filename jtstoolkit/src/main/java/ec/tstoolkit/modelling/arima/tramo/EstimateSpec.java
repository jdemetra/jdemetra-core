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

    public static final String SPAN = "span",
            EML = "eml",
            TOL = "tol",
            UBP = "ubp";
    
    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, EML), Boolean.class);
        dic.put(InformationSet.item(prefix, TOL), Double.class);
        dic.put(InformationSet.item(prefix, UBP), Double.class);
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
    }

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private boolean eml_ = true;
    private double tol_ = DEF_TOL, ubp_ = DEF_UBP;
    public static final double DEF_TOL = 1e-7, DEF_UBP = .96;

    public EstimateSpec() {
    }

    public void reset() {
        span_ = new TsPeriodSelector();
        eml_ = true;
        tol_ = DEF_TOL;
        ubp_ = DEF_UBP;
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

    public boolean isEML() {
        return eml_;
    }

    public void setEML(boolean value) {
        eml_ = value;
    }

    public double getTol() {
        return tol_;
    }

    public void setTol(double value) {
        if (value <= 0 || value > 1e-2) {
            throw new TramoException("Invalid Tol parameter");
        }
        tol_ = value;
    }

    public double getUbp() {
        return ubp_;
    }

    public void setUbp(double value) {
        ubp_ = value;
    }

    public boolean isDefault() {
        return eml_ && tol_ == DEF_TOL && ubp_ == DEF_UBP && span_.getType() == PeriodSelectorType.All;
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
        hash = 13 * hash + (this.eml_ ? 1 : 0);
        hash = 13 * hash + Jdk6.Double.hashCode(this.tol_);
        hash = 13 * hash + Jdk6.Double.hashCode(this.ubp_);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EstimateSpec && equals((EstimateSpec) obj));
    }

    private boolean equals(EstimateSpec other) {
        return eml_ == other.eml_ && tol_ == other.tol_ && ubp_ == other.ubp_ && Objects.equals(other.span_, span_);
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
        if (verbose || !eml_) {
            info.add(EML, eml_);
        }
        if (verbose || tol_ != DEF_TOL) {
            info.add(TOL, tol_);
        }
        if (verbose || ubp_ != DEF_UBP) {
            info.add(UBP, ubp_);
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
            Double ubp = info.get(UBP, Double.class);
            if (ubp != null) {
                ubp_ = ubp;
            }
            Boolean eml = info.get(EML, Boolean.class);
            if (eml != null) {
                eml_ = eml;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
