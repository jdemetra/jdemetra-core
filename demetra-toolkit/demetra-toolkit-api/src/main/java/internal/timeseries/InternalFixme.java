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
package internal.timeseries;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import nbbrd.design.RepresentableAsInt;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
@Deprecated
public class InternalFixme {

    public int getAsInt(TsUnit freq) {
        return OldFreq.of(freq).toInt();
    }

    public int getId(TsPeriod p) {
        return (int) p.getId();
    }

    public int getPosition(TsPeriod p) {
        if (p.getEpoch()!= TsPeriod.DEFAULT_EPOCH) {
            throw new UnsupportedOperationException("Unsupported origin");
        }
        OldFreq freq = OldFreq.of(p.getUnit());
        return (p.start().getMonthValue() - 1) * freq.toInt() / 12;
    }

    @RepresentableAsInt
    @lombok.AllArgsConstructor
    private enum OldFreq {
        Undefined(0, TsUnit.UNDEFINED),
        Yearly(1, TsUnit.YEAR),
        HalfYearly(2, TsUnit.HALF_YEAR),
        Quarterly(4, TsUnit.QUARTER),
        Monthly(12, TsUnit.MONTH);

        final int annualFrequency;
        final TsUnit freq;

        public int toInt() {
            return annualFrequency;
        }

        public static @NonNull OldFreq parse(int value) throws IllegalArgumentException {
            for (OldFreq o : values()) {
                if (o.annualFrequency == value) {
                    return o;
                }
            }
            throw new IllegalArgumentException("Cannot parse " + value);
        }

        public static OldFreq of(TsUnit freq) {
            switch (freq.getChronoUnit()) {
                case YEARS:
                    if (freq.getAmount() == 1) {
                        return Yearly;
                    }
                    break;
                case MONTHS:
                    if (freq.getAmount() == 6) {
                        return HalfYearly;
                    }
                    if (freq.getAmount() == 3) {
                        return Quarterly;
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
