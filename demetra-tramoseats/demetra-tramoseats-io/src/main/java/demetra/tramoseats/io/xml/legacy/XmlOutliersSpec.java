/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.xml.legacy;

import demetra.timeseries.TimeSelector;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import demetra.toolkit.io.xml.legacy.core.XmlPeriodSelection;
import demetra.tramo.OutlierSpec;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for OutlierSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="OutlierSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}OutlierSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Types" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS"/&gt;
 *         &lt;element name="CriticalValue" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="2"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EML" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="DeltaTC" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0"/&gt;
 *               &lt;maxExclusive value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutlierSpecType", propOrder = {
    "types",
    "criticalValue",
    "eml",
    "deltaTC"
})
public class XmlOutliersSpec
        extends demetra.toolkit.io.xml.legacy.modelling.XmlOutlierSpec {

    @XmlList
    @XmlElement(name = "Types", required = true)
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> types;
    @XmlElement(name = "CriticalValue")
    protected Double criticalValue;
    @XmlElement(name = "EML", defaultValue = "false")
    protected Boolean eml;
    @XmlElement(name = "DeltaTC", defaultValue = "0.7")
    protected Double deltaTC;

    /**
     * Gets the value of the types property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the types property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTypes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     *
     *
     * @return
     */
    public List<String> getTypes() {
        if (types == null) {
            types = new ArrayList<>();
        }
        return this.types;
    }

    /**
     * Gets the value of the criticalValue property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getCriticalValue() {
        return criticalValue;
    }

    /**
     * Sets the value of the criticalValue property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setCriticalValue(Double value) {
        if (value != null && value == 0) {
            this.criticalValue = null;
        } else {
            this.criticalValue = value;
        }
    }

    /**
     * Gets the value of the eml property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public boolean isEML() {
        return eml != null ? eml : false;
    }

    /**
     * Sets the value of the eml property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setEML(Boolean value) {
        if (value != null && !value) {
            this.eml = null;
        } else {
            this.eml = value;
        }
    }

    /**
     * Gets the value of the deltaTC property.
     *
     * @return possible object is {@link Double }
     *
     */
    public double getDeltaTC() {
        return deltaTC == null ? OutlierSpec.DEF_DELTATC : deltaTC;
    }

    /**
     * Sets the value of the deltaTC property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setDeltaTC(Double value) {
        if (value != null && value == OutlierSpec.DEF_DELTATC) {
            deltaTC = null;
        } else {
            this.deltaTC = value;
        }
    }

    public static OutlierSpec unmarshal(XmlOutliersSpec xml) {
        if (xml.types == null) {
            return OutlierSpec.DEFAULT;
        }
        OutlierSpec.Builder builder = OutlierSpec.builder();
        if (xml.span != null) {
            builder = builder.span(XmlPeriodSelection.getAdapter().unmarshal(xml.span));
        }
        if (xml.eml != null) {
            builder = builder.maximumLikelihood(xml.eml);
        }
        if (xml.criticalValue != null) {
            builder = builder.criticalValue(xml.criticalValue);
        }
        if (xml.deltaTC != null) {
            builder = builder.deltaTC(xml.deltaTC);
        }
        if (xml.types.contains(AdditiveOutlier.CODE)) {
            builder = builder.ao(true);
        }
        if (xml.types.contains(LevelShift.CODE)) {
            builder = builder.ls(true);
        }
        if (xml.types.contains(TransitoryChange.CODE)) {
            builder = builder.tc(true);
        }
        if (xml.types.contains(PeriodicOutlier.CODE)) {
            builder = builder.so(true);
        }
        return builder.build();
    }

    public static XmlOutliersSpec marshal(OutlierSpec v) {
        if (!v.isUsed()) {
            return null;
        }
        XmlOutliersSpec xml = new XmlOutliersSpec();
        if (!v.isDefault()) {
            marshal(v, xml);
        }
        return xml;
    }

    public static boolean marshal(OutlierSpec v, XmlOutliersSpec xml) {
        if (v.getSpan().getType() != TimeSelector.SelectionType.All) {
            xml.span = new XmlPeriodSelection();
            XmlPeriodSelection.marshal(v.getSpan(), xml.span);
        }
        xml.setCriticalValue(v.getCriticalValue());
        xml.setEML(v.isMaximumLikelihood());
        xml.setDeltaTC(v.getDeltaTC());

        List<String> types = xml.getTypes();
        if (v.isAo()) {
            types.add(AdditiveOutlier.CODE);
        }
        if (v.isLs()) {
            types.add(LevelShift.CODE);
        }
        if (v.isTc()) {
            types.add(TransitoryChange.CODE);
        }
        if (v.isSo()) {
            types.add(PeriodicOutlier.CODE);
        }
        return true;
    }
;
}
