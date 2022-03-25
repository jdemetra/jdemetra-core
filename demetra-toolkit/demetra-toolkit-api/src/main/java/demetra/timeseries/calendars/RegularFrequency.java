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
package demetra.timeseries.calendars;

import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import nbbrd.design.Development;
import nbbrd.design.RepresentableAs;
import nbbrd.design.RepresentableAsInt;

import java.time.temporal.ChronoUnit;

/**
 * Frequency of an event.
 * Only regular frequencies higher or equal to yearly frequency are considered.
 *
 * @author Jean Palate
 */
@RepresentableAsInt
@RepresentableAs(value = TsUnit.class, parseMethodName = "parseTsUnit")
@Development(status = Development.Status.Release)
public enum RegularFrequency {
    /**
     * Undefined frequency. To be used when the frequency of an event is
     * unknown.
     */
    Undefined(0),
    /**
     * One event by year
     */
    Yearly(1),
    /*
     * One event every half-year
     */
    /**
     *
     */
    HalfYearly(2),
    /*
     * One event every four months
     */
    /**
     *
     */
    QuadriMonthly(3),
    /*
     * One event every quarter
     */
    /**
     *
     */
    Quarterly(4),
    /*
     * One event every two months
     */
    /**
     *
     */
    BiMonthly(6),
    /*
     * One event every month
     */
    /**
     *
     */
    Monthly(12);

    private static final RegularFrequency[] ENUMS = RegularFrequency.values();

    /**
     * Enum correspondence to an integer
     *
     * @param value
     * Integer representation of the frequency
     * @return Enum representation of the frequency
     */
    public static RegularFrequency parse(int value) throws IllegalArgumentException{
        if (value <= 0)
            return Undefined;
        if (12 % value == 0) {
            for (int i = 0; i < ENUMS.length; ++i) {
                if (value == ENUMS[i].value) {
                    return ENUMS[i];
                }
            }
        }
        throw new IllegalArgumentException("Cannot parse " + value);
    }

    private final int value;

    /**
     * Contains all the significant frequencies considered in the package
     * @return 
     */
    public static final RegularFrequency[] all()
    {return ENUMS.clone();}

    RegularFrequency(final int value) {
        this.value = value;
    }

    /**
     * Integer representation of the frequency
     *
     * @return The number of events by year
     */
    public int toInt() {
        return value;
    }

    /**
     * Checks that any period of the given frequency is strictly contained in
     * a period of this frequency
     *
     * @param hfreq
     * @return True if hfreq is a multiple of this frequency,
     * false otherwise
     */
    public boolean contains(RegularFrequency hfreq) {
        return hfreq.value > value && hfreq.value % value == 0;
    }

    public int ratio(RegularFrequency lfreq) {
        if (value % lfreq.value != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        return value / lfreq.value;
    }

    public TsUnit toTsUnit() {
        switch (this) {
            case Yearly:
                return TsUnit.YEAR;
            case HalfYearly:
                return TsUnit.HALF_YEAR;
            case QuadriMonthly:
                return TsUnit.of(4, ChronoUnit.MONTHS);
            case Quarterly:
                return TsUnit.QUARTER;
            case BiMonthly:
                return TsUnit.of(2, ChronoUnit.MONTHS);
            case Monthly:
                return TsUnit.MONTH;
            case Undefined:
                return TsUnit.UNDEFINED;
        }
        throw new RuntimeException("Unreachable");
    }

    public static RegularFrequency parseTsUnit(TsUnit unit) throws IllegalArgumentException {
        if (unit.equals(TsUnit.UNDEFINED)) {
            return RegularFrequency.Undefined;
        }
        switch (unit.getChronoUnit()) {
            case YEARS:
                if (unit.getAmount() == 1) {
                    return Yearly;
                }
                break;
            case MONTHS:
                if (unit.getAmount() == 6) {
                    return HalfYearly;
                }
                if (unit.getAmount() == 4) {
                    return QuadriMonthly;
                }
                if (unit.getAmount() == 3) {
                    return Quarterly;
                }
                if (unit.getAmount() == 2) {
                    return BiMonthly;
                }
                if (unit.getAmount() == 1) {
                    return Monthly;
                }
                break;
        }
        throw new IllegalArgumentException("Unsupported unit " + unit);
    }
}
