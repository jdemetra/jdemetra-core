/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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

package jdplus.data.interpolation;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.design.Development;
import demetra.util.IntList;

/**
 * Interpolates a series by replacing missing values by a pre-specified
 * value. This approach is used in the X13 software (in combination with
 * the "additive outlier" approach.
 * Jean Palate
 */
@Development(status = Development.Status.Release)
public class ConstInterpolator implements DataInterpolator {

    /**
     * The default value used for interpolation
     */
    public static final double BIGVALUE = 1e10;
    /**
     * The constant used in the interpolation
     */
    public final double value;

    /**
     * Create a default interpolator, using the average as interpolation value
     */
    public ConstInterpolator() {
        value = Double.NaN;
    }

    /**
     * Create an interpolator, using a given interpolation value
     *
     * @param val The interpolation value
     */
    public ConstInterpolator(double val) {
        value = val;
    }

    @Override
    public double[] interpolate(DoubleSeq data, IntList missingpos) {
        double[] ndata = data.toArray();
        double nval = value;
        if (!Double.isFinite(nval)) { 
            nval = DoublesMath.averageWithMissing(data);
        }
        for (int i = 0; i < ndata.length; ++i) {
            if (!Double.isFinite(ndata[i])) {
                missingpos.add(i);
                ndata[i] = nval;
            }
        }
        return ndata;
    }
}
