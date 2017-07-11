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
package ec.demetra.xml.sa.x13;

import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AutoTransformationSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="AutoTransformationSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Fct" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="0.5"/&gt;
 *               &lt;maxInclusive value="2.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoTransformationSpecType", propOrder = {
    "aicDiff"
})
public class XmlAutoTransformationSpec {

    @XmlElement(name = "AicDiff", defaultValue = "-2.0")
    protected Double aicDiff;

    /**
     * Gets the value of the aicDiff property.
     *
     * @return possible object is {@link Double }
     *
     */
    public double getAicDiff() {
        return aicDiff == null ? TransformSpec.DEF_AICDIFF : aicDiff;
    }

    /**
     * Sets the value of the aicDiff property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setAicDiff(Double value) {
        if (value != null && value == TransformSpec.DEF_AICDIFF) {
            aicDiff = null;
        } else {
            aicDiff = value;
        }
    }

    public static final InPlaceXmlUnmarshaller<XmlAutoTransformationSpec, TransformSpec> UNMARSHALLER = (XmlAutoTransformationSpec xml, TransformSpec v) -> {
        v.setFunction(DefaultTransformationType.Auto);
        v.setAICDiff(xml.getAicDiff());
        return true;
    };

    public static final InPlaceXmlMarshaller<XmlAutoTransformationSpec, TransformSpec> MARSHALLER = (TransformSpec v, XmlAutoTransformationSpec xml) -> {
        if (xml.aicDiff != null) {
            xml.setAicDiff(v.getAICDiff());
        }
        return true;
    };

}
