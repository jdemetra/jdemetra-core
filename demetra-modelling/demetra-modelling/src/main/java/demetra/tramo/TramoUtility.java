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
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import static demetra.maths.Optimizer.LevenbergMarquardt;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaModel;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.internal.HannanRissanenInitializer;
import demetra.stats.tests.LjungBox;
import demetra.stats.tests.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class TramoUtility {
    int autlar(final int n, final SarimaSpecification spec) {
        int d = spec.getD() + spec.getPeriod() * spec.getBd();
        int q = spec.getQ() + spec.getPeriod() * spec.getBq();
        int p = spec.getP() + spec.getPeriod() * spec.getBp();
        int nd = n - d;
        int nar = (int) Math.log(nd * nd);
        int m = Math.max(p, 2 * q);
        if (m > nar) {
            nar = m;
        }
        if (nar >= nd) {
            nar = nd - nd / 4;
        }
        if (nar > 50) {
            nar = 50;
        }
        int ncol = spec.getP() + (1 + spec.getP()) * spec.getBp() + spec.getQ()
                + (1 + spec.getQ()) * spec.getBq();
        return nd - nar - Math.max(p, q) - ncol;
    }

    boolean meantest(final int n, final double t) {
        double vct = 2.5;
        if (n <= 80) {
            vct = 1.96;
        } else if (n <= 155) {
            vct = 1.98;
        } else if (n <= 230) {
            vct = 2.1;
        } else if (n <= 320) {
            vct = 2.3;
        }
        return Math.abs(t) > vct;
    }

    public int calcLBLength(final int freq) {
        int n;
        if (freq == 12) {
            n = 24;
        } else if (freq == 1) {
            n = 8;
        } else {
            n = 4 * freq;
        }
        return n;
    }

    double PLjungBox(final int freq, final double[] res,
            final int hp) {
        int n = calcLBLength(freq);

        StatisticalTest lb = new LjungBox(DoubleSequence.ofInternal(res))
                .hyperParametersCount(hp)
                .lag(n)
                .build();
        return 1 - lb.getPValue();
    }

}
