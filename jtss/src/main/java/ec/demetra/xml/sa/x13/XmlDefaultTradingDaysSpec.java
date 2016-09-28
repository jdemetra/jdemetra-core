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
package ec.demetra.xml.sa.x13;

import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
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
        extends ec.demetra.xml.modelling.XmlDefaultTradingDaysSpec {

    @XmlElement(name = "AutoAdjust")
    protected Boolean autoAdjust;

    public Boolean getAutoAdjust() {
        return autoAdjust;
    }

    public void setAutoAdjust(Boolean value) {
        if (value != null && value) {
            autoAdjust = null;
        } else {
            this.autoAdjust = value;
        }
    }

    public static final InPlaceXmlMarshaller<XmlDefaultTradingDaysSpec, TradingDaysSpec> MARSHALLER = (TradingDaysSpec v, XmlDefaultTradingDaysSpec xml) -> {
        xml.setCalendar(v.getHolidays());
        xml.setTdOption(v.getTradingDaysType());
        xml.setLpOption(v.getLengthOfPeriod());
        xml.setAutoAdjust(v.isAutoAdjust());
        return true;
    };

    public static final InPlaceXmlUnmarshaller<XmlDefaultTradingDaysSpec, TradingDaysSpec> UNMARSHALLER = (XmlDefaultTradingDaysSpec xml, TradingDaysSpec v) -> {
        if (xml.calendar != null) {
            v.setHolidays(xml.calendar);
        }
        if (xml.tdOption != null) {
            v.setTradingDaysType(xml.tdOption);
        }
        if (xml.lpOption != null) {
            v.setLengthOfPeriod(xml.lpOption);
        }
        if (xml.autoAdjust != null) {
            v.setAutoAdjust(xml.autoAdjust);
        }
        return true;
    };
}
