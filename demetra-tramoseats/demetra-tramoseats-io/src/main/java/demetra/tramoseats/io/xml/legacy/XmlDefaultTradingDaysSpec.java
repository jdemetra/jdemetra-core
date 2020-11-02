/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.tramoseats.io.xml.legacy;

import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.tramo.RegressionTestType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.tramo.TradingDaysSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for DefaultTradingDaysSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="DefaultTradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}DefaultTradingDaysSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Test" type="{ec/eurostat/jdemetra/sa/tramoseats}TradingDaysTestEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefaultTradingDaysSpecType", propOrder = {
    "test"
})
public class XmlDefaultTradingDaysSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlDefaultTradingDaysSpec {

    @XmlElement(name = "Test")
    @XmlSchemaType(name = "NMTOKEN")
    @XmlJavaTypeAdapter(TradingDaysTestEnum.Adapter.class)
    protected RegressionTestType test;

    /**
     * Gets the value of the test property.
     *
     * @return possible object is {@link TradingDaysTestEnum }
     *
     */
    public RegressionTestType getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value allowed object is {@link TradingDaysTestEnum }
     *
     */
    public void setTest(RegressionTestType value) {
        this.test = value;
    }

    public static boolean marshal(TradingDaysSpec v, XmlDefaultTradingDaysSpec xml) {
        xml.setCalendar(v.getHolidays());
        xml.setTdOption(v.getTradingDaysType());
        xml.setLpOption(v.getLengthOfPeriodType());
        if (v.isTest()) {
            xml.setTest(v.getRegressionTestType());
        }
        return true;
    }

    public static TradingDaysSpec unmarshal(XmlDefaultTradingDaysSpec xml) {
        if (xml.tdOption == null && xml.lpOption == null) {
            return TradingDaysSpec.none();
        }
        TradingDaysType td = xml.tdOption == null ? TradingDaysType.None : xml.tdOption;
        LengthOfPeriodType lp = xml.lpOption == null ? LengthOfPeriodType.None : xml.lpOption;
        RegressionTestType test = xml.test == null ? RegressionTestType.None : xml.test;
        if (xml.calendar == null) {
            return TradingDaysSpec.td(td, lp, test);
        } else {
            return TradingDaysSpec.holidays(xml.calendar, td, lp, test);
        }
    }
}
