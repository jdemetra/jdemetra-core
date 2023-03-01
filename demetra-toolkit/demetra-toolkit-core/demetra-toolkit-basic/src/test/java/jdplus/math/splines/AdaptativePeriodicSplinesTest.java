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
package jdplus.math.splines;

import demetra.data.DoubleSeq;
import demetra.data.WeeklyData;
import jdplus.data.DataBlock;
import jdplus.data.analysis.DiscreteKernel;
import jdplus.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.math.linearfilters.FilterUtility;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.linearfilters.LocalPolynomialFilters;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author palatej
 */
public class AdaptativePeriodicSplinesTest {

    public AdaptativePeriodicSplinesTest() {
    }

    public static void main(String[] arg) {

        double[] y = WeeklyData.US_CLAIMS;

        int q = 50;
        double[] knots = new double[q];
        double P = 365.25 / 7;
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int nyears = 20;
        int ny = (int) (nyears * P);
        int jump = 4;
        int nq = q / jump;
        int[] fixedKnots = new int[nq];
        for (int i = 0; i < nq; ++i) {
            fixedKnots[i] = i * jump;
        }

        DoubleSeq m = DoubleSeq.onMapping(ny, i -> i * c - P * (int) ((i * c) / P));
        SymmetricFilter sf = LocalPolynomialFilters.of(26, 1, DiscreteKernel.uniform(26));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y).log(), sf, lfilters, afilters);

        DataBlock Y = DataBlock.make(ny);
        Y.set(i -> Math.log(y[i]) - t.get(i));

        long l0 = System.currentTimeMillis();
        int min = 12;
        for (double lambda = 0; lambda < 1; lambda += 0.0001) {
            AdaptativePeriodicSplines.Specification spec = AdaptativePeriodicSplines.Specification.builder()
                    .x(m)
                    .y(Y)
                    .period(P)
                    .knots(knots)
                    .fixedKnots(null)
                    .build();

            AdaptativePeriodicSplines kernel = AdaptativePeriodicSplines.of(spec);
            kernel.process(lambda);
            System.out.print(lambda);
            System.out.print('\t');
            System.out.print(kernel.aic());
            System.out.print('\t');
            System.out.print(kernel.bic());
            System.out.print('\t');
            System.out.print(kernel.selectedKnots().length);
            System.out.print('\t');
            System.out.println(kernel.z());
            if (kernel.selectedKnots().length < min) {
                break;
            }
        }

        long l1 = System.currentTimeMillis();
        System.out.println(l1 - l0);

    }
}
