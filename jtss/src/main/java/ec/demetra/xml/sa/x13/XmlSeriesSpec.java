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
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.TsPeriodSelector;
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
        extends ec.demetra.xml.modelling.XmlSeriesSpec {

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

    public static final IXmlMarshaller<XmlSeriesSpec, RegArimaSpecification> MARSHALLER = (RegArimaSpecification v) -> {
        TsPeriodSelector vspan = v.getBasic().getSpan();
        if (v.getBasic().isPreliminaryCheck() && vspan.isAll())
            return null; // default
        
        XmlSeriesSpec xml=new XmlSeriesSpec();
        xml.setPreliminaryChecks(v.getBasic().isPreliminaryCheck());
        if (! vspan.isAll()){
            xml.span=new XmlPeriodSelection();
            XmlPeriodSelection.MARSHALLER.marshal(vspan, xml.span);
        }
        return xml;
    };
    
    public static final InPlaceXmlUnmarshaller<XmlSeriesSpec, RegArimaSpecification> UNMARSHALLER = (XmlSeriesSpec xml, RegArimaSpecification v) -> {
        if (xml.preliminaryChecks != null)
            v.getBasic().setPreliminaryCheck(xml.preliminaryChecks);
        if (xml.span != null)
            XmlPeriodSelection.UNMARSHALLER.unmarshal(xml.span, v.getBasic().getSpan());
        return true;
    };
}
