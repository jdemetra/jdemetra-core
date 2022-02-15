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
package jdplus.tramo.internal;

import nbbrd.design.Development;
import jdplus.regarima.outlier.CriticalValueComputer;
import jdplus.regsarima.GlsSarimaComputer;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import jdplus.stats.tests.LjungBox;
import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class TramoUtility {
    
    public static final double MINCV = 2.0;
    
    public int autlar(final int n, final SarimaOrders spec) {
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

    public boolean meantest(final int n, final double t) {
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

    public double PLjungBox(final int freq, final double[] res,
            final int hp) {
        int n = LjungBox.defaultAutoCorrelationsCount(freq);

        StatisticalTest lb = new LjungBox(DoubleSeq.of(res))
                .hyperParametersCount(hp)
                .lag(n)
                .build();
        return 1 - lb.getPvalue();
    }

    public IRegArimaComputer<SarimaModel> processor(boolean ml, double precision) {
        HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                .stabilize(true)
                .useDefaultIfFailed(true)
                .build();
        return GlsSarimaComputer.builder()
                .initializer(initializer)
                .useMaximumLikelihood(ml)
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(precision)
                .build();
    }

    public double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.simpleComputer().applyAsDouble(nobs), MINCV);
    }
    
}
