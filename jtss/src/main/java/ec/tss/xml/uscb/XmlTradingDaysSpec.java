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


package ec.tss.xml.uscb;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlTradingDaysSpec.NAME)
public class XmlTradingDaysSpec extends AbstractXmlTdVariables implements IXmlConverter<TradingDaysSpec> {
    static final String NAME = "tdVariableSpecType";

    public static enum XmlTradingDaysOption
    {
        None, Td, TdNoLpYear, Td1Coef, Td1NoLpYear;
        
    };

    public static enum XmlLengthOfPeriodOption
    {
        None, LpYear, LengthofPeriod
    };

    @XmlElement
    public XmlTradingDaysOption tdoption = XmlTradingDaysOption.None;
    public boolean isTdoptionSpecified() {
        return tdoption != XmlTradingDaysOption.None;
    }
    @XmlElement
    public XmlLengthOfPeriodOption lpoption = XmlLengthOfPeriodOption.None;
    public boolean isLpoptionSpecified() {
        return lpoption != XmlLengthOfPeriodOption.None;
    }
    @XmlElement
    public RegressionTestSpec aicTest = RegressionTestSpec.None;
    public boolean isAicTestSpecified() {
        return aicTest != RegressionTestSpec.None;
    }
    @XmlElement
    public XmlChangeOfRegimeSpec changeOfRegime;
    @XmlElement
    public String holidays;
    @XmlElement(name = "string")
    @XmlElementWrapper(name = "userVariables")
    public String[] userVariables;

    @Override
    public void copyTo(RegressionSpec spec) {
        spec.setTradingDays(create());
    }

    @Override
    public TradingDaysSpec create() {
        TradingDaysSpec spec = new TradingDaysSpec();
        spec.setTest(aicTest);
        spec.setHolidays(holidays);
        spec.setUserVariables(userVariables);
        switch (tdoption){
            case Td:
                spec.setTradingDaysType(TradingDaysType.TradingDays);
                spec.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
                break;
            case Td1Coef:
                spec.setTradingDaysType(TradingDaysType.WorkingDays);
                spec.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
                break;
            case TdNoLpYear:
                spec.setAutoAdjust(false);
                spec.setTradingDaysType(TradingDaysType.TradingDays);
                break;
            case Td1NoLpYear:
                spec.setAutoAdjust(false);
                spec.setTradingDaysType(TradingDaysType.WorkingDays);
                break;
           }
        if (! spec.isAutoAdjust())
        switch (lpoption){
            case LpYear:
                spec.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
                break;
            case LengthofPeriod:
                spec.setLengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);
                break;
        }
      return spec;
    }

    @Override
    public void copy(TradingDaysSpec t) {
        aicTest = t.getTest();
        switch (t.getTradingDaysType()){
            case TradingDays:
                if (t.isAutoAdjust() && t.getLengthOfPeriod() != LengthOfPeriodType.None)
                    tdoption=XmlTradingDaysOption.Td;
                else 
                    tdoption=XmlTradingDaysOption.TdNoLpYear;
                break;
            case WorkingDays:
                if (t.isAutoAdjust() && t.getLengthOfPeriod() != LengthOfPeriodType.None)
                    tdoption=XmlTradingDaysOption.Td1Coef;
                else 
                    tdoption=XmlTradingDaysOption.Td1NoLpYear;
                break;
        }
        if (! t.isAutoAdjust()){
            switch (t.getLengthOfPeriod()){
                case LeapYear:
                    lpoption = XmlLengthOfPeriodOption.LpYear;
                    break;
                case LengthOfPeriod:
                    lpoption = XmlLengthOfPeriodOption.LengthofPeriod;
                    break;
            }
        }
        holidays = t.getHolidays();
        userVariables = t.getUserVariables();
    }
}
