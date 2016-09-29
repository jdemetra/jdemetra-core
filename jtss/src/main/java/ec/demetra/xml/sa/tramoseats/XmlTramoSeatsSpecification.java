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
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.sa.XmlSaSpecification;
import ec.demetra.xml.sa.benchmarking.XmlCholetteSpec;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for TramoSeatsSpecificationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TramoSeatsSpecificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/sa}SaSpecificationType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Preprocessing" type="{ec/eurostat/jdemetra/sa/tramoseats}TramoSpecificationType" minOccurs="0"/&gt;
 *         &lt;element name="Decomposition" type="{ec/eurostat/jdemetra/sa/tramoseats}SeatsSpecType"/&gt;
 *         &lt;element name="Benchmarking" type="{ec/eurostat/jdemetra/sa/benchmarking}CholetteType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="TramoSeatsSpecification")
@XmlType(name = "TramoSeatsSpecificationType", propOrder = {
    "preprocessing",
    "decomposition",
    "benchmarking"
})
public class XmlTramoSeatsSpecification
    extends XmlSaSpecification
{

    @XmlElement(name = "Preprocessing")
    protected XmlTramoSpecification preprocessing;
    @XmlElement(name = "Decomposition", required = true)
    protected XmlSeatsSpec decomposition;
    @XmlElement(name = "Benchmarking")
    protected XmlCholetteSpec benchmarking;

    /**
     * Gets the value of the preprocessing property.
     * 
     * @return
     *     possible object is
     *     {@link TramoSpecificationType }
     *     
     */
    public XmlTramoSpecification getPreprocessing() {
        return preprocessing;
    }

    /**
     * Sets the value of the preprocessing property.
     * 
     * @param value
     *     allowed object is
     *     {@link TramoSpecificationType }
     *     
     */
    public void setPreprocessing(XmlTramoSpecification value) {
        this.preprocessing = value;
    }

    /**
     * Gets the value of the decomposition property.
     * 
     * @return
     *     possible object is
     *     {@link SeatsSpecType }
     *     
     */
    public XmlSeatsSpec getDecomposition() {
        return decomposition;
    }

    /**
     * Sets the value of the decomposition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SeatsSpecType }
     *     
     */
    public void setDecomposition(XmlSeatsSpec value) {
        this.decomposition = value;
    }

    /**
     * Gets the value of the benchmarking property.
     * 
     * @return
     *     possible object is
     *     {@link CholetteType }
     *     
     */
    public XmlCholetteSpec getBenchmarking() {
        return benchmarking;
    }

    /**
     * Sets the value of the benchmarking property.
     * 
     * @param value
     *     allowed object is
     *     {@link CholetteType }
     *     
     */
    public void setBenchmarking(XmlCholetteSpec value) {
        this.benchmarking = value;
    }
    
    public static final InPlaceXmlUnmarshaller<XmlTramoSeatsSpecification, TramoSeatsSpecification> UNMARSHALLER = (XmlTramoSeatsSpecification xml, TramoSeatsSpecification v) -> {
        if (xml.preprocessing != null)
            XmlTramoSpecification.UNMARSHALLER.unmarshal(xml.preprocessing, v.getTramoSpecification());
        if (xml.decomposition != null)
            XmlSeatsSpec.UNMARSHALLER.unmarshal(xml.decomposition, v.getSeatsSpecification());
        if (xml.benchmarking != null)
            XmlCholetteSpec.UNMARSHALLER.unmarshal(xml.benchmarking, v.getBenchmarkingSpecification());
        else
            v.getBenchmarkingSpecification().setEnabled(false);
        return true;
    };
    
    public static final InPlaceXmlMarshaller<XmlTramoSeatsSpecification, TramoSeatsSpecification> MARSHALLER = (TramoSeatsSpecification v, XmlTramoSeatsSpecification xml ) -> {
        xml.preprocessing=new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(v.getTramoSpecification(), xml.preprocessing);
        xml.decomposition=new XmlSeatsSpec();
        XmlSeatsSpec.MARSHALLER.marshal(v.getSeatsSpecification(), xml.decomposition);
        xml.benchmarking=XmlCholetteSpec.MARSHALLER.marshal(v.getBenchmarkingSpecification());
        return true;
    };
}
