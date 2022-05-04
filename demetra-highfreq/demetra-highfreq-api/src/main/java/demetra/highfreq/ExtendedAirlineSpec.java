/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.highfreq;

import demetra.data.Parameter;
import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ExtendedAirlineSpec {

    private boolean mean;
    // Periodic airline model
    private double[] periodicities;
    private int differencingOrder;
    private Parameter phi, theta;
    private Parameter[] stheta;
    private boolean adjustToInt;

    public boolean isValid() {
        if (phi != null && theta != null) {
            return false;
        }
        if (phi == null && theta == null) {
            return false;
        }
        if (stheta.length != periodicities.length) {
            return false;
        }
        int maxdiff = periodicities.length;
        if (theta != null) {
            ++maxdiff;
        }
        return differencingOrder <= maxdiff;
    }

    public boolean hasAr() {
        return phi != null;
    }

    public static final double[] NO_PERIOD = new double[0];

    public static Builder builder() {
        return new Builder()
                .differencingOrder(-1)
                .periodicities(NO_PERIOD);
    }

    public static final ExtendedAirlineSpec DEFAULT_Y = builder()
            .periodicities(new double[]{7, 365.25})
            .differencingOrder(3)
            .phi(null)
            .theta(Parameter.undefined())
            .stheta(Parameter.make(2))
            .adjustToInt(true)
            .build();

    public static final ExtendedAirlineSpec DEFAULT_FY = builder()
            .periodicities(new double[]{7, 365.25})
            .differencingOrder(3)
            .phi(null)
            .theta(Parameter.undefined())
            .stheta(Parameter.make(2))
            .adjustToInt(false)
            .build();

    public static final ExtendedAirlineSpec DEFAULT_W = builder()
            .periodicities(new double[]{365.25 / 7})
            .differencingOrder(2)
            .phi(null)
            .theta(Parameter.undefined())
            .stheta(Parameter.make(1))
            .adjustToInt(true)
            .build();

    public static final ExtendedAirlineSpec DEFAULT_WD = builder()
            .periodicities(new double[]{7})
            .differencingOrder(2)
            .phi(null)
            .theta(Parameter.undefined())
            .stheta(Parameter.make(1))
            .adjustToInt(true)
            .build();

    public static final ExtendedAirlineSpec DEFAULT_FW = builder()
            .periodicities(new double[]{365.25 / 7})
            .differencingOrder(3)
            .phi(null)
            .theta(Parameter.undefined())
            .stheta(Parameter.make(1))
            .adjustToInt(false)
            .build();

    public int freeParametersCount() {
        int np=0;
        if (phi != null && phi.isFree())
            ++np;
        if (theta != null && theta.isFree())
            ++np;
        np+=Parameter.freeParametersCount(stheta);
        return np;
    }

}
