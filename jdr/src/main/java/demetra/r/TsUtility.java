/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.AggregationType;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
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
                TsPeriod pstart = TsPeriod.of(TsUnit.MONTH, LocalDate.of(year, (start - 1) * c + 1, 1));
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
                TsPeriod pstart = TsPeriod.of(TsUnit.MONTH, LocalDate.of(year, (start - 1) * c + 1, 1));
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
        LocalDate start = s.getStart().start().toLocalDate();

        int freq = s.getTsUnit().ratioOf(TsUnit.YEAR);
        int c = 12 / freq;
        int mon = start.getMonthValue();
        int year = start.getYear();
        return new int[]{freq, year, 1 + (mon - 1) / c};
    }

}
