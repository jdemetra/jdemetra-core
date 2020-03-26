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
 * One way ANOVA test on seasonality. The treatments are the different periods.
 * This implementation is identical to the ANOVA one (but faster)
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class StableSeasonality {

    /**
     *
     * @param series Sequence of data
     * @param period Tested periodicity
     * @return
     */
    public static AnovaTest of(DoubleSeq series, int period) {
        // compute mean
        double mm = series.average();

        // compute total SSQ
        double SSQ = 0.0;
        int n = series.length();
        DoubleSeqCursor reader = series.cursor();
        for (int i = 0; i < n; i++) {
            double cur = reader.getAndNext();
            SSQ += (cur - mm) * (cur - mm);
        }

        // compute SS of seasonality factors
        double SSM = 0;
        for (int i = 0; i < period; ++i) {
            double s = 0;
            int nc = 0;
            for (int j = i; j < n; j += period) {
                s += series.get(j);
                ++nc;
            }
            double mmj = s / nc;
            SSM += (mmj - mm) * (mmj - mm) * nc;
        }

        double SSR = SSQ - SSM;
        if (SSR < 0) {
            SSR = 0;
        }
        return new AnovaTest(SSM, period - 1, SSR,
                n - period);
    }
}
