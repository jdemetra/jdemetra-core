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


package ec.tss.xml.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlCalendarSpec.NAME)
public class XmlCalendarSpec implements IXmlTramoSeatsSpec {
    static final String NAME = "calendarSpecType";

    @XmlElement
    public XmlTradingDaysSpec tradingDays;
    @XmlElement
    public XmlMovingHolidaysSpec movingHolidays;

    public static XmlCalendarSpec create(RegressionSpec spec) {
        if (spec == null)
            return null;
        XmlEaster easter = XmlEaster.create(spec.getCalendar());
        XmlTradingDaysSpec td = XmlTradingDaysSpec.create(spec.getCalendar());
        if (td == null && easter == null)
            return null;
        XmlCalendarSpec xspec = new XmlCalendarSpec();
        if (easter != null) {
            xspec.movingHolidays = new XmlMovingHolidaysSpec();
            xspec.movingHolidays.easter = easter;
        }
        xspec.tradingDays = td;
        return xspec;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        if (tradingDays != null)
            tradingDays.copyTo(spec);
        if (movingHolidays != null && movingHolidays.easter != null)
            movingHolidays.easter.copyTo(spec);
    }
}
