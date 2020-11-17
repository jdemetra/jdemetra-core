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
package demetra.toolkit.io.xml.legacy.arima;

import demetra.data.Parameter;
import demetra.toolkit.io.xml.legacy.core.XmlParameters;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 *                 Description of the polynomials of a Box and Jenkins seasonal Arima model:
 *                 auto-regressive (stationary and non stationary)
 *                 and moving average regular and seasonal polynmials (+ differencing orders).
 *             
 * 
 * <p>Java class for SARIMA_PolynomialsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SARIMA_PolynomialsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/core}SARIMA_Polynomials"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SARIMA_PolynomialsType", propOrder = {
    "ar",
    "d",
    "ma",
    "bar",
    "bd",
    "bma"
})
public class XmlSarimaPolynomials {

    @XmlElement(name = "AR")
    @XmlJavaTypeAdapter(XmlParameters.Adapter.class)
    protected Parameter[] ar;
    @XmlElement(name = "D")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer d;
    @XmlElement(name = "MA")
    @XmlJavaTypeAdapter(XmlParameters.Adapter.class)
    protected Parameter[] ma;
    @XmlElement(name = "BAR")
    @XmlJavaTypeAdapter(XmlParameters.Adapter.class)
    protected Parameter[] bar;
    @XmlElement(name = "BD")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer bd;
    @XmlElement(name = "BMA")
    @XmlJavaTypeAdapter(XmlParameters.Adapter.class)
    protected Parameter[] bma;

    /**
     * Gets the value of the ar property.
     * 
     * @return
     *     possible object is
     *     {@link XmlParameters }
     *     
     */
    public Parameter[] getAR() {
        return ar;
    }

    /**
     * Sets the value of the ar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameter[] }
     *     
     */
    public void setAR(Parameter[] value) {
        this.ar = value;
    }

    /**
     * Gets the value of the d property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Integer getD() {
        return d;
    }

    /**
     * Sets the value of the d property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setD(Integer value) {
        this.d = value;
    }

    /**
     * Gets the value of the ma property.
     * 
     * @return
     *     possible object is
     *     {@link Parameter[] }
     *     
     */
    public Parameter[] getMA() {
        return ma;
    }

    /**
     * Sets the value of the ma property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameter[] }
     *     
     */
    public void setMA(Parameter[] value) {
        this.ma = value;
    }

    /**
     * Gets the value of the sar property.
     * 
     * @return
     *     possible object is
     *     {@link Parameter[] }
     *     
     */
    public Parameter[] getBAR() {
        return bar;
    }

    /**
     * Sets the value of the sar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameter[] }
     *     
     */
    public void setBAR(Parameter[] value) {
        this.bar = value;
    }

    /**
     * Gets the value of the sd property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Integer getBD() {
        return bd;
    }

    /**
     * Sets the value of the sd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBD(Integer value) {
        this.bd = value;
    }

    /**
     * Gets the value of the sma property.
     * 
     * @return
     *     possible object is
     *     {@link Parameter[] }
     *     
     */
    public Parameter[] getBMA() {
        return bma;
    }

    /**
     * Sets the value of the sma property.
     * 
     * @param value
     *     allowed object is
     *     {@link Parameter[] }
     *     
     */
    public void setBMA(Parameter[] value) {
        this.bma = value;
    }

}
