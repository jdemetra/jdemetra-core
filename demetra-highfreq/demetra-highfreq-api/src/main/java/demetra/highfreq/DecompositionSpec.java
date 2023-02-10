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

import demetra.timeseries.TsUnit;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class DecompositionSpec {

    public static final boolean DEF_ITERATIVE = true, DEF_NOISY = true, DEF_STDEV = false, DEF_BIAS = true;

    @lombok.NonNull
    private double[] periodicities;
    private boolean iterative;
    private boolean noisy;
    private boolean stdev;
    private int backcastsCount, forecastsCount;
    private boolean biasCorrection;
    private boolean adjustToInt;

    public static Builder builder() {
        return new Builder()
                .iterative(DEF_ITERATIVE)
                .noisy(DEF_NOISY)
                .stdev(DEF_STDEV)
                .biasCorrection(DEF_BIAS);

    }

    public static DecompositionSpec createDefault(TsUnit unit) {
        if (unit.equals(TsUnit.UNDEFINED)) {
            return null;
        }
        Builder builder = builder();
        int freq = unit.getAnnualFrequency();
        if (freq > 0) {
            return builder
                    .periodicities(new double[]{freq})
                    .adjustToInt(true)
                    .build();
        } else if (unit.equals(TsUnit.WEEK)) {
            return builder
                    .periodicities(new double[]{365.25 / 7})
                    .adjustToInt(false)
                    .build();
        } else if (unit.equals(TsUnit.DAY)) {
            return builder
                    .periodicities(new double[]{7, 365.25})
                    .adjustToInt(true)
                    .build();
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
