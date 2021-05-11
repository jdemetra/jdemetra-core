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
package demetra.tramoseats.io.xml.legacy;

import internal.workspace.file.xml.XmlProcessingContext;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TramoRequestsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TramoRequestsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Item" type="{ec/eurostat/jdemetra/sa/tramoseats}TramoAtomicRequestType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Context" type="{ec/eurostat/jdemetra/core}ProcessingContextType"/&gt;
 *         &lt;element name="OutputFilter" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "TramoRequests")
@XmlType(name = "TramoRequestsType", propOrder = {
    "item",
    "context",
    "outputFilter"
})
public class XmlTramoRequests {

    @XmlElement(name = "Item", required = true)
    protected List<XmlTramoAtomicRequest> item;
    @XmlElement(name = "Context")
    protected XmlProcessingContext context;
    @XmlList
    @XmlElement(name = "OutputFilter", required = true)
    protected List<String> outputFilter;
    @XmlAttribute(name = "flat")
    protected Boolean flat;
    @XmlAttribute(name = "parallelProcessing")
    protected Boolean parallel;

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
     * {@link TramoAtomicRequestType }
     *
     *
     */
    public List<XmlTramoAtomicRequest> getItems() {
        if (item == null) {
            item = new ArrayList<>();
        }
        return this.item;
    }

    /**
     * Gets the value of the context property.
     *
     * @return
     * possible object is
     * {@link ProcessingContextType }
     *
     */
    public XmlProcessingContext getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     *
     * @param value
     * allowed object is
     * {@link ProcessingContextType }
     *
     */
    public void setContext(XmlProcessingContext value) {
        this.context = value;
    }

    /**
     * Gets the value of the outputFilter property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outputFilter property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutputFilter().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getOutputFilter() {
        if (outputFilter == null) {
            outputFilter = new ArrayList<String>();
        }
        return this.outputFilter;
    }

    /**
     * @return the flat
     */
    public boolean getFlat() {
        return flat != null ? flat : false;
    }

    /**
     * @param flat the flat to set
     */
    public void setFlat(Boolean flat) {
        this.flat = flat;
    }

    /**
     * @return the flat
     */
    public boolean getParallelProcessing() {
        return parallel != null ? parallel : true;
    }

    /**
     * @param parallel
     */
    public void setParallelProcessing(Boolean parallel) {
        this.parallel = parallel;
    }

    public void checkNames() {
        int n = 1;
        for (XmlTramoAtomicRequest req : item) {
            if (req.series.getName() == null || req.series.getName().length() == 0) {
                req.series.setName("series" + n);
            }
            ++n;
        }
    }

}
