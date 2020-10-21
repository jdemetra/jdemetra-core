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
import demetra.timeseries.TsData;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.io.xml.legacy.IXmlUnmarshaller;
import demetra.toolkit.io.xml.legacy.InPlaceXmlMarshaller;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
    protected Map<String, String> metaData;
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
    public Map<String, String> getMetaData() {
        return metaData;
    }

    /**
     * Sets the value of the metaData property.
     *
     * @param value allowed object is {@link MetaDataType }
     *
     */
    public void setMetaData(Map<String, String> value) {
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

    public static final InPlaceXmlMarshaller<XmlTs, Ts> TS_MARSHALLER = (Ts v, XmlTs xml) -> {
        TsData data = v.getData();
        if (! data.isEmpty()) {
            XmlTsData.MARSHALLER.marshal(data, xml);
        }
        xml.source = v.getMoniker().getSource();
        xml.identifier = v.getMoniker().getId();
        xml.name = v.getName();
        xml.metaData = v.getMeta();

        return true;
    };

     public static final IXmlUnmarshaller<XmlTs, Ts> TS_UNMARSHALLER = (XmlTs xml) -> {
        Ts.Builder builder = Ts.builder()
                .name(xml.name == null ? "series" : xml.name)
                .moniker(TsMoniker.of(xml.source, xml.identifier))
                .type(xml.values != null ? TsInformationType.UserDefined : TsInformationType.None);
        if (xml.metaData != null) {
            builder.meta(xml.metaData);
        }
        if (xml.values != null) {
            builder.data(XmlTsData.UNMARSHALLER.unmarshal(xml));
        }
        return builder.build();
    };

    public static class Adapter extends XmlAdapter<XmlTs, Ts> {

        @Override
        public Ts unmarshal(XmlTs v) throws Exception {
            return TS_UNMARSHALLER.unmarshal(v);
        }

        @Override
        public XmlTs marshal(Ts v) throws Exception {
            XmlTs xml = new XmlTs();
            TS_MARSHALLER.marshal(v, xml);
            return xml;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlTs, Ts> getTsAdapter() {
        return ADAPTER;
    }

}
