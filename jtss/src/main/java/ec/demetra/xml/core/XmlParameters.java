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

import ec.tss.xml.*;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Array of parameters. When all the coefficients are unspecified, the order can
 * be used
 *
 *
 * <p>
 * Java class for ParametersType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ParametersType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Coefficient" type="{ec/eurostat/jdemetra/core}IndexedParameterType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="Order" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParametersType", propOrder = {
    "coefficient",
    "order"
})
public class XmlParameters {

    @XmlElement(name = "Coefficient")
    protected List<XmlIndexedParameter> coefficient;
    @XmlElement(name = "Order")
    @XmlSchemaType(name = "unsignedInt")
    protected Integer order;

    /**
     * Gets the value of the coefficient property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the coefficient property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCoefficient().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IndexedParameterType }
     *
     *
     */
    public List<XmlIndexedParameter> getCoefficient() {
        if (coefficient == null) {
            coefficient = new ArrayList<>();
        }
        return this.coefficient;
    }

    /**
     * Gets the value of the order property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setOrder(Integer value) {
        this.order = value;
    }

    public int size() {
        if (order != null) {
            return order;
        } else {
            return coefficient.size();
        }
    }

    public static final InPlaceXmlMarshaller<XmlParameters, Parameter[]> MARSHALLER = (Parameter[] v, XmlParameters xml) -> {
        if (v == null) {
            return true;
        }
        if (Parameter.isDefault(v)) {
            xml.setOrder(v.length);
        } else {
            for (int i = 0; i < v.length; ++i) {
                XmlIndexedParameter xp = new XmlIndexedParameter();
                XmlParameter.MARSHALLER.marshal(v[i], xp);
                xp.setIndex(i + 1);
                xml.getCoefficient().add(xp);
            }
        }
        return true;

    };

    public static final InPlaceXmlUnmarshaller<XmlParameters, Parameter[]> UNMARSHALLER = (XmlParameters xml, Parameter[] v) -> {
        if (xml.order != null){
            return true;
        }
        for (int i = 0; i < xml.size(); ++i) {
            XmlParameter.UNMARSHALLER.unmarshal(xml.coefficient.get(i), v[i]);
        }
        return true;
    };

    public static class Adapter extends XmlAdapter<XmlParameters, Parameter[]> {

        @Override
        public Parameter[] unmarshal(XmlParameters v) throws Exception {
            if (v == null) {
                return null;
            }
            Parameter[] p = Parameter.create(v.size());
            if (p != null) {
                UNMARSHALLER.unmarshal(v, p);
            }
            return p;
        }

        @Override
        public XmlParameters marshal(Parameter[] v) throws Exception {
            if (v == null) {
                return null;
            }
            XmlParameters x = new XmlParameters();
            MARSHALLER.marshal(v, x);
            return x;
        }
    };

    private static final XmlAdapter<XmlParameters, Parameter[]> ADAPTER = new Adapter();

    public static XmlAdapter<XmlParameters, Parameter[]> getAdapter() {
        return ADAPTER;
    }
}
