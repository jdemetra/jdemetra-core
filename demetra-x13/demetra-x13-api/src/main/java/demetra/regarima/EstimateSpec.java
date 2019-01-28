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
package demetra.regarima;

import demetra.timeseries.TimeSelector;
import demetra.timeseries.TimeSelector.SelectionType;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public class EstimateSpec {

    private TimeSelector span = TimeSelector.all();
    private double tol = DEF_TOL;
    public static final double DEF_TOL = 1e-7;

    public EstimateSpec() {
    }

    public void reset() {
        span = TimeSelector.all();
        tol = DEF_TOL;
    }

    public EstimateSpec(EstimateSpec spec) {
        tol = spec.tol;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector value) {
             span = value;
    }

    public double getTol() {
        return tol;
    }

    public void setTol(double value) {
        tol = value;
    }

    public boolean isDefault() {
        return tol == DEF_TOL && span.getType() == SelectionType.All;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Double.hashCode(this.tol);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EstimateSpec && equals((EstimateSpec) obj));
    }

    private boolean equals(EstimateSpec other) {
        return other.tol == tol && Objects.equals(other.span, span);
    }

}
