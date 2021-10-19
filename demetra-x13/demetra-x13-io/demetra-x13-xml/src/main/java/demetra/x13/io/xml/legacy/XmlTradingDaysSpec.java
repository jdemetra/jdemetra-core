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
import demetra.toolkit.io.xml.legacy.modelling.XmlUserTradingDaysSpec;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * Java class for TradingDaysSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TradingDaysSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Default" type="{ec/eurostat/jdemetra/sa/x13}XmlDefaultTradingDaysSpec"/&gt;
 *         &lt;element name="Stock" type="{ec/eurostat/jdemetra/sa/x13}XmlStockTradingDaysSpec"/&gt;
 *         &lt;element name="User" type="{ec/eurostat/jdemetra/modelling}XmlUserTradingDaysSpec"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TradingDaysSpecType", propOrder = {
    "defaulttd",
    "stock",
    "user",
    "test"
})
public class XmlTradingDaysSpec {

    @XmlElement(name = "Default")
    protected XmlDefaultTradingDaysSpec defaulttd;
    @XmlElement(name = "Stock")
    protected XmlStockTradingDaysSpec stock;
    @XmlElement(name = "User")
    protected XmlUserTradingDaysSpec user;
    @XmlElement(name = "Test")
    @XmlSchemaType(name = "NMTOKEN")
    @XmlJavaTypeAdapter(RegressionTestEnum.Adapter.class)
    protected RegressionTestSpec test;

    /**
     * Gets the value of the default property.
     *
     * @return possible object is {@link XmlDefaultTradingDaysSpec }
     *
     */
    public XmlDefaultTradingDaysSpec getDefault() {
        return defaulttd;
    }

    /**
     * Sets the value of the default property.
     *
     * @param value allowed object is {@link XmlDefaultTradingDaysSpec }
     *
     */
    public void setDefault(XmlDefaultTradingDaysSpec value) {
        this.defaulttd = value;
    }

    /**
     * Gets the value of the stock property.
     *
     * @return possible object is {@link XmlStockTradingDaysSpec }
     *
     */
    public XmlStockTradingDaysSpec getStock() {
        return stock;
    }

    /**
     * Sets the value of the stock property.
     *
     * @param value allowed object is {@link XmlStockTradingDaysSpec }
     *
     */
    public void setStock(XmlStockTradingDaysSpec value) {
        this.stock = value;
    }

    /**
     * Gets the value of the user property.
     *
     * @return possible object is {@link XmlUserTradingDaysSpec }
     *
     */
    public XmlUserTradingDaysSpec getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     *
     * @param value allowed object is {@link XmlUserTradingDaysSpec }
     *
     */
    public void setUser(XmlUserTradingDaysSpec value) {
        this.user = value;
    }

    /**
     * Gets the value of the test property.
     *
     * @return possible object is {@link RegressionTestEnum }
     *
     */
    public RegressionTestSpec getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value allowed object is {@link RegressionTestEnum }
     *
     */
    public void setTest(RegressionTestSpec value) {
        if (value == RegressionTestSpec.None) {
            this.test = null;
        } else {
            this.test = value;
        }
    }

    public static XmlTradingDaysSpec marshal(TradingDaysSpec v) {
        if (!v.isUsed()) {
            return null;
        }
        XmlTradingDaysSpec xml = new XmlTradingDaysSpec();
        marshal(v, xml);
        return xml;
    }

    public static boolean marshal(TradingDaysSpec v, XmlTradingDaysSpec xml) {
        if (v.isDefault()) {
            return true;
        }
        if (v.isStockTradingDays()) {
            xml.stock = new XmlStockTradingDaysSpec();
            XmlStockTradingDaysSpec.marshal(v, xml);
            return true;
        }
        String[] userVariables = v.getUserVariables();
        if (userVariables != null) {
            XmlUserTradingDaysSpec xspec = new XmlUserTradingDaysSpec();
            for (int i = 0; i < userVariables.length; ++i) {
                xspec.getVariables().add(userVariables[i]);
            }
            xml.setUser(xspec);
            return true;
        }
        xml.defaulttd = new XmlDefaultTradingDaysSpec();
        XmlDefaultTradingDaysSpec.marshal(v, xml);
        return false;
    }

    public static TradingDaysSpec unmarshal(XmlTradingDaysSpec xml) {
        if (xml.defaulttd != null) {
            return XmlDefaultTradingDaysSpec.unmarshal(xml);
        } else if (xml.stock != null) {
            return XmlStockTradingDaysSpec.unmarshal(xml);
        } else {
            List<String> variables = xml.user.getVariables();
            String[] var = new String[variables.size()];
            var = variables.toArray(var);
            return TradingDaysSpec.userDefined(var, RegressionTestSpec.None);
        }
    }
}
