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

import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CalendarSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="CalendarSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}CalendarSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TradingDays" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlTradingDaysSpec" minOccurs="0"/&gt;
 *         &lt;element name="Easter" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlEasterSpec" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CalendarSpecType", propOrder = {
    "tradingDays",
    "easter"
})
public class XmlCalendarSpec
        extends ec.demetra.xml.modelling.XmlCalendarSpec {

    @XmlElement(name = "TradingDays")
    protected XmlTradingDaysSpec tradingDays;
    @XmlElement(name = "Easter")
    protected XmlEasterSpec easter;

    /**
     * Gets the value of the tradingDays property.
     *
     * @return possible object is {@link XmlTradingDaysSpec }
     *
     */
    public XmlTradingDaysSpec getTradingDays() {
        return tradingDays;
    }

    /**
     * Sets the value of the tradingDays property.
     *
     * @param value allowed object is {@link XmlTradingDaysSpec }
     *
     */
    public void setTradingDays(XmlTradingDaysSpec value) {
        this.tradingDays = value;
    }

    /**
     * Gets the value of the easter property.
     *
     * @return possible object is {@link XmlEasterSpec }
     *
     */
    public XmlEasterSpec getEaster() {
        return easter;
    }

    /**
     * Sets the value of the easter property.
     *
     * @param value allowed object is {@link XmlEasterSpec }
     *
     */
    public void setEaster(XmlEasterSpec value) {
        this.easter = value;
    }

    public static final IXmlMarshaller<XmlCalendarSpec, CalendarSpec> MARSHALLER = (CalendarSpec v) -> {
        if (!v.isUsed()) {
            return null;
        }

        XmlCalendarSpec xml = new XmlCalendarSpec();
        if (v.getTradingDays().isUsed()) {
            XmlTradingDaysSpec xtd = new XmlTradingDaysSpec();
            XmlTradingDaysSpec.MARSHALLER.marshal(v.getTradingDays(), xtd);
            xml.tradingDays = xtd;
        }
        if (v.getEaster().isUsed()) {
            XmlEasterSpec xe = new XmlEasterSpec();
            XmlEasterSpec.MARSHALLER.marshal(v.getEaster(), xe);
            xml.easter = xe;
        }
        return xml;
    };

    public static final InPlaceXmlUnmarshaller<XmlCalendarSpec, CalendarSpec> UNMARSHALLER = (XmlCalendarSpec xml, CalendarSpec v) -> {
        if (xml.tradingDays != null){
            XmlTradingDaysSpec.UNMARSHALLER.unmarshal(xml.tradingDays, v.getTradingDays());
        }
        if (xml.easter != null){
            XmlEasterSpec.UNMARSHALLER.unmarshal(xml.easter, v.getEaster());
        }
        return true;
    };
}
