/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.core.XmlPeriodSelection;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.regression.OutlierType;
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
        extends ec.demetra.xml.modelling.XmlOutlierSpec {

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
     */
    public List<String> getTypes() {
        if (types == null) {
            types = new ArrayList<String>();
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

    public static final InPlaceXmlUnmarshaller<XmlOutliersSpec, OutlierSpec> UNMARSHALLER = (XmlOutliersSpec xml, OutlierSpec v) -> {
        if (xml.span != null) {
            XmlPeriodSelection.UNMARSHALLER.unmarshal(xml.span, v.getSpan());
        } else {
            v.getSpan().all();
        }
        if (xml.eml != null) {
            v.setEML(xml.eml);
        }
        if (xml.criticalValue != null) {
            v.setCriticalValue(xml.criticalValue);
        }
        if (xml.deltaTC != null) {
            v.setDeltaTC(xml.deltaTC);
        }
        for (String o : xml.types) {
            v.add(OutlierType.valueOf(o));
        }
        return true;
    };

    public static final IXmlMarshaller<XmlOutliersSpec, OutlierSpec> MARSHALLER = (OutlierSpec v) -> {
        if (!v.isUsed())
            return null;
        XmlOutliersSpec xml=new XmlOutliersSpec();
        if (v.isDefault())
            return xml;
        if (v.getSpan().getType() != PeriodSelectorType.All) {
            xml.span = new XmlPeriodSelection();
            XmlPeriodSelection.MARSHALLER.marshal(v.getSpan(), xml.span);
        }
        xml.setCriticalValue(v.getCriticalValue());
        xml.setEML(v.isEML());
        xml.setDeltaTC(v.getDeltaTC());
        OutlierType[] otypes = v.getTypes();
        for (int i = 0; i < otypes.length; ++i) {
            xml.getTypes().add(otypes[i].name());
        }
        return xml;
    };
}
