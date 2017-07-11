/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.regression;

import ec.demetra.xml.core.XmlTsDataList;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.TsVariableDescriptor.UserComponentType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for UserVariablesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserVariablesType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}ModifiableRegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variable" type="{http://www.w3.org/2001/XMLSchema}IDREFS"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="effect" use="required" type="{ec/eurostat/jdemetra/core}ComponentEnum" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserVariablesType", propOrder = {
    "data",
    "variables"
})
public abstract class XmlUserVariables
    extends XmlModifiableRegressionVariable
{

    @XmlList
    @XmlElement(name = "Variables")
    @XmlSchemaType(name = "NMTOKENS")
    protected String[] variables;
    @XmlElement(name = "Data")
    private XmlTsDataList data;
    @XmlAttribute(name = "effect", required = true)
    protected UserComponentType effect;

    public String[] getVariables() {
        return this.variables;
    }

    public void setVariables(String[] value){
        variables=value;
    }
 
    /**
     * @return the data
     */
    public XmlTsDataList getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(XmlTsDataList data) {
        this.data = data;
    }

    /**
     * Gets the value of the effect property.
     * 
     * @return
     *     possible object is
     *     {@link ComponentEnum }
     *     
     */
    public UserComponentType getEffect() {
        return effect;
    }

    /**
     * Sets the value of the effect property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComponentEnum }
     *     
     */
    public void setEffect(UserComponentType value) {
        this.effect = value;
    }

}
