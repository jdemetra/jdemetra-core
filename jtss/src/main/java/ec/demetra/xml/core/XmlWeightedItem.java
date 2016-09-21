/*
* Copyright 2013 National Bank of Belgium
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


package ec.demetra.xml.core;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeightedItem", propOrder = {
    "item",
    "weight"
})
public class XmlWeightedItem {

    @XmlElement(name = "Item", required = true)
    @XmlSchemaType(name = "IDREF")
    protected String item;
    @XmlElement(name = "Weight")
    protected double weight;
    
    public XmlWeightedItem(){}
    
    public XmlWeightedItem(String item, double weight)
    {
        this.item=item;
        this.weight=weight;
    }

    public XmlWeightedItem(String item)
    {
        this.item=item;
        this.weight=1;
    }

    /**
     * Gets the value of the item property.
     * 
     * @return
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     * 
     * @param value
     */
    public void setItem(String value) {
        this.item = value;
    }

    /**
     * Gets the value of the weight property.
     * 
     * @return 
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     * 
     * @param value
     */
    public void setWeight(double value) {
        this.weight = value;
    }

}
