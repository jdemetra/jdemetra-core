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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for RegressionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegressionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Item" type="{ec/eurostat/jdemetra/core}RegressionItemType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="Regression")
@XmlType(name = "RegressionType", propOrder = {
    "item"
})
public class XmlRegression {

    @XmlElement(name = "Item")
    protected List<XmlRegressionItem> item;

    /**
     * Gets the value of the item property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegressionItemType }
     * 
     * 
     */
    public List<XmlRegressionItem> getItems() {
        if (item == null) {
            item = new ArrayList<>();
        }
        return this.item;
    }
    
    public boolean isEmpty(){
        return item == null || item.isEmpty();
    }

   public static List<Class> xmlClasses(){
        List<Class> xmlclvar = TsVariableAdapters.getDefault().getXmlClasses();
        List<Class> xmlclmod = TsModifierAdapters.getDefault().getXmlClasses();
        xmlclvar.addAll(xmlclmod);
        return xmlclvar;
    }
    
    public static synchronized JAXBContext context() throws JAXBException{ 
    
        List<Class> xmlClasses = xmlClasses();
        xmlClasses.add(XmlRegression.class);
        JAXBContext jaxb = JAXBContext.newInstance(xmlClasses.toArray(new Class[xmlClasses.size()]));
        return jaxb;
    }
}
