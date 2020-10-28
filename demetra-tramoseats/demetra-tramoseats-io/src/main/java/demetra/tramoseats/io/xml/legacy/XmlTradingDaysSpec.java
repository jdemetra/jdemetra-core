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

import demetra.timeseries.regression.RegressionTestType;
import demetra.toolkit.io.xml.legacy.modelling.XmlUserTradingDaysSpec;
import demetra.tramo.TradingDaysSpec;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
 *         &lt;element name="Automatic" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlAutomaticTradingDaysSpec"/&gt;
 *         &lt;element name="Default" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlDefaultTradingDaysSpec"/&gt;
 *         &lt;element name="Stock" type="{ec/eurostat/jdemetra/sa/tramoseats}XmlStockTradingDaysSpec"/&gt;
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
    "automatic",
    "defaulttd",
    "stock",
    "user"
})
public class XmlTradingDaysSpec {

    @XmlElement(name = "Automatic")
    protected XmlAutomaticTradingDaysSpec automatic;
    @XmlElement(name = "Default")
    protected XmlDefaultTradingDaysSpec defaulttd;
    @XmlElement(name = "Stock")
    protected XmlStockTradingDaysSpec stock;
    @XmlElement(name = "User")
    protected XmlUserTradingDaysSpec user;

    /**
     * Gets the value of the automatic property.
     *
     * @return possible object is {@link XmlAutomaticTradingDaysSpec }
     *
     */
    public XmlAutomaticTradingDaysSpec getAutomatic() {
        return automatic;
    }

    /**
     * Sets the value of the automatic property.
     *
     * @param value allowed object is {@link XmlAutomaticTradingDaysSpec }
     *
     */
    public void setAutomatic(XmlAutomaticTradingDaysSpec value) {
        this.automatic = value;
    }

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
    
    public static XmlTradingDaysSpec marshal(TradingDaysSpec v){
        if (! v.isUsed())
            return null;
        XmlTradingDaysSpec xml=new XmlTradingDaysSpec();
        marshal(v, xml);
        return xml;
    }

    public static boolean marshal(TradingDaysSpec v, XmlTradingDaysSpec xml) {
        if (v.isDefault()) {
            return true;
        }
        if (v.isAutomatic()) {
            XmlAutomaticTradingDaysSpec xspec = new XmlAutomaticTradingDaysSpec();
            XmlAutomaticTradingDaysSpec.marshal(v, xspec);
            xml.setAutomatic(xspec);
            return true;
        }
        if (v.isStockTradingDays()) {
            XmlStockTradingDaysSpec xspec = new XmlStockTradingDaysSpec();
            XmlStockTradingDaysSpec.marshal(v, xspec);
            xml.setStock(xspec);
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
        XmlDefaultTradingDaysSpec xdef = new XmlDefaultTradingDaysSpec();
        XmlDefaultTradingDaysSpec.marshal(v, xdef);
        xml.setDefault(xdef);

        return false;
    }

    public static TradingDaysSpec unmarshal(XmlTradingDaysSpec xml){
        if (xml.automatic != null) {
            return XmlAutomaticTradingDaysSpec.unmarshal(xml.automatic);
        } else if (xml.defaulttd != null) {
            return XmlDefaultTradingDaysSpec.unmarshal(xml.defaulttd);
        } else if (xml.stock != null) {
            return XmlStockTradingDaysSpec.unmarshal(xml.stock);
        } else {
            List<String> variables = xml.user.getVariables();
            String[] var = new String[variables.size()];
            var = variables.toArray(var);
            return TradingDaysSpec.userDefined(var, RegressionTestType.None);
        }
    }
}
