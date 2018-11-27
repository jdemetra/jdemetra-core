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
package demetra.tramo;

import demetra.design.Development;
import demetra.timeseries.TimeSelector;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class EstimateSpec {

    public static final String SPAN = "span",
            EML = "eml",
            TOL = "tol",
            UBP = "ubp";

    private TimeSelector span = TimeSelector.all();
    private boolean ml = true;
    private double tol = DEF_TOL, ubp = DEF_UBP;
    public static final double DEF_TOL = 1e-7, DEF_UBP = .96;

    public EstimateSpec() {
    }

    public EstimateSpec(EstimateSpec other) {
        this.ml = other.ml;
        this.span = other.span.clone();
    }

    public void reset() {
        span=TimeSelector.all();
        ml = true;
        tol = DEF_TOL;
        ubp = DEF_UBP;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector span) {
        this.span = span;
    }

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public void setMaximumLikelihood(boolean value) {
        ml = value;
    }

    public double getTol() {
        return tol;
    }

    public void setTol(double value) {
        if (value <= 0 || value > 1e-2) {
            throw new TramoException("Invalid Tol parameter");
        }
        tol = value;
    }

    public double getUbp() {
        return ubp;
    }

    public void setUbp(double value) {
        ubp = value;
    }

    public boolean isDefault() {
        return ml && tol == DEF_TOL && ubp == DEF_UBP && span.getType() == TimeSelector.SelectionType.All;
    }

    @Override
    public EstimateSpec clone() {
        try {
            EstimateSpec spec = (EstimateSpec) super.clone();
            spec.span = span.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.ml ? 1 : 0);
        hash = 13 * hash + Double.hashCode(this.tol);
        hash = 13 * hash + Double.hashCode(this.ubp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EstimateSpec && equals((EstimateSpec) obj));
    }

    private boolean equals(EstimateSpec other) {
        return ml == other.ml && tol == other.tol && ubp == other.ubp && Objects.equals(other.span, span);
    }

}
