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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Base type for any modifiable regression variable. By design we can decide that some variables
 *                 should not be modifiable (for instance outliers)
 *             
 * 
 * <p>Java class for ModifiableRegressionVariableType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModifiableRegressionVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}RegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Modifier" type="{ec/eurostat/jdemetra/core}RegressionVariableModifierType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModifiableRegressionVariableType", propOrder = {
    "modifier"
})
@XmlSeeAlso({
    XmlGenericTradingDays.class,
    XmlUserVariables.class,
    XmlUserVariable.class
})
public abstract class XmlModifiableRegressionVariable
    extends XmlRegressionVariable
{

    @XmlElement(name = "Modifier")
    protected List<XmlRegressionVariableModifier> modifier;

    /**
     * Gets the value of the modifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegressionVariableModifierType }
     * 
     * 
     * @return 
     */
    
    public int getModifiersCount(){
        return modifier == null ? 0 : modifier.size();
    }
    
    public List<XmlRegressionVariableModifier> getModifiers() {
        if (modifier == null) {
            modifier = new ArrayList<>();
        }
        return this.modifier;
    }
     
}
