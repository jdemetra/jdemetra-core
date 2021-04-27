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
package jdplus.modelling;

import nbbrd.design.Development;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class RangeMeanTest {

    private int groupLength, trim;
//    private int groupLength, trim;

    public RangeMeanTest groupSize(int n) {
        groupLength = n;
        return this;
    }

    public RangeMeanTest trim(int n) {
        trim = n;
        return this;
    }

    public LeastSquaresResults process(DoubleSeq data) {
        if (data.anyMatch(x -> x <= 0)) {
            return null;
        }
        if (groupLength == 0) {
            throw new IllegalArgumentException();
        }
        return compute(data);
    }

    /**
     *
     */
    public RangeMeanTest() {
    }

    /**
     *
     * @param period
     * @param n
     * @return
     */
    public static int computeDefaultGroupSize(int period, int n) {
        switch (period) {
            case 12:
            case 6:
                return 12;
            case 4:
                return (n > 165) ? 8 : 12;
            case 3:
                return (n > 165) ? 6 : 12;
            case 2:
                return (n > 165) ? 6 : 12;
            case 1:
                return (n > 165) ? 5 : 9;
            default:
                return period;
        }
    }

    /**
     *
     * @param data
     * @return
     */
    public LeastSquaresResults compute(DoubleSeq data) {
        int n = data.length();
        groupLength = 0;
        trim = 0;
        int npoints = n / groupLength;
        if (npoints <= 3) {
            if (groupLength == 0) {
                throw new IllegalArgumentException("Not enough data");
            }
        }
        double[] range = new double[npoints], smean = new double[npoints], srt = new double[groupLength];

        for (int i = 0; i < npoints; ++i) {
            // fill srt;
            for (int j = 0; j < groupLength; ++j) {
                srt[j] = data.get(j + i * groupLength);
            }
            Arrays.sort(srt);
            range[i] = srt[groupLength - trim - 1] - srt[trim];
            double s = srt[trim];
            for (int j = trim + 1; j < groupLength - trim; ++j) {
                s += srt[j];
            }
            s /= (groupLength - 2 * trim);
            smean[i] = s;
        }
        try {
            LinearModel model = LinearModel.builder()
                    .y(DoubleSeq.of(range))
                    .addX(DoubleSeq.of(smean))
                    .meanCorrection(true)
                    .build();
            return Ols.compute(model);
        } catch (Exception err) {
            return null;
        }
    }

}
