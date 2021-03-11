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
package demetra.toolkit.io.xml.legacy.processing;

import demetra.timeseries.TsData;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.DynamicTsDataSupplier;
import demetra.timeseries.regression.DynamicTsVariable;
import demetra.toolkit.io.xml.legacy.core.XmlTsData;
import demetra.toolkit.io.xml.legacy.core.XmlTsMoniker;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * Default dynamic regression variable. Identifier must be present. The name of
 * the tsdata sub-item should be considered as a description, while the "name"
 * attribute is the actual name of the variable
 *
 *
 * <p>
 * Java class for DynamicTsVariableType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="DynamicTsVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TsVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Moniker" type="{ec/eurostat/jdemetra/core}XmlTsMoniker"/&gt;
 *         &lt;element name="Tsdata" type="{ec/eurostat/jdemetra/core}TsDataType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DynamicTsVariableType", propOrder = {
    "moniker",
    "tsData"
})
public class XmlDynamicTsVariable
        extends XmlTsVariable {

    @XmlElement(name = "Moniker", required = true)
    @XmlJavaTypeAdapter(XmlTsMoniker.Adapter.class)
    protected TsMoniker moniker;
    @XmlElement(name = "TsData")
    protected XmlTsData tsData;

    /**
     * Gets the value of the moniker property.
     *
     * @return possible object is {@link XmlTsMoniker }
     *
     */
    public TsMoniker getMoniker() {
        return moniker;
    }

    /**
     * Sets the value of the moniker property.
     *
     * @param value allowed object is {@link XmlTsMoniker }
     *
     */
    public void setMoniker(TsMoniker value) {
        this.moniker = value;
    }

    /**
     * Gets the value of the tsdata property.
     *
     * @return possible object is {@link TsDataType }
     *
     */
    public XmlTsData getTsdata() {
        return tsData;
    }

    /**
     * Sets the value of the tsdata property.
     *
     * @param value allowed object is {@link TsDataType }
     *
     */
    public void setTsdata(XmlTsData value) {
        this.tsData = value;
    }

    public static class Adapter extends XmlAdapter<XmlDynamicTsVariable, DynamicTsDataSupplier> {

        @Override
        public DynamicTsDataSupplier unmarshal(XmlDynamicTsVariable v) {
            if (v.tsData != null) {
                return new DynamicTsDataSupplier(v.moniker, XmlTsData.unmarshal(v.tsData));
            } else {
                return new DynamicTsDataSupplier(v.moniker, null);
            }
        }

        @Override
        public XmlDynamicTsVariable marshal(DynamicTsDataSupplier v) {
            XmlDynamicTsVariable x = new XmlDynamicTsVariable();
            TsData data = v.get();
            if (data != null) {
                x.tsData = new XmlTsData();
                XmlTsData.marshal(data, x.tsData);
            }
            x.moniker = v.getMoniker();
            return x;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }

}
