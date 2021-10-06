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
package jdplus.sa.diagnostics;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.stats.DescriptiveStatistics;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaDiagnosticsUtility {

    private final double E_LIMIT = .005;

    public boolean isSignificant(DoubleSeq s, DoubleSeq ref, double limit) {
        if (s.isEmpty()) {
            return false;
        }
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        DescriptiveStatistics refdesc = DescriptiveStatistics.of(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > limit;
    }

    public boolean isSignificant(DoubleSeq s, DoubleSeq ref) {
        return isSignificant(s, ref, E_LIMIT);
    }

    public boolean isSignificant(DoubleSeq s, double limit) {
        if (s == null) {
            return false;
        }
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        double se = sdesc.getStdev();
        return se > limit;
    }

    public boolean isSignificant(DoubleSeq s) {
        return isSignificant(s, E_LIMIT);
    }

    /**
     * X11 Combined seasonality test
     * @param s The tested series. Should contain at least 3 years (after de-trending)
     * @param nyears Number of years taken at the end of the series. &le 0 for the whole series
     * @param xbar (Theoretical) average of the series. Unused if detrend is true (xbar = 0)
     * @param detrend Removal of the trend (using delta(max(1, freq/4))
     * @return 
     */
    public CombinedSeasonality combinedSeasonalityTest(TsData s, int nyears, double xbar, boolean detrend) {
        int freq = s.getAnnualFrequency();
        if (detrend) {
            s = s.delta(Math.max(1, freq / 4));
        }
        int len = s.length();
        if (len < 3 * freq) // at least 3 full years (after detrending)
        {
            return null;
        }
        if (nyears > 0 && len > nyears * freq) {
            s = s.drop(len - nyears * freq, 0);
        }
        return CombinedSeasonality.of(s, xbar);
    }

}
