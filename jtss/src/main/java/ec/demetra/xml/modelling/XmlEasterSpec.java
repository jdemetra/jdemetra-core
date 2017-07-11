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
package ec.demetra.xml.modelling;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EasterSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EasterSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Duration" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedShort"&gt;
 *               &lt;minInclusive value="1"/&gt;
 *               &lt;maxInclusive value="15"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="julian" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EasterSpecType", propOrder = {
    "duration"
})
@XmlSeeAlso({})
public class XmlEasterSpec {

    @XmlElement(name = "Duration")
    protected Integer duration;
    @XmlAttribute(name = "julian")
    protected Boolean julian;

    /**
     * Gets the value of the duration property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setDuration(Integer value) {
        this.duration = value;
    }

    /**
     * Gets the value of the julian property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public boolean isJulian() {
        if (julian == null) {
            return false;
        } else {
            return julian;
        }
    }

    /**
     * Sets the value of the julian property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setJulian(Boolean value) {
        if (value != null && !value) {
            this.julian = null;
        } else {
            this.julian = value;
        }
    }

}
