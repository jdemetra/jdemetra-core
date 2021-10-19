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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.toolkit.io.xml.legacy.core.XmlParameter;
import demetra.toolkit.io.xml.legacy.core.XmlParameters;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *                 Regression variable with its coefficient(s). The number of coefficients should correspond to the regression variable
 *             
 * 
 * <p>Java class for RegressionItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegressionItemType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variable" type="{ec/eurostat/jdemetra/core}RegressionVariableType"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="Coefficient" type="{ec/eurostat/jdemetra/core}ParameterType"/&gt;
 *           &lt;element name="Coefficients" type="{ec/eurostat/jdemetra/core}ParametersType"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegressionItemType", propOrder = {
    "variable",
    "coefficient",
    "coefficients"
})
public class XmlRegressionItem {

    @XmlElement(name = "Variable", required = true)
    protected XmlRegressionVariable variable;
    @XmlElement(name = "Coefficient")
    protected XmlParameter coefficient;
    @XmlElement(name = "Coefficients")
    protected XmlParameters coefficients;
    @XmlAttribute(name = "name")
    @XmlSchemaType(name = "NMTOKEN")
    protected String name;

    /**
     * Gets the value of the variable property.
     * 
     * @return
     *     possible object is
     *     {@link RegressionVariableType }
     *     
     */
    public XmlRegressionVariable getVariable() {
        return variable;
    }

    /**
     * Sets the value of the variable property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegressionVariableType }
     *     
     */
    public void setVariable(XmlRegressionVariable value) {
        this.variable = value;
    }

    /**
     * Gets the value of the coefficient property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterType }
     *     
     */
    public XmlParameter getCoefficient() {
        return coefficient;
    }

    /**
     * Sets the value of the coefficient property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterType }
     *     
     */
    public void setCoefficient(XmlParameter value) {
        this.coefficient = value;
    }

    /**
     * Gets the value of the coefficients property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersType }
     *     
     */
    public XmlParameters getCoefficients() {
        return coefficients;
    }

    /**
     * Sets the value of the coefficients property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersType }
     *     
     */
    public void setCoefficients(XmlParameters value) {
        this.coefficients = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
