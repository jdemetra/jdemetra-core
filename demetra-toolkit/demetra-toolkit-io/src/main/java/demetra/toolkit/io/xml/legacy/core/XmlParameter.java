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

import demetra.data.Parameter;
import demetra.data.ParameterType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Parameter, with (except for "Undefined" parameter) its value and the way it
 * has to be interpreted. Standard error and TStat of the parameter can be
 * provided if the parameter has been estimated or derived.
 *
 *
 * <p>
 * Java class for ParameterType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="ParameterType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="Stde" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="Tstat" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" use="required" type="{ec/eurostat/jdemetra/core}ParameterInfoEnum" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterType", propOrder = {
    "value",
    "stde",
    "tstat"
})
@XmlSeeAlso({})
public class XmlParameter {

    @XmlElement(name = "Value")
    private Double value;
    @XmlElement(name = "Stde")
    protected Double stde;
    @XmlElement(name = "Tstat")
    protected Double tstat;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "type", required = true)
    protected ParameterType type;

    public XmlParameter() {
    }

    public XmlParameter(double val, ParameterType type) {
        this.value = val;
        this.type = type;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setValue(Double value) {
        this.value = value;
    }

//    /**
//     * Gets the value of the stde property.
//     *
//     * @return possible object is {@link Double }
//     *
//     */
//    public Double getStde() {
//        return stde;
//    }
//
//    /**
//     * Sets the value of the stde property.
//     *
//     * @param value allowed object is {@link Double }
//     *
//     */
//    public void setStde(Double value) {
//        if (value != null && value == 0D) {
//            this.stde = 0D;
//        } else {
//            this.stde = value;
//        }
//    }
//    /**
//     * Gets the value of the tstat property.
//     *
//     * @return possible object is {@link Double }
//     *
//     */
//    public Double getTstat() {
//        return tstat;
//    }
//
//    /**
//     * Sets the value of the tstat property.
//     *
//     * @param value allowed object is {@link Double }
//     *
//     */
//    public void setTstat(Double value) {
//        this.tstat = value;
//    }
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
     * Gets the value of the type property.
     *
     * @return possible object is {@link ParameterInfoEnum }
     *
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link ParameterInfoEnum }
     *
     */
    public void setType(ParameterType value) {
        this.type = value;
    }

    public static boolean marshal(Parameter v, XmlParameter xml) {
        if (!Parameter.isDefined(v)) {
            xml.type = ParameterType.Undefined;
            return true;
        } else {
            xml.type = v.getType();
            xml.value = v.getValue();
            return true;
        }
    }

    public static Parameter unmarshal(XmlParameter v){
        switch (v.type) {
            case Initial:
                return Parameter.initial(v.value);
            case Fixed:
                return Parameter.fixed(v.value);
            case Estimated:
                return Parameter.estimated(v.value);
            default:
                return Parameter.undefined();
        }
    }

    public static class Adapter extends XmlAdapter<XmlParameter, Parameter> {

        @Override
        public Parameter unmarshal(XmlParameter v) throws Exception {
            return XmlParameter.unmarshal(v);
        }

        @Override
        public XmlParameter marshal(Parameter v) throws Exception {
            XmlParameter x = new XmlParameter();
            XmlParameter.marshal(v, x);
            return x;
        }
    };

    private final static Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
