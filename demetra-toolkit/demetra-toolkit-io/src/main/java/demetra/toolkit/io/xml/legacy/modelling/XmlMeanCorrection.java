/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.toolkit.io.xml.legacy.modelling;

import demetra.toolkit.io.xml.legacy.XmlEmptyElement;
import demetra.toolkit.io.xml.legacy.core.XmlParameter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for MeanCorrectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MeanCorrectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="Value" type="{ec/eurostat/jdemetra/core}ParameterType"/&gt;
 *           &lt;element name="Test" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeanCorrectionType", propOrder = {
    "value",
    "test"
})
public class XmlMeanCorrection {

    @XmlElement(name = "Value")
    protected XmlParameter value;
    @XmlElement(name = "Test")
    protected XmlEmptyElement test;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterType }
     *     
     */
    public XmlParameter getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterType }
     *     
     */
    public void setValue(XmlParameter value) {
        this.value = value;
    }

    /**
     * Gets the value of the test property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public XmlEmptyElement getTest() {
        return test;
    }

    /**
     * Sets the value of the test property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setTest(XmlEmptyElement value) {
        this.test = value;
    }

}
