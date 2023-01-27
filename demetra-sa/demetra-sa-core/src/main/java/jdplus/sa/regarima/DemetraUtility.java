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
package jdplus.sa.regarima;

import demetra.arima.SarimaOrders;
import nbbrd.design.Development;
import jdplus.regarima.outlier.CriticalValueComputer;
import jdplus.sarima.SarimaModel;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class DemetraUtility {

    public static final double MINCV = 2.8;

    public double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.simpleComputer().applyAsDouble(nobs), MINCV);
    }

    public IRegArimaComputer<SarimaModel> processor(boolean ml, double precision) {
        return RegSarimaComputer.builder()
                .useMaximumLikelihood(ml)
                .precision(precision)
                .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                .build();
    }

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
}
