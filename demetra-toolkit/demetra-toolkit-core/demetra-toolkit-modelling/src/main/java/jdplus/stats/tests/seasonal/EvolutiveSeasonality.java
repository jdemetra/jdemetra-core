/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests.seasonal;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import jdplus.stats.tests.AnovaTest;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class EvolutiveSeasonality {

    /**
     *
     * @param series Sequence of data
     * @param period Tested periodicity
     * @param startPos Position in a cycle of the first obs.
     * @param multiplicative Multiplicative series (average = 1) or Additive series
     * (average = 0)
     * @return
     */
    public AnovaTest of(final DoubleSeq series, int period, int startPos, boolean multiplicative) {
        // determine "full cycles"

        int n = series.length();
        int nskip=0;
        if (startPos != 0){
            nskip=period-startPos;
        }
        int ny = (n-nskip) / period;
        if (ny == 0) {
            return null;
        }
        double xbar = multiplicative ? 1 : 0;

        // determine dimensions of marginal means vectors
        double[] mc = new double[period];
        double[] my = new double[ny];
        double m = 0.0;

        double[] tmp = new double[ny * period];

        DoubleSeqCursor reader = series.cursor();
        reader.skip(nskip);
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = Math.abs(reader.getAndNext() - xbar);
        }

        for (int i = 0, mm = 0; i < ny; i++) {
            for (int j = 0; j < period; j++, mm++) {
                mc[j] += tmp[mm];
                my[i] += tmp[mm];
            }
            m += my[i];
        }

        m /= period * ny;
        for (int i = 0; i < period; i++) {
            mc[i] /= ny;
        }
        for (int i = 0; i < ny; i++) {
            my[i] /= period;
        }

        double ss = 0.0, ssa = 0.0, ssb = 0.0;
        for (int i = 0, ll = 0; i < ny; i++) {
            for (int j = 0; j < period; j++, ll++) {
                ss += (tmp[ll] - m) * (tmp[ll] - m);
            }
        }

        for (int i = 0; i < ny; i++) {
            ssb += ((my[i] - m) * (my[i] - m));
        }
        ssb *= period;
        for (int i = 0; i < period; i++) {
            ssa += ((mc[i] - m) * (mc[i] - m));
        }
        ssa *= ny;

        double ssr = ss - ssa - ssb;
        if (ssr < 0) {
            ssr = 0;
        }

        return new AnovaTest(ssb, ny - 1, ssr, (ny - 1)
                * (period - 1));
    }

}
