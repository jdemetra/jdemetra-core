/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.timeseries.r;

import demetra.data.AggregationType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class TsUtility {

    public TsData of(int freq, int year, int start, double[] data) {
        switch (freq) {
            case 1:
                return TsData.ofInternal(TsPeriod.yearly(year), data);
            case 12:
                return TsData.ofInternal(TsPeriod.monthly(year, start), data);
            default:
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data);
        }
    }

    public TsDomain of(int freq, int year, int start, int len) {
        switch (freq) {
            case 1:
                return TsDomain.of(TsPeriod.yearly(year), len);
            case 12:
                return TsDomain.of(TsPeriod.monthly(year, start), len);
            default:
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsDomain.of(pstart, len);
        }
    }

    public TsData aggregate(TsData source, int nfreq, String conversion, boolean fullperiods) {
        AggregationType agg = AggregationType.valueOf(conversion);
        if (agg == null) {
            return null;
        }
        TsUnit unit = TsUnit.ofAnnualFrequency(nfreq);
        return source.aggregate(unit, agg, fullperiods);
    }

    /**
     * Information useful for the conversion of series in R returns [freq, year,
     * period] (period is 1-based)
     *
     * @param s
     * @return
     */
    public int[] startPeriod(TsData s) {
        return of(s.getStart());
    }

    public int[] of(TsPeriod p) {
        LocalDate start = p.start().toLocalDate();
        int freq = p.getUnit().getAnnualFrequency();
        int c = 12 / freq;
        int mon = start.getMonthValue();
        int year = start.getYear();
        return new int[]{freq, year, 1 + (mon - 1) / c};
    }

}
