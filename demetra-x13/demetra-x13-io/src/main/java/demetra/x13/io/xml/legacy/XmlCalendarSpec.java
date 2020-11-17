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

import demetra.regarima.RegressionSpec;
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
        extends demetra.toolkit.io.xml.legacy.modelling.XmlCalendarSpec {

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

    public static XmlCalendarSpec marshal(RegressionSpec v) {
        if (v.getEaster().isUsed() || v.getTradingDays().isUsed()) {
            XmlCalendarSpec xcal = new XmlCalendarSpec();
            marshal(v, xcal);
            return xcal;
        } else {
            return null;
        }
    }

    public static boolean marshal(RegressionSpec v, XmlCalendarSpec xml) {
        if (!v.isUsed()) {
            return true;
        }

        if (v.getTradingDays().isUsed()) {
            XmlTradingDaysSpec xtd = new XmlTradingDaysSpec();
            XmlTradingDaysSpec.marshal(v.getTradingDays(), xtd);
            xml.tradingDays = xtd;
        }
        if (v.getEaster().isUsed()) {
            XmlEasterSpec xe = new XmlEasterSpec();
            XmlEasterSpec.marshal(v.getEaster(), xe);
            xml.easter = xe;
        }
        return true;
    }

    public static RegressionSpec.Builder unmarshal(XmlCalendarSpec xml, RegressionSpec.Builder builder) {
        if (xml.tradingDays != null) {
            builder = builder.tradingDays(XmlTradingDaysSpec.unmarshal(xml.tradingDays));
        }
        if (xml.easter != null) {
            builder = builder.easter(XmlEasterSpec.unmarshal(xml.easter));
        }
        return builder;
    }
}
