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

import ec.demetra.xml.core.XmlPeriodSelection;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
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
    "outlier",
    "deltaTC",
    "method"
})
public class XmlOutliersSpec
        extends ec.demetra.xml.modelling.XmlOutlierSpec {

    @XmlElement(name = "Types", required = true)
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> types;
    @XmlElement(name = "CriticalValue")
    protected Double criticalValue;
    @XmlElement(name = "Outlier")
    protected List<XmlSingleOutlierSpec> outlier;
    @XmlElement(name = "DeltaTC", defaultValue = "0.7")
    protected Double deltaTC;
    @XmlElement(name = "Method")
    protected OutlierSpec.Method method;

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
     * Gets the value of the deltaTC property.
     *
     * @return possible object is {@link Double }
     *
     */
    public double getDeltaTC() {
        return deltaTC == null ? OutlierSpec.DEF_TCRATE : deltaTC;
    }

    /**
     * Sets the value of the deltaTC property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setDeltaTC(Double value) {
        if (value != null && value == OutlierSpec.DEF_TCRATE) {
            deltaTC = null;
        } else {
            this.deltaTC = value;
        }
    }

    public List<XmlSingleOutlierSpec> getOutliers() {
        if (outlier == null) {
            outlier = new ArrayList<>();
        }
        return outlier;
    }

    public OutlierSpec.Method getMethod() {
        return method;
    }

    public void setMethod(OutlierSpec.Method method) {
        if (method != null && method == OutlierSpec.Method.AddOne) {
            this.method = null;
        } else {
            this.method = method;
        }
    }

    public static final InPlaceXmlUnmarshaller<XmlOutliersSpec, OutlierSpec> UNMARSHALLER = (XmlOutliersSpec xml, OutlierSpec v) -> {
        if (xml.span != null) {
            XmlPeriodSelection.UNMARSHALLER.unmarshal(xml.span, v.getSpan());
        } else {
            v.getSpan().all();
        }
        if (xml.types != null && !xml.types.isEmpty()) {
            if (xml.criticalValue != null) {
                v.setDefaultCriticalValue(xml.criticalValue);
            }
            xml.types.stream().forEach((o) -> {
                v.add(OutlierType.valueOf(o));
            });
        } else if (xml.outlier != null) {
            // process single outliers
            for (XmlSingleOutlierSpec xcur : xml.outlier){
                SingleOutlierSpec cur=new SingleOutlierSpec();
                cur.setType(OutlierType.valueOf(xcur.type));
                if (xcur.criticalValue != null){
                    cur.setCriticalValue(xcur.criticalValue);
                }
                v.add(cur);
            }

        }
        if (xml.deltaTC != null) {
            v.setMonthlyTCRate(xml.deltaTC);
        }
        if (xml.method != null) {
            v.setMethod(xml.method);
        }

        return true;
    };

    public static final IXmlMarshaller<XmlOutliersSpec, OutlierSpec> MARSHALLER = (OutlierSpec v) -> {
        if (!v.isUsed()) {
            return null;
        }
        XmlOutliersSpec xml = new XmlOutliersSpec();
        if (v.getSpan().getType() != PeriodSelectorType.All) {
            xml.span = new XmlPeriodSelection();
            XmlPeriodSelection.MARSHALLER.marshal(v.getSpan(), xml.span);
        }
        xml.setCriticalValue(v.getDefaultCriticalValue());
        xml.setDeltaTC(v.getMonthlyTCRate());
        SingleOutlierSpec[] otypes= v.getTypes();
        double va=otypes[0].getCriticalValue();
        boolean same=true;
        for (int i=1; i<otypes.length; ++i){
            if (otypes[i].getCriticalValue() != va){
                same=false;
                break;
            }
        }
        if (same){
            List<String> stypes = xml.getTypes();
            for (int i=0; i<otypes.length; ++i){
                stypes.add(otypes[i].getType().name());
            }
        }else{
            List<XmlSingleOutlierSpec> xtypes = xml.getOutliers();
            for (int i=0; i<otypes.length; ++i){
                XmlSingleOutlierSpec xcur=new XmlSingleOutlierSpec();
                xcur.type=otypes[i].getType().name();
                xcur.criticalValue=otypes[i].getCriticalValue();
                xtypes.add(xcur);
            }
        }
         return xml;
    };
}
