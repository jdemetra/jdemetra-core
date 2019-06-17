/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.benchmarking.ssf;

import jdplus.benchmarking.ssf.SsfSpline;
import jdplus.data.DataBlockStorage;
import jdplus.maths.functions.CubicSpline;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.SsfData;
import java.util.function.DoubleUnaryOperator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class SsfSplineTest {

    public SsfSplineTest() {
    }

    @Test
    public void testEquidistant() {
        double[] x = new double[]{-3, 20, -10, 5};
        double[] s = new double[25];
        for (int i = 0; i < s.length; ++i) {
            s[i] = Double.NaN;
        }
        for (int i = 0; i < x.length; ++i) {
            s[(i + 1) * 5] = x[i];
        }
        ISsf ssf = SsfSpline.of(0, 1);
        DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new SsfData(DoubleSeq.of(s)));
        DoubleSeq component = sr.item(0);

        DoubleUnaryOperator fn = CubicSpline.of(new double[]{5, 10, 15, 20}, x);

        for (int i = 0; i < 25; ++i) {
            assertEquals(component.get(i), fn.applyAsDouble(i), 1e-9);
        }
        DefaultSmoothingResults r = DkToolkit.smooth(ssf, new SsfData(DoubleSeq.of(s)), true, true);
        System.out.println(r.getComponentVariance(1));
    }

    @Test
    @Ignore
    public void stressTest() {
        double[] x = new double[]{-3, 20, -10, 5, 6, 50, -10, 8, 9, 60, 100, 50};
        double[] s = new double[(x.length + 1) * 5];
        for (int i = 0; i < s.length; ++i) {
            s[i] = Double.NaN;
        }
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; ++i) {
            s[(i + 1) * 5] = x[i];
            z[i] = (i + 1) * 5;
        }
        int K = 100000;
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            ISsf ssf = SsfSpline.of(0, 1);
            DataBlockStorage sr = DkToolkit.fastSmooth(ssf, new SsfData(DoubleSeq.of(s)));
            DoubleSeq component = sr.item(0);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            DoubleUnaryOperator fn = CubicSpline.of(z, x);

            for (int i = 0; i < s.length; ++i) {
                double q=fn.applyAsDouble(i);
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
