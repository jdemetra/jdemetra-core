/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.modelling;

import ec.demetra.xml.core.XmlPeriodSelection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *                 Detection of outliers
 *             
 * 
 * <p>Java class for OutlierSpecType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OutlierSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Span" type="{ec/eurostat/jdemetra/core}PeriodSelectionType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutlierSpecType", propOrder = {
    "span"
})
@XmlSeeAlso({
})
public abstract class XmlOutlierSpec {

    @XmlElement(name = "Span")
    protected XmlPeriodSelection span;

    /**
     * Gets the value of the span property.
     * 
     * @return
     *     possible object is
     *     {@link PeriodSelectionType }
     *     
     */
    public XmlPeriodSelection getSpan() {
        return span;
    }

    /**
     * Sets the value of the span property.
     * 
     * @param value
     *     allowed object is
     *     {@link PeriodSelectionType }
     *     
     */
    public void setSpan(XmlPeriodSelection value) {
        this.span = value;
    }

}
