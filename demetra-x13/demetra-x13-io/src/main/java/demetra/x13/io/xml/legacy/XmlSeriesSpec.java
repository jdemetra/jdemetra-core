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

import demetra.regarima.BasicSpec;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.TimeSelector;
import demetra.toolkit.io.xml.legacy.core.XmlPeriodSelection;
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
    
    public static final XmlSeriesSpec marshal(BasicSpec v) {
        TimeSelector vspan = v.getSpan();
        if (v.isPreliminaryCheck() && vspan.isAll())
            return null; // default
        XmlSeriesSpec xml=new XmlSeriesSpec();
        marshal(v, xml);
        return xml;
    }
    

    public static final boolean marshal(BasicSpec v, XmlSeriesSpec xml) {
        TimeSelector vspan = v.getSpan();
        if (v.isPreliminaryCheck() && vspan.isAll())
            return true; // default
        
        xml.setPreliminaryChecks(v.isPreliminaryCheck());
        if (! vspan.isAll()){
            xml.span=XmlPeriodSelection.marshal(vspan);
        }
        return true;
    };
    
    public static final BasicSpec unmarshal(XmlSeriesSpec xml) {
        if (xml == null)
            return BasicSpec.DEFAULT;
        BasicSpec.Builder builder = BasicSpec.builder();
        if (xml.preliminaryChecks != null)
            builder.preliminaryCheck(xml.preliminaryChecks);
        if (xml.span != null)
            builder.span(XmlPeriodSelection.unmarshal(xml.span));
        return builder.build();
    };
}
