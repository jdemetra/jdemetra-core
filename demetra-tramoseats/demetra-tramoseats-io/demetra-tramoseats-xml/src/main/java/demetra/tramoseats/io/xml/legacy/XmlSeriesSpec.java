/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.xml.legacy;

import demetra.timeseries.TimeSelector;
import demetra.toolkit.io.xml.legacy.core.XmlPeriodSelection;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SeriesSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="SeriesSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}SeriesSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PreliminaryChecks" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SeriesSpecType", propOrder = {
    "preliminaryChecks"
})
public class XmlSeriesSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlSeriesSpec {

    @XmlElement(name = "PreliminaryChecks", defaultValue = "true")
    protected Boolean preliminaryChecks;

    /**
     * Gets the value of the preliminaryChecks property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isPreliminaryChecks() {
        return preliminaryChecks;
    }

    /**
     * Sets the value of the preliminaryChecks property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setPreliminaryChecks(Boolean value) {
        if (value != null && value) {
            this.preliminaryChecks = null;
        } else {
            this.preliminaryChecks = value;
        }
    }

    public static XmlSeriesSpec marshal(TramoSpec v) {
        TimeSelector vspan = v.getTransform().getSpan();
        if (v.getTransform().isPreliminaryCheck() && vspan.isAll()) {
            return null; // default
        }
        XmlSeriesSpec xml = new XmlSeriesSpec();
        xml.setPreliminaryChecks(v.getTransform().isPreliminaryCheck());
        if (!vspan.isAll()) {
            xml.span = new XmlPeriodSelection();
            XmlPeriodSelection.marshal(vspan, xml.span);
        }
        return xml;
    }

    public static TransformSpec.Builder unmarshal(XmlSeriesSpec xml, TransformSpec.Builder builder) {
        if (xml == null) {
            return builder;
        }
        if (xml.preliminaryChecks != null) {
            builder = builder.preliminaryCheck(xml.preliminaryChecks);
        }
        if (xml.span != null) {
            builder = builder.span(XmlPeriodSelection.unmarshal(xml.span));
        }
        return builder;
    }
}
