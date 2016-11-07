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


package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.IntValue;
import ec.tstoolkit.timeseries.TsException;

/**
 * Frequency of an event. 
 * Only regular frequencies higher or equal to yearly frequency are considered.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum TsFrequency implements IntValue{
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

    /**
     * Enum correspondence to an integer
     * 
     * @param value
     *            Integer representation of the frequency
     * @return Enum representation of the frequency
     */
    public static TsFrequency valueOf(int value) {
        return IntValue.valueOf(TsFrequency.class, value).orElse(null);
    }

    private final int value;

    /**
     * Contains all the significant frequencies considered in the package
     */
    static final TsFrequency[] allFreqs = new TsFrequency[] {
	    TsFrequency.Yearly, TsFrequency.HalfYearly,
	    TsFrequency.QuadriMonthly, TsFrequency.Quarterly,
	    TsFrequency.BiMonthly, TsFrequency.Monthly };

    TsFrequency(final int value) {
	this.value = value;
    }

    /**
     * Integer representation of the frequency
     * 
     * @return The number of events by year
     */
    @Override
    public int intValue() {
	return value;
    }

    /**
     * Checks that any period of the given frequency is strictly contained in 
     * a period of this frequency 
     * @param hfreq
     * @return True if hfreq is a multiple of this frequency, 
     * false otherwise
     */
    public boolean contains(TsFrequency hfreq){
        return hfreq.value>value && hfreq.value%value == 0;
    }
    
    public int ratio(TsFrequency lfreq){
        if (value%lfreq.value != 0)
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        return value/lfreq.value;
    }
}
