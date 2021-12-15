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
package demetra.x13.io.xml.legacy;

import demetra.regarima.RegressionTestSpec;
import demetra.regarima.TradingDaysSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
    "autoAdjust"
})
public class XmlDefaultTradingDaysSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlDefaultTradingDaysSpec {

    @XmlElement(name = "AutoAdjust")
    protected Boolean autoAdjust;

    public Boolean getAutoAdjust() {
        return autoAdjust;
    }

    public void setAutoAdjust(Boolean value) {
//        if (value != null && value) {
//            autoAdjust = null;
//        } else {
        this.autoAdjust = value;
//        }
    }

    public static boolean marshal(TradingDaysSpec v, XmlTradingDaysSpec xml) {
        if (xml.defaulttd != null) {
            xml.defaulttd.setCalendar(v.getHolidays());
            xml.defaulttd.setTdOption(v.getTradingDaysType());
            xml.defaulttd.setLpOption(v.getLengthOfPeriodType());
            if (v.getRegressionTestType() != RegressionTestSpec.None) {
                xml.setTest(v.getRegressionTestType());
            }
            return true;
        } else {
            return false;
        }
    }

    public static TradingDaysSpec unmarshal(XmlTradingDaysSpec xml) {
        if (xml.defaulttd == null) {
            return null;
        }
        if (xml.defaulttd.tdOption == null && xml.defaulttd.lpOption == null) {
            return TradingDaysSpec.none();
        }
        TradingDaysType td = xml.defaulttd.tdOption == null ? TradingDaysType.NONE : xml.defaulttd.getTdOption();
        LengthOfPeriodType lp = xml.defaulttd.lpOption == null ? LengthOfPeriodType.None : xml.defaulttd.getLpOption();
        RegressionTestSpec test = xml.test == null ? RegressionTestSpec.None : xml.test;
        boolean adjust = xml.defaulttd.autoAdjust == null ? false : xml.defaulttd.autoAdjust;
        if (xml.defaulttd.calendar == null) {
            return TradingDaysSpec.td(td, lp, test, adjust);
        } else {
            return TradingDaysSpec.holidays(xml.defaulttd.calendar, td, lp, test, adjust);
        }
    }
}
