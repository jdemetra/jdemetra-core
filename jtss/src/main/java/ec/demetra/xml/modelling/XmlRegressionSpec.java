/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.modelling;

import ec.demetra.xml.regression.XmlRegression;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *                 Regression variables
 *             
 * 
 * <p>Java class for RegressionSpecType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegressionSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variables" type="{ec/eurostat/jdemetra/core}XmlRegression"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegressionSpecType", propOrder = {
    "variables"
})
@XmlSeeAlso({
})
public abstract class XmlRegressionSpec {

    @XmlElement(name = "Variables", required = true)
    protected XmlRegression variables;

    /**
     * Gets the value of the variables property.
     * 
     * @return
     *     possible object is
     *     {@link XmlRegression }
     *     
     */
    public XmlRegression getVariables() {
        return variables;
    }

    /**
     * Sets the value of the variables property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlRegression }
     *     
     */

    public void setVariables(XmlRegression value) {
        this.variables = value;
    }

}
