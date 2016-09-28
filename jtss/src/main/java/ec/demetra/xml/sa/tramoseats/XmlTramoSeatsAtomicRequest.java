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

import ec.demetra.xml.core.XmlTs;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for TramoSeatsAtomicRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TramoSeatsAtomicRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Series" type="{ec/eurostat/jdemetra/core}TsType"/&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/sa/tramoseats}TramoSeatsSpecificationGroup"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TramoSeatsAtomicRequestType", propOrder = {
    "series",
    "specification",
    "defaultSpecification"
})
@XmlSeeAlso({
    XmlTramoSeatsRequest.class
})
public class XmlTramoSeatsAtomicRequest {

    @XmlElement(name = "Series", required = true)
    protected XmlTs series;
    @XmlElement(name = "Specification")
    protected XmlTramoSeatsSpecification specification;
    @XmlElement(name = "DefaultSpecification")
    @XmlSchemaType(name = "NMTOKEN")
    protected String defaultSpecification;

    /**
     * Gets the value of the series property.
     * 
     * @return
     *     possible object is
     *     {@link TsType }
     *     
     */
    public XmlTs getSeries() {
        return series;
    }

    /**
     * Sets the value of the series property.
     * 
     * @param value
     *     allowed object is
     *     {@link TsType }
     *     
     */
    public void setSeries(XmlTs value) {
        this.series = value;
    }

    /**
     * Gets the value of the specification property.
     * 
     * @return
     *     possible object is
     *     {@link TramoSeatsSpecificationType }
     *     
     */
    public XmlTramoSeatsSpecification getSpecification() {
        return specification;
    }

    /**
     * Sets the value of the specification property.
     * 
     * @param value
     *     allowed object is
     *     {@link TramoSeatsSpecificationType }
     *     
     */
    public void setSpecification(XmlTramoSeatsSpecification value) {
        this.specification = value;
    }

    /**
     * Gets the value of the defaultSpecification property.
     * 
     * @return
     *     possible object is
     *     {@link TramoSeatsSpecificationEnum }
     *     
     */
    public String getDefaultSpecification() {
        return defaultSpecification;
    }

    /**
     * Sets the value of the defaultSpecification property.
     * 
     * @param value
     *     allowed object is
     *     {@link TramoSeatsSpecificationEnum }
     *     
     */
    public void setDefaultSpecification(String value) {
        this.defaultSpecification = value;
    }

}
