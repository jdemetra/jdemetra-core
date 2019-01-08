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
@Development(status = Development.Status.Beta)
@lombok.Data
public final class EstimateSpec implements Cloneable{

    public static final double DEF_TOL = 1e-7, DEF_UBP = .96;
    
    @lombok.NonNull
    private TimeSelector span = TimeSelector.all();
    private boolean maximumLikelihood = true;
    private double tol = DEF_TOL, ubp = DEF_UBP;

    private static final EstimateSpec DEFAULT=new EstimateSpec();
    
    public EstimateSpec() {
    }

    public void reset() {
        span=TimeSelector.all();
        maximumLikelihood = true;
        tol = DEF_TOL;
        ubp = DEF_UBP;
    }

    public void setTol(double value) {
        if (value <= 0 || value > 1e-2) {
            throw new TramoException("Invalid Tol parameter");
        }
        tol = value;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
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
}
