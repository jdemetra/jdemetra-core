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
package demetra.x13.io.xml.legacy;

import demetra.regarima.EstimateSpec;
import demetra.toolkit.io.xml.legacy.core.XmlPeriodSelection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EstimationSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="EstimationSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}EstimationSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Span" type="{ec/eurostat/jdemetra/core}PeriodSelectionType" minOccurs="0"/&gt;
 *         &lt;element name="Precision" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt; *
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "EstimationSpec")
@XmlType(name = "EstimationSpecType", propOrder = {
    "precision",})
public class XmlEstimationSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlEstimationSpec {

    @XmlElement(name = "Precision", defaultValue = "0.0000001")
    protected Double precision;

    /**
     * Gets the value of the precision property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getPrecision() {
        return precision == null ? EstimateSpec.DEF_TOL : precision;
    }

    /**
     * Sets the value of the precision property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setPrecision(Double value) {
        if (value != null && value == EstimateSpec.DEF_TOL) {
            precision = null;
        } else {
            precision = value;
        }
    }

    public boolean isDefault() {
        return span == null && precision == null;
    }

    public static final EstimateSpec unmarshal(XmlEstimationSpec xml) {
        EstimateSpec.Builder builder = EstimateSpec.builder();
        if (xml.span != null) {
            builder = builder.span(XmlPeriodSelection.unmarshal(xml.span));
        }
        if (xml.precision != null) {
            builder.tol(xml.precision);
        }
        return builder.build();
    }

    public static final XmlEstimationSpec marshal(EstimateSpec v) {
        if (v.isDefault()) {
            return null;
        }
        XmlEstimationSpec xml = new XmlEstimationSpec();
        marshal(v, xml);
        return xml;
    }

    public static final boolean marshal(EstimateSpec v, XmlEstimationSpec xml) {
        if (v.isDefault()) {
            return true;
        }
        xml.span = XmlPeriodSelection.marshal(v.getSpan());
        xml.setPrecision(v.getTol());
        return true;
    }
;
}
