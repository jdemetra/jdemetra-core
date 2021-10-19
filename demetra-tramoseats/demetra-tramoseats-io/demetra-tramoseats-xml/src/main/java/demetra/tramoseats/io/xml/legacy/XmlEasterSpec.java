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

import demetra.tramo.EasterSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EasterSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EasterSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}EasterSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Test" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="option" type="{ec/eurostat/jdemetra/sa/tramoseats}EasterHolidayEnum" default="IncludeEaster" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EasterSpecType", propOrder = {
    "test"
})
public class XmlEasterSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlEasterSpec {

    @XmlElement(name = "Test")
    protected Boolean test;
    @XmlAttribute(name = "option")
    protected EasterSpec.Type option;

    /**
     * Gets the value of the test property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public boolean isTest() {
        return test == null ? false : test;
    }

    /**
     * Sets the value of the test property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setTest(Boolean value) {
        if (value != null && !value) {
            this.test = null;
        } else {
            this.test = value;
        }
    }

    /**
     * Gets the value of the option property.
     *
     * @return possible object is {@link EasterHolidayEnum }
     *
     */
    public EasterSpec.Type getOption() {
        return option;
    }

    /**
     * Sets the value of the option property.
     *
     * @param value allowed object is {@link EasterHolidayEnum }
     *
     */
    public void setOption(EasterSpec.Type value) {
        if (value == EasterSpec.Type.IncludeEaster) {
            option = null;
        } else {
            this.option = value;
        }
    }
    
    public static XmlEasterSpec marshal(EasterSpec v ){
        if (! v.isDefined())
            return null;
        XmlEasterSpec xml=new XmlEasterSpec();
        marshal(v, xml);
        return xml;
    }
    

    public static boolean marshal(EasterSpec v, XmlEasterSpec xml){
        if (v.isDefault()) {
            return true;
        }
        if (v.getDuration() != EasterSpec.DEF_IDUR) {
            xml.setDuration(v.getDuration());
        }
        xml.setJulian(v.isJulian());
        xml.setTest(v.isTest());
        xml.setOption(v.getType());
        return true;
    }

    public static EasterSpec unmarshal(XmlEasterSpec xml){
        if (xml == null)
            return EasterSpec.DEFAULT_UNUSED;
        EasterSpec.Builder builder = EasterSpec.builder();
        if (xml.julian != null) {
            builder=builder.julian(xml.julian);
        }
        if (xml.test != null) {
            builder=builder.test(xml.test);
        }
        if (xml.duration != null) {
            builder=builder.duration(xml.duration);
        }
        if (xml.option != null) {
            builder=builder.type(xml.option);
        }
        return builder.build();
    }

}
