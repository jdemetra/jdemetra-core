/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.AggregationType;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDataConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
                TsPeriod pstart = TsPeriod.of(TsUnit.MONTHLY, LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data);
        }
    }

    public TsData aggregate(TsData source, int nfreq, String conversion, boolean fullperiods) {
        AggregationType agg = AggregationType.valueOf(conversion);
        if (agg == null) {
            return null;
        }
        TsUnit unit = toTsUnit(nfreq);
        if (unit == null) {
            return null;
        }
        return TsDataConverter.changeTsUnit(source, unit, agg, fullperiods);
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

        int freq = periodFromTsUnit(s.getUnit());
        int c = 12 / freq;
        int mon = start.getMonthValue();
        int year = start.getYear();
        return new int[]{freq, year, 1 + (mon - 1) / c};
    }

    public int periodFromTsUnit(TsUnit o) {
        if (o.equals(TsUnit.MONTHLY)) {
            return 12;
        }
        if (o.equals(TsUnit.QUARTERLY)) {
            return 4;
        }
        if (o.equals(TsUnit.YEARLY)) {
            return 1;
        }
        if (o.equals(TsUnit.HALF_YEARLY)) {
            return 2;
        }
        if (o.equals(TsUnit.BI_MONTHLY)) {
            return 6;
        }
        if (o.equals(TsUnit.QUADRI_MONTHLY)) {
            return 3;
        }
        return 0;
    }

    public TsUnit toTsUnit(int freq) {
        switch (freq) {
            case 12:
                return TsUnit.MONTHLY;
            case 4:
                return TsUnit.QUARTERLY;
            case 1:
                return TsUnit.YEARLY;
            case 2:
                return TsUnit.HALF_YEARLY;
            case 6:
                return TsUnit.BI_MONTHLY;
            case 3:
                return TsUnit.QUADRI_MONTHLY;
            default:
                return null;
        }
    }
}
