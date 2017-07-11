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
package ec.demetra.xml.sa.tramoseats;

import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for StockTradingDaysSpecType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StockTradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}StockTradingDaysSpecType"&gt;
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
@XmlType(name = "StockTradingDaysSpecType", propOrder = {
    "test"
})
public class XmlStockTradingDaysSpec
    extends ec.demetra.xml.modelling.XmlStockTradingDaysSpec
{

    @XmlElement(name = "Test")
    @XmlSchemaType(name = "NMTOKEN")
    protected RegressionTestType test;

    /**
     * Gets the value of the test property.
     * 
     * @return
     *     possible object is
     *     {@link TradingDaysTestEnum }
     *     
     */
    public RegressionTestType getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     * 
     * @param value
     *     allowed object is
     *     {@link TradingDaysTestEnum }
     *     
     */
    public void setTest(RegressionTestType value) {
        this.test = value;
    }

    public static final InPlaceXmlMarshaller<XmlStockTradingDaysSpec, TradingDaysSpec> MARSHALLER = (TradingDaysSpec v, XmlStockTradingDaysSpec xml) -> {
        xml.setCalendar(v.getHolidays());
        if (v.isTest())
            xml.setTest(v.getRegressionTestType());
        xml.w=v.getStockTradingDays();
        return true;
    };
    
    public static final InPlaceXmlUnmarshaller<XmlStockTradingDaysSpec, TradingDaysSpec> UNMARSHALLER = (XmlStockTradingDaysSpec xml, TradingDaysSpec v) -> {
        if (xml.calendar != null) {
            v.setHolidays(xml.calendar);
        }
        if (xml.test != null)
            v.setRegressionTestType(xml.test);
        v.setStockTradingDays(xml.w);
        return true;
    };
}
