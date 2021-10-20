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
package demetra.toolkit.io.xml.legacy.core;

import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.time.LocalDate;
import java.time.Month;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlTsPeriod.NAME)
public class XmlTsPeriod implements IXmlConverter<TsPeriod> {

    static final String NAME = "TsPeriodType";
    /**
     *
     */
    @XmlElement(name = "Frequency")
    public int freq;
    /**
     *
     */
    @XmlElement(name = "Year")
    public int year;
    /**
     *
     */
    @XmlElement(name = "Period")
    public Integer period;

    /**
     *
     * @param t
     */
    @Override
    public void copy(TsPeriod t) {
        freq = t.getUnit().getAnnualFrequency();
        if (freq != 1) {
            period = t.annualPosition() + 1;
        } else {
            period = null;
        }
        year = t.year();
    }

    /**
     *
     * @return
     */
    @Override
    public TsPeriod create() {
        return of(freq, year, period);
    }

    public static TsPeriod of(int f, int y, Integer P) {
        int p = P == null ? 1 : P;
        switch (f) {
            case 1:
                return TsPeriod.yearly(y);
            case 4:
                return TsPeriod.quarterly(y, p);
            case 12:
                return TsPeriod.monthly(y, p);
            default:
                return TsPeriod.of(TsUnit.ofAnnualFrequency(f), LocalDate.of(y, p * 12 / f, 1));
        }

    }
}
