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
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlTradingDaysSpec.NAME)
public class XmlTradingDaysSpec implements IXmlTramoSeatsSpec {

    static final String NAME = "tradingDaysSpecType";
    @XmlElement
    public String calendar;
    @XmlElement(name = "string")
    @XmlElementWrapper(name = "userVariables")
    public String[] userVariables;
    @XmlElement
    public TradingDaysType tdOption = TradingDaysType.None;

    public boolean isTdOptionSpecified() {
        return tdOption != null;
    }
    @XmlElement
    public LengthOfPeriodType lpOption = LengthOfPeriodType.None;

    public boolean isLpOptionSpecified() {
        return lpOption != null;
    }
    @XmlAttribute
    public Boolean pretest;

    public boolean isPretestSpecified() {
        return pretest != null;
    }
    @XmlAttribute
    public RegressionTestType testType;

    public static XmlTradingDaysSpec create(CalendarSpec cspec) {
        if (cspec == null || !cspec.getTradingDays().isUsed()) {
            return null;
        }
        TradingDaysSpec td = cspec.getTradingDays();
        XmlTradingDaysSpec xspec = new XmlTradingDaysSpec();
        xspec.tdOption = td.getTradingDaysType();
        xspec.lpOption = td.isLeapYear() ? LengthOfPeriodType.LeapYear : LengthOfPeriodType.None;
        xspec.testType = td.getRegressionTestType();
        xspec.calendar = cspec.getTradingDays().getHolidays();
        xspec.userVariables = cspec.getTradingDays().getUserVariables();
        return xspec;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        RegressionSpec reg = spec.getTramoSpecification().getRegression();
        CalendarSpec cspec = reg.getCalendar();
        TradingDaysSpec tdspec = cspec.getTradingDays();
        if (isTdOptionSpecified()) {
            tdspec.setTradingDaysType(tdOption);
        }
        if (isLpOptionSpecified()) {
            tdspec.setLeapYear(lpOption != null);
        }
        if (isPretestSpecified()) {
            tdspec.setTest(pretest);
        } else if (testType != null) {
            tdspec.setRegressionTestType(testType);
        }
        if (calendar != null) {
            tdspec.setHolidays(calendar);
        }
        if (userVariables != null) {
            tdspec.setUserVariables(userVariables);
        }
    }
}
