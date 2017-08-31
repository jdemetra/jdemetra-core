/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntSupplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
@Deprecated
public class Fixme {

    public int getAsInt(TsFrequency freq) {
        return OldFreq.of(freq).getAsInt();
    }

    public int getId(TsPeriod p) {
        return (int) p.getOffset();
    }

    public int getId(RegularDomain p) {
        return getId(p.getStartPeriod());
    }

    public int getPosition(TsPeriod p) {
        if (!p.getOrigin().equals(TsPeriod.DEFAULT_ORIGIN)) {
            throw new UnsupportedOperationException("Unsupported origin");
        }
        OldFreq freq = OldFreq.of(p.getFreq());
        return (p.start().getMonthValue() - 1) / (12/freq.getAsInt());
    }

    public final TsFrequency Undefined = TsFrequency.of(666, ChronoUnit.MONTHS);

    public List<TsFrequency> complementOfUndefined() {
        return Arrays.asList(TsFrequency.YEARLY, TsFrequency.QUARTERLY, TsFrequency.MONTHLY, TsFrequency.HALF_YEARLY, TsFrequency.QUADRI_MONTHLY, TsFrequency.BI_MONTHLY);
    }

    public List<TsFrequency> allOf() {
        return Arrays.asList(Undefined, TsFrequency.YEARLY, TsFrequency.QUARTERLY, TsFrequency.MONTHLY, TsFrequency.HALF_YEARLY, TsFrequency.QUADRI_MONTHLY, TsFrequency.BI_MONTHLY);
    }

    public TsPeriod asPeriod(TsFrequency freq, int year, int pos) {
        LocalDate date = LocalDate.of(year, (12 / OldFreq.of(freq).getAsInt()) * pos + 1, 1);
        return TsPeriod.of(freq, date);
    }

    public enum OldFreq implements IntSupplier {
        Undefined(0, Fixme.Undefined),
        Yearly(1, TsFrequency.YEARLY),
        HalfYearly(2, TsFrequency.HALF_YEARLY),
        QuadriMonthly(3, TsFrequency.QUADRI_MONTHLY),
        Quarterly(4, TsFrequency.QUARTERLY),
        BiMonthly(6, TsFrequency.BI_MONTHLY),
        Monthly(12, TsFrequency.MONTHLY);

        final int val;
        final TsFrequency freq;

        private OldFreq(int val, TsFrequency freq) {
            this.val = val;
            this.freq = freq;
        }

        @Override
        public int getAsInt() {
            return val;
        }

        public TsFrequency convert() {
            return freq;
        }

        public static OldFreq of(TsFrequency freq) {
            switch (freq.getUnit()) {
                case YEARS:
                    if (freq.getAmount() == 1) {
                        return Yearly;
                    }
                    break;
                case MONTHS:
                    if (freq.getAmount() == 6) {
                        return HalfYearly;
                    }
                    if (freq.getAmount() == 4) {
                        return QuadriMonthly;
                    }
                    if (freq.getAmount() == 3) {
                        return Quarterly;
                    }
                    if (freq.getAmount() == 2) {
                        return BiMonthly;
                    }
                    if (freq.getAmount() == 1) {
                        return Monthly;
                    }
                    break;
            }
            throw new UnsupportedOperationException(freq.toString());
        }
    }

    /**
     * Returns the number of days for the month before or equal to the given
     * month. We consider that there are 28 days in February
     *
     * @param month 1-based index of the month
     * @return
     */
    public static int getCumulatedMonthDays(int month) {
        return CUMULATEDMONTHDAYS[month];
    }

    /**
     * Cumulative number of days (if no leap year). CumulatedMonthDays[2] =
     * number of days from 1/1 to 28/2.
     */
    private static final int[] CUMULATEDMONTHDAYS = {0, 31, 59, 90, 120, 151,
        181, 212, 243, 273, 304, 334, 365};
}
