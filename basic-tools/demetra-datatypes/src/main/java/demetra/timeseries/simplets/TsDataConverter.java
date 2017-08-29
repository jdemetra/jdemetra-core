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
package demetra.timeseries.simplets;

import demetra.data.AggregationType;
import demetra.data.DoubleSequence;
import demetra.timeseries.Fixme;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsDataConverter {

    /**
     * Makes a frequency change of this series.
     *
     * @param s
     * @param newfreq The new frequency. Must be la divisor of the present
     * frequency.
     * @param conversion Aggregation mode.
     * @param complete If true, the observation for a given period in the new
     * series is set to Missing if some data in the original series are Missing.
     * @return A new time series is returned.
     */
    public TsData changeFrequency(TsData s, TsUnit newfreq, AggregationType conversion, boolean complete) {
        int ratio = s.getUnit().ratio(newfreq);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                return null;
            case 1:
                return TsData.of(s.getStart().withUnit(newfreq), s.values());
        }
        if (s.isEmpty()) {
            return TsData.of(s.getStart().withUnit(newfreq), s.values());
        }
        return complete
                ? changeComplete(s, newfreq, conversion, ratio)
                : changeIncomplete(s, newfreq, conversion, ratio);
    }

    private TsData changeComplete(TsData s, TsUnit newfreq, AggregationType conversion, int ratio) {
        return changeUsingRatio(s, newfreq, conversion, true, ratio);
    }

    private TsData changeIncomplete(TsData s, TsUnit newfreq, AggregationType conversion, int ratio) {
        return changeUsingRatio(s, newfreq, conversion, false, ratio);
    }

    private TsData changeUsingRatio(TsData s, TsUnit newfreq, AggregationType conversion, boolean complete, int ratio) {
        TsPeriod start = s.getStart();
        DoubleSequence values = s.values();
        int c = values.length();
        int z0 = 0;
        int beg = Fixme.getId(start);

        // d0 and d1
        int nbeg = beg / ratio;
        // nbeg is the first period in the new frequency
        // z0 is the number of periods in the old frequency being dropped
        int n0 = ratio, n1 = ratio;
        if (beg % ratio != 0) {
            if (complete) {
                // Attention! Different treatment if beg is negative 
                // We always have that x = x/q + x%q
                // but the integer division is rounded towards 0
                if (beg > 0) {
                    ++nbeg;
                    z0 = ratio - beg % ratio;
                } else {
                    z0 = -beg % ratio;
                }
            } else {
                if (beg < 0) {
                    --nbeg;
                }
                n0 = (nbeg + 1) * ratio - beg;
            }
        }

        int end = beg + c; // excluded
        int nend = end / ratio;

        if (end % ratio != 0) {
            if (complete) {
                if (end < 0) {
                    --nend;
                }
            } else {
                if (end > 0) {
                    ++nend;
                }
                n1 = end - (nend - 1) * ratio;
            }
        }
        int n = nend - nbeg;
        double[] result = new double[n];
        if (n > 0) {
            for (int i = 0, j = z0; i < n; ++i) {
                int nmax = ratio;
                if (i == 0) {
                    nmax = n0;
                } else if (i == n - 1) {
                    nmax = n1;
                }
                double d = 0;
                int ncur = 0;

                for (int k = 0; k < nmax; ++k, ++j) {
                    double dcur = values.get(j);
                    if (Double.isFinite(dcur)) {
                        switch (conversion) {
                            case Last:
                                d = dcur;
                                break;
                            case First:
                                if (ncur == 0) {
                                    d = dcur;
                                }
                                break;
                            case Min:
                                if ((ncur == 0) || (dcur < d)) {
                                    d = dcur;
                                }
                                break;
                            case Max:
                                if ((ncur == 0) || (dcur > d)) {
                                    d = dcur;
                                }
                                break;
                            default:
                                d += dcur;
                                break;
                        }
                        ++ncur;
                    }
                }
                if ((ncur == ratio) || (!complete && (ncur != 0))) {
                    if (conversion == AggregationType.Average) {
                        d /= ncur;
                    }
                    result[i] = d;
                }
            }
        }

        return TsData.of(TsPeriod.of(newfreq, nbeg), DoubleSequence.ofInternal(result));
    }
}
