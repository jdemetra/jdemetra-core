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
package demetra.toolkit.io.xml.legacy.core;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsMoniker;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ordered collection of time series. The collection may have its own identifier
 * and its own metadata.
 *
 *
 * <p>
 * Java class for TsCollectionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TsCollectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MetaData" type="{ec/eurostat/jdemetra/core}MetaDataType" minOccurs="0"/&gt;
 *         &lt;element name="Data"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Ts" type="{ec/eurostat/jdemetra/core}TsType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{ec/eurostat/jdemetra/core}TsIdentifier"/&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlRootElement(name = "TsCollection")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TsCollectionType", propOrder = {
        "metaData",
        "data"
})
public class XmlTsCollection {

    @XmlElement(name = "MetaData")
    @XmlJavaTypeAdapter(XmlMetaData.Adapter.class)
    protected Map<String, String> metaData;
    @XmlElement(name = "Data", required = true)
    protected XmlTsCollection.Data data;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "source")
    protected String source;
    @XmlAttribute(name = "identifier")
    protected String identifier;

    /**
     * Gets the value of the metaData property.
     *
     * @return possible object is {@link MetaDataType }
     */
    public Map<String, String> getMetaData() {
        return metaData;
    }

    /**
     * Sets the value of the metaData property.
     *
     * @param value allowed object is {@link MetaDataType }
     */
    public void setMetaData(Map<String, String> value) {
        this.metaData = value;
    }

    /**
     * Gets the value of the data property.
     *
     * @return possible object is {@link TsCollectionType.Data }
     */
    public XmlTsCollection.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     *
     * @param value allowed object is {@link TsCollectionType.Data }
     */
    public void setData(XmlTsCollection.Data value) {
        this.data = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link String }
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the identifier property.
     *
     * @return possible object is {@link String }
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Ts" type="{ec/eurostat/jdemetra/core}TsType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "ts"
    })
    public static class Data {

        @XmlElement(name = "Ts")
        protected List<XmlTs> ts;

        /**
         * Gets the value of the ts property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a
         * snapshot. Therefore any modification you make to the returned list
         * will be present inside the JAXB object. This is why there is not a
         * <CODE>set</CODE> method for the ts property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTs().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link TsType }
         */
        public List<XmlTs> getTs() {
            if (ts == null) {
                ts = new ArrayList<>();
            }
            return this.ts;
        }
    }

    public static TsCollection unmarshal(XmlTsCollection xml) {
        TsCollection.Builder builder = TsCollection.builder()
                .moniker(TsMoniker.of(xml.source, xml.identifier))
                .name(xml.name == null ? "set" : xml.name);
        if (xml.metaData != null) {
            builder.meta(xml.metaData);
        }
        if (xml.data != null) {
            builder.items(unmarshal(xml.data));
        }
        return builder.build();
    }

    private static List<Ts> unmarshal(Data data) {
        return data.getTs().stream().map(XmlTs::unmarshal).collect(Collectors.toList());
    }

    public static final boolean marshal(TsCollection v, XmlTsCollection xml) {
        xml.source = v.getMoniker().getSource();
        xml.identifier = v.getMoniker().getId();
        xml.name = v.getName();
        if (!v.getMeta().isEmpty()) {
            xml.metaData = v.getMeta();
        }
        if (!v.isEmpty()) {
            xml.data = new Data();
            for (Ts s : v) {
                XmlTs xs = new XmlTs();
                XmlTs.marshal(s, xs);
                xml.data.getTs().add(xs);
            }
        }
        return true;
    }

    public static class Adapter extends XmlAdapter<XmlTsCollection, TsCollection> {

        @Override
        public TsCollection unmarshal(XmlTsCollection v) throws Exception {
            return XmlTsCollection.unmarshal(v);
        }

        @Override
        public XmlTsCollection marshal(TsCollection v) throws Exception {
            XmlTsCollection xml = new XmlTsCollection();
            XmlTsCollection.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlTsCollection, TsCollection> getTsAdapter() {
        return ADAPTER;
    }

}
