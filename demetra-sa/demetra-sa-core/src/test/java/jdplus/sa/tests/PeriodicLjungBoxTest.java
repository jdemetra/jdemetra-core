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
package jdplus.sa.tests;

import demetra.data.WeeklyData;
import java.util.Random;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class PeriodicLjungBoxTest {

    public PeriodicLjungBoxTest() {
    }

    @Test
    public void testWeekly() {
        PeriodicLjungBox lb = new PeriodicLjungBox(ldel(WeeklyData.US_PETROLEUM), 5);
        StatisticalTest test = lb.lags(365.25 / 7, 5).usePositiveAutocorrelations().build();
//        System.out.println(test.getValue());
//        for (int i = 40; i < 60; ++i) {
//            StatisticalTest ntest = lb.lags(i, 5).usePositiveAutocorrelations().build();
//            System.out.println(ntest.getValue());
//        }
    }

    @Test
    public void testRandom() {
        Random rnd = new Random();
        PeriodicLjungBox lb = new PeriodicLjungBox(DoubleSeq.onMapping(1000, i -> rnd.nextGaussian()), 0);
//        for (int i = 3; i < 20; ++i) {
//            StatisticalTest test = lb.lags(365.25 / i, 10).useAllAutocorrelations().build();
//            System.out.println(test.getValue());
//        }
    }

    private DoubleSeq ldel(final double[] x) {
        final double[] lx = new double[x.length];
        for (int i = 0; i < lx.length; ++i) {
            lx[i] = Math.log(x[i]);
        }
        final double[] dlx = new double[x.length - 1];
        double s = 0;
        for (int i = 0; i < dlx.length; ++i) {
            dlx[i] = lx[i + 1] - lx[i];
            s += dlx[i];
        }
        s /= dlx.length;
        for (int i = 0; i < dlx.length; ++i) {
            dlx[i] -= s;
        }
        return DoubleSeq.onMapping(dlx.length, i -> dlx[i]);
    }

    private DoubleSeq log(final double[] x) {
        final double[] lx = new double[x.length];
        int s = 0;
        for (int i = 0; i < lx.length; ++i) {
            lx[i] = Math.log(x[i]);
            s += lx[i];
        }
        s /= lx.length;
        for (int i = 0; i < lx.length; ++i) {
            lx[i] -= s;
        }
        return DoubleSeq.onMapping(lx.length, i -> lx[i]);
    }
}
