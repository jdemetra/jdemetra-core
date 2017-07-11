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

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.MetaData;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Metadata defined as an (unordered) set of properties
 *
 *
 * <p>
 * Java class for MetaDataType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="MetaDataType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Property" type="{ec/eurostat/jdemetra/core}PropertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetaDataType", propOrder = {
    "property"
})
public class XmlMetaData {

    static final String RNAME = "MetaData", NAME = RNAME + "Type";
    /**
     *
     */
    @XmlElement(name = "Property")
    protected XmlProperty[] property;

    /**
     * @return the property
     */
    public XmlProperty[] getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(XmlProperty[] property) {
        this.property = property;
    }

    public static class Adapter extends XmlAdapter<XmlMetaData, MetaData> {

        @Override
        public MetaData unmarshal(XmlMetaData v) throws Exception {
            if (v == null || v.property == null) {
                return null;
            }
            MetaData rslt = new MetaData();
            for (int i = 0; i < v.property.length; ++i) {
                rslt.put(v.property[i].name, v.property[i].value);
            }
            return rslt;
        }

        @Override
        public XmlMetaData marshal(MetaData v) throws Exception {
            if (v == null || v.isEmpty()) {
                return null;
            }
            XmlMetaData x = new XmlMetaData();
            Set<String> keys = v.keySet();

            x.property = new XmlProperty[keys.size()];
            int pos = 0;
            for (String key : keys) {
                XmlProperty xp = new XmlProperty();
                xp.name = key;
                xp.value = v.get(key);
                x.property[pos] = xp;
                ++pos;
            }
            return x;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static XmlAdapter<XmlMetaData, MetaData> getAdapter() {
        return ADAPTER;
    }

}
