/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.xml.legacy;

import demetra.sa.io.xml.legacy.XmlSaSpecification;
import ec.demetra.xml.sa.benchmarking.XmlCholetteSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for X13SpecificationType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="X13SpecificationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/sa}SaSpecificationType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Preprocessing" type="{ec/eurostat/jdemetra/sa/x13}RegArimaSpecificationType" minOccurs="0"/&gt;
 *         &lt;element name="Decomposition" type="{ec/eurostat/jdemetra/sa/x13}X11SpecType"/&gt;
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
@XmlRootElement(name="X13Specification")
@XmlType(name = "X13SpecificationType", propOrder = {
    "preprocessing",
    "decomposition",
    "benchmarking"
})
public class XmlX13Specification
        extends XmlSaSpecification {
    
    @XmlElement(name = "Preprocessing")
    protected XmlRegArimaSpecification preprocessing;
    @XmlElement(name = "Decomposition", required = true)
    protected XmlX11Spec decomposition;
    @XmlElement(name = "Benchmarking")
    protected XmlCholetteSpec benchmarking;

    /**
     * Gets the value of the preprocessing property.
     *
     * @return possible object is {@link RegArimaSpecificationType }
     *
     */
    public XmlRegArimaSpecification getPreprocessing() {
        return preprocessing;
    }

    /**
     * Sets the value of the preprocessing property.
     *
     * @param value allowed object is {@link RegArimaSpecificationType }
     *
     */
    public void setPreprocessing(XmlRegArimaSpecification value) {
        this.preprocessing = value;
    }

    /**
     * Gets the value of the decomposition property.
     *
     * @return possible object is {@link X11SpecType }
     *
     */
    public XmlX11Spec getDecomposition() {
        return decomposition;
    }

    /**
     * Sets the value of the decomposition property.
     *
     * @param value allowed object is {@link X11SpecType }
     *
     */
    public void setDecomposition(XmlX11Spec value) {
        this.decomposition = value;
    }

    /**
     * Gets the value of the benchmarking property.
     *
     * @return possible object is {@link CholetteType }
     *
     */
    public XmlCholetteSpec getBenchmarking() {
        return benchmarking;
    }

    /**
     * Sets the value of the benchmarking property.
     *
     * @param value allowed object is {@link CholetteType }
     *
     */
    public void setBenchmarking(XmlCholetteSpec value) {
        this.benchmarking = value;
    }
    
    public static final InPlaceXmlUnmarshaller<XmlX13Specification, X13Specification> UNMARSHALLER = (XmlX13Specification xml, X13Specification v) -> {
        if (xml.preprocessing != null) {
            XmlRegArimaSpecification.UNMARSHALLER.unmarshal(xml.preprocessing, v.getRegArimaSpecification());
        } else {
            v.getRegArimaSpecification().getBasic().setPreprocessing(false);
        }
        if (xml.decomposition != null) {
            XmlX11Spec.UNMARSHALLER.unmarshal(xml.decomposition, v.getX11Specification());
        }
        if (xml.benchmarking != null) {
            XmlCholetteSpec.UNMARSHALLER.unmarshal(xml.benchmarking, v.getBenchmarkingSpecification());
        } else {
            v.getBenchmarkingSpecification().setEnabled(false);
        }
        return true;
    };
    
    public static final InPlaceXmlMarshaller<XmlX13Specification, X13Specification> MARSHALLER = (X13Specification v, XmlX13Specification xml) -> {
        if (v.getRegArimaSpecification().getBasic().isPreprocessing()) {
            xml.preprocessing = new XmlRegArimaSpecification();
            XmlRegArimaSpecification.MARSHALLER.marshal(v.getRegArimaSpecification(), xml.preprocessing);
        }
        xml.decomposition = new XmlX11Spec();
        XmlX11Spec.MARSHALLER.marshal(v.getX11Specification(), xml.decomposition);
        xml.benchmarking = XmlCholetteSpec.MARSHALLER.marshal(v.getBenchmarkingSpecification());
        return true;
    };
}
