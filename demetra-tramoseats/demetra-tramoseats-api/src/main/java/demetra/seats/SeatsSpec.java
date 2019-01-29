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
package demetra.seats;

import demetra.design.Development;

/**
 * @author Jean Palate 
 */
@Development(status = Development.Status.Alpha)
@lombok.Data
public final class SeatsSpec implements Cloneable {
     public static enum ApproximationMode {

        None, Legacy, Noisy
    };

    public static enum EstimationMethod {

        Burman, KalmanSmoother, McElroyMatrix
    }
    
    public static final double DEF_EPSPHI = 2, DEF_RMOD = .5, DEF_SMOD1 = .8, DEF_SMOD = .8, DEF_XL = .95;

    private ApproximationMode approximationMode = ApproximationMode.Legacy;
    private EstimationMethod method = EstimationMethod.Burman;
    private double xlBoundary=DEF_XL;
    private double seasTolerance=DEF_EPSPHI;
    private double trendBoundary=DEF_RMOD, seasBoundary=DEF_SMOD, seasBoundaryAtPi=DEF_SMOD1;
    
    private static final SeatsSpec DEFAULT=new SeatsSpec();

    public void setXlBoundary(double value) {
        if (value < 0.9 || value > 1) {
            throw new SeatsException("XL should belong to [0.9, 1]");
        }
        xlBoundary = value;
    }


    public void setSeasTolerance(double value) {
        if (value < 0 || value > 10) {
            throw new SeatsException("EPSPHI (expressed in degrees) should belong to [0, 10]");
        }
        seasTolerance = value;
    }

    public void setTrendBoundary(double value) {
        if (value < 0 || value > 1) {
            throw new SeatsException("RMOD should belong to [0, 1]");
        }
        trendBoundary = value;
    }

    public void setSeasBoundary(double value) {
        if (value < 0 || value > 1) {
            throw new SeatsException("SMOD should belong to [0, 1]");
        }
        seasBoundary = value;
    }

    public void setSeasBoundary1(double value) {
        if (value < 0 || value > 1) {
            throw new SeatsException("SMOD1 should belong to [0, 1]");
        }
        seasBoundaryAtPi = value;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public SeatsSpec clone() {
        try {
            SeatsSpec spec = (SeatsSpec) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

 }
