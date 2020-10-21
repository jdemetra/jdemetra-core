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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SARIMA_OrdersType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="SARIMA_OrdersType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="P" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/&gt;
 *         &lt;element name="D" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/&gt;
 *         &lt;element name="Q" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/&gt;
 *         &lt;element name="BP" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/&gt;
 *         &lt;element name="BD" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/&gt;
 *         &lt;element name="BQ" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SARIMA_OrdersType", propOrder = {
    "p",
    "d",
    "q",
    "bp",
    "bd",
    "bq"
})
public class XmlSarimaOrders {

    @XmlElement(name = "P")
    @XmlSchemaType(name = "unsignedShort")
    protected int p;
    @XmlElement(name = "D")
    @XmlSchemaType(name = "unsignedShort")
    protected int d;
    @XmlElement(name = "Q")
    @XmlSchemaType(name = "unsignedShort")
    protected int q;
    @XmlElement(name = "BP")
    @XmlSchemaType(name = "unsignedShort")
    protected int bp;
    @XmlElement(name = "BD")
    @XmlSchemaType(name = "unsignedShort")
    protected int bd;
    @XmlElement(name = "BQ")
    @XmlSchemaType(name = "unsignedShort")
    protected int bq;

    /**
     * Gets the value of the p property.
     *
     */
    public int getP() {
        return p;
    }

    /**
     * Sets the value of the p property.
     *
     */
    public void setP(int value) {
        this.p = value;
    }

    /**
     * Gets the value of the d property.
     *
     */
    public int getD() {
        return d;
    }

    /**
     * Sets the value of the d property.
     *
     */
    public void setD(int value) {
        this.d = value;
    }

    /**
     * Gets the value of the q property.
     *
     */
    public int getQ() {
        return q;
    }

    /**
     * Sets the value of the q property.
     *
     */
    public void setQ(int value) {
        this.q = value;
    }

    /**
     * Gets the value of the bp property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public int getBP() {
        return bp;
    }

    /**
     * Sets the value of the bp property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setBP(int value) {
        bp = value;
    }

    /**
     * Gets the value of the bd property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public int getBD() {
        return bd;
    }

    /**
     * Sets the value of the bd property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setBD(int value) {
        bd = value;
    }

    /**
     * Gets the value of the bq property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public int getBQ() {
        return bq;
    }

    /**
     * Sets the value of the bq property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setBQ(int value) {
        bq = value;
    }

}
