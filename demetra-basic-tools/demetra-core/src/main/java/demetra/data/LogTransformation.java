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
package demetra.data;

import demetra.design.Development;
import demetra.data.transformation.DataTransformation;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LogTransformation implements DataTransformation {

    /**
     *
     * @param data
     * @return
     */
    public boolean canTransform(DoubleSequence data) {
        return data.allMatch(x -> Double.isFinite(x) ? x > 0 : true);
    }

    /**
     *
     * @return
     */
    @Override
    public DataTransformation converse() {
        return new ExpTransformation();
    }

    /**
     *
     * @param data
     * @param ljacobian
     * @return
     */
    @Override
    public DoubleSequence transform(DoubleSequence data, LogJacobian ljacobian) {
        double[] x = data.toArray();
        for (int i = 0; i < x.length; ++i) {
            if (Double.isFinite(x[i])) {
                x[i] = Math.log(x[i]);
            }
        }
        if (ljacobian != null) {
            double s = 0;
            for (int i = ljacobian.start; i < ljacobian.end; ++i) {
                if (Double.isFinite(x[i])) {
                    s += x[i];
                }
            }
            ljacobian.value -= s;
        }
        return DoubleSequence.ofInternal(x);
    }
}