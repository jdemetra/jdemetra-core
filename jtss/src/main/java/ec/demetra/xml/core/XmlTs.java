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

import ec.tss.Ts;
import ec.tss.TsFactory;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.TsStatus;
import ec.tss.xml.IXmlUnmarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Jean Palate
 */
/**
 *
 * Complete information of a time series. It extends a "TsDataType" (time series
 * data) with its identifier and with metadata.
 *
 *
 * <p>
 * Java class for TsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="TsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TsDataType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="MetaData" type="{ec/eurostat/jdemetra/core}MetaDataType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{ec/eurostat/jdemetra/core}TsIdentifier"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlRootElement(name = "Ts")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TsType", propOrder = {
    "metaData"
})
public class XmlTs
        extends XmlTsData {

    @XmlElement(name = "MetaData")
    @XmlJavaTypeAdapter(XmlMetaData.Adapter.class)
    protected MetaData metaData;
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

    public static final IXmlUnmarshaller<XmlTs, TsInformation> INFO_UNMARSHALLER = (XmlTs xml) -> {
        TsMoniker moniker = TsMoniker.create(xml.source, xml.identifier);
        TsInformation info = new TsInformation(xml.name == null ? "series" : xml.name, moniker, xml.values != null
                ? TsInformationType.UserDefined : TsInformationType.None);
        if (xml.metaData != null) {
            info.metaData = xml.metaData;
        }
        if (xml.values != null) {
            info.data = XmlTsData.UNMARSHALLER.unmarshal(xml);
        }
        return info;
    };

    public static final InPlaceXmlMarshaller<XmlTs, TsInformation> INFO_MARSHALLER = (TsInformation v, XmlTs xml) -> {
        if (v.data != null) {
            XmlTsData.MARSHALLER.marshal(v.data, xml);
        }
        xml.source = v.moniker.getSource();
        xml.identifier = v.moniker.getId();
        xml.name = v.name;
        if (v.metaData != null && !v.metaData.isEmpty()) {
            xml.metaData = v.metaData;
        }
        return true;
    };

    public static final InPlaceXmlMarshaller<XmlTs, Ts> MARSHALLER = (Ts v, XmlTs xml) -> {
        if (v.hasData() == TsStatus.Valid) {
            XmlTsData.MARSHALLER.marshal(v.getTsData(), xml);
        }
        xml.source = v.getMoniker().getSource();
        xml.identifier = v.getMoniker().getId();
        xml.name = v.getName();
        xml.metaData = v.getMetaData();

        return true;
    };

    public static final IXmlUnmarshaller<XmlTs, Ts> TS_UNMARSHALLER = (XmlTs xml) -> {
        TsMoniker moniker = TsMoniker.create(xml.source, xml.identifier);
        Ts ts = TsFactory.instance.getTs(moniker);
        if (ts != null) {
            return ts;
        }
        TsData data = null;
        if (xml.values != null) {
            data = XmlTsData.UNMARSHALLER.unmarshal(xml);
        }
        return TsFactory.instance.createTs(xml.name, xml.metaData, data);
    };

    public static class Adapter extends XmlAdapter<XmlTs, TsInformation> {

        @Override
        public TsInformation unmarshal(XmlTs v) throws Exception {
            return INFO_UNMARSHALLER.unmarshal(v);
        }

        @Override
        public XmlTs marshal(TsInformation v) throws Exception {
            XmlTs xml = new XmlTs();
            INFO_MARSHALLER.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlTs, TsInformation> getTsAdapter() {
        return ADAPTER;
    }

}
