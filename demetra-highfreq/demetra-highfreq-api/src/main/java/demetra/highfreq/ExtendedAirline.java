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

import demetra.data.DoubleSeq;
import demetra.data.Parameter;

/**
 * Description of a fractional airline model with multiple periodicities
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ExtendedAirline {

    private double[] periodicities;
    private int ndifferencing;
    private boolean ar;
    private DoubleSeq p;

    public static ExtendedAirline of(ExtendedAirlineSpec spec) {
        double[] periodicities = spec.getPeriodicities();
        int diff = spec.getDifferencingOrder();
        boolean ar = spec.getPhi() != null;
        if (diff < 0) {
            diff = periodicities.length;
            if (!ar) {
                ++diff;
            }
        }
        double[] p = new double[periodicities.length + 1];
        Parameter p0 = ar ? spec.getPhi() : spec.getTheta();
        if (p0 == null) {
            p[0] = 0;
        } else if (p0.isDefined()) {
            p[0] = p0.getValue();
        } else {
            p[0] = .2;
        }
        Parameter[] stheta = spec.getStheta();
        if (stheta != null) {
            for (int i = 0; i < stheta.length; ++i) {
                if (stheta[i] == null) {
                    p[i + 1] = 0;
                } else if (stheta[i].isDefined()) {
                    p[i + 1] = stheta[i].getValue();
                } else {
                    p[i + 1] = .2;
                }
            }
        } else {
            for (int i = 0; i < periodicities.length; ++i) {
                p[i + 1] = .2;
            }
        }
        return new ExtendedAirline(periodicities, diff, ar, DoubleSeq.of(p));
    }

}
