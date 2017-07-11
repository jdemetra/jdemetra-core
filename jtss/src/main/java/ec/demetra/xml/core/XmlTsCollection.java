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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.xml.IXmlUnmarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tstoolkit.MetaData;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
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
 *
 *
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
    protected MetaData metaData;
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
     *
     */
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * Sets the value of the metaData property.
     *
     * @param value allowed object is {@link MetaDataType }
     *
     */
    public void setMetaData(MetaData value) {
        this.metaData = value;
    }

    /**
     * Gets the value of the data property.
     *
     * @return possible object is {@link TsCollectionType.Data }
     *
     */
    public XmlTsCollection.Data getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     *
     * @param value allowed object is {@link TsCollectionType.Data }
     *
     */
    public void setData(XmlTsCollection.Data value) {
        this.data = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the identifier property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     *
     * @param value allowed object is {@link String }
     *
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
     *
     *
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
         *
         *
         */
        public List<XmlTs> getTs() {
            if (ts == null) {
                ts = new ArrayList<>();
            }
            return this.ts;
        }
    }

    public static final IXmlUnmarshaller<XmlTsCollection, TsCollectionInformation> INFO_UNMARSHALLER = (XmlTsCollection xml) -> {
        TsMoniker moniker = TsMoniker.create(xml.source, xml.identifier);
        TsCollectionInformation cinfo = new TsCollectionInformation(moniker,
                TsInformationType.UserDefined);
        cinfo.name = xml.name;
        if (xml.metaData != null) {
            cinfo.metaData = xml.metaData;
        }
        if (xml.data != null) {
            for (XmlTs xs : xml.data.getTs()) {
                cinfo.items.add(XmlTs.INFO_UNMARSHALLER.unmarshal(xs));
            }
        }
        return cinfo;
    };

    public static final IXmlUnmarshaller<XmlTsCollection, TsCollection> UNMARSHALLER = (XmlTsCollection xml) -> {
        TsCollectionInformation info = INFO_UNMARSHALLER.unmarshal(xml);
        if (info == null) {
            return null;
        }
        ArrayList<Ts> ts = new ArrayList<>();
        if (info.items != null) {
            for (TsInformation tsinfo : info.items) {
                ts.add(TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker,
                        tsinfo.metaData, tsinfo.data));
            }
        }
        return TsFactory.instance.createTsCollection(info.name, info.moniker,
                info.metaData, ts);
    };

    public static final InPlaceXmlMarshaller<XmlTsCollection, TsCollectionInformation> INFO_MARSHALLER = (TsCollectionInformation v, XmlTsCollection xml) -> {
        xml.source = v.moniker.getSource();
        xml.identifier = v.moniker.getId();
        xml.name = v.name;
        if (v.metaData != null && !v.metaData.isEmpty()) {
            xml.metaData = v.metaData;
        }
        if (!v.items.isEmpty()) {
            xml.data = new Data();
            for (TsInformation info : v.items) {
                XmlTs xs = new XmlTs();
                XmlTs.INFO_MARSHALLER.marshal(info, xs);
                xml.data.getTs().add(xs);
            }
        }
        return true;
    };

    public static final InPlaceXmlMarshaller<XmlTsCollection, TsCollection> MARSHALLER = (TsCollection v, XmlTsCollection xml) -> {
        xml.source = v.getMoniker().getSource();
        xml.identifier = v.getMoniker().getId();
        xml.name = v.getName();
        xml.metaData = v.getMetaData();

        int n = v.getCount();
        if (n > 0) {
            xml.data = new Data();
            for (Ts s : v) {
                XmlTs xs = new XmlTs();
                XmlTs.MARSHALLER.marshal(s, xs);
                xml.data.getTs().add(xs);
            }
        }
        return true;
    };

    public static class Adapter extends XmlAdapter<XmlTsCollection, TsCollectionInformation> {

        @Override
        public TsCollectionInformation unmarshal(XmlTsCollection v) throws Exception {
            return INFO_UNMARSHALLER.unmarshal(v);
        }

        @Override
        public XmlTsCollection marshal(TsCollectionInformation v) throws Exception {
            XmlTsCollection xml = new XmlTsCollection();
            INFO_MARSHALLER.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlTsCollection, TsCollectionInformation> getTsAdapter() {
        return ADAPTER;
    }

}
