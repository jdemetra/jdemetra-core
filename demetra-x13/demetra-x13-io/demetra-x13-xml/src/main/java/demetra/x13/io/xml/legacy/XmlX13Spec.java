/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.xml.legacy;

import demetra.regarima.RegArimaSpec;
import demetra.sa.io.xml.legacy.XmlSaSpecification;
import demetra.sa.io.xml.legacy.benchmarking.XmlCholetteSpec;
import demetra.x13.X13Spec;
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
@XmlRootElement(name = "X13Specification")
@XmlType(name = "X13SpecificationType", propOrder = {
    "preprocessing",
    "decomposition",
    "benchmarking"
})
public class XmlX13Spec
        extends XmlSaSpecification {

    @XmlElement(name = "Preprocessing")
    protected XmlRegArimaSpec preprocessing;
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
    public XmlRegArimaSpec getPreprocessing() {
        return preprocessing;
    }

    /**
     * Sets the value of the preprocessing property.
     *
     * @param value allowed object is {@link RegArimaSpecificationType }
     *
     */
    public void setPreprocessing(XmlRegArimaSpec value) {
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

    public static X13Spec unmarshal(XmlX13Spec xml) {
        X13Spec.Builder builder = X13Spec.builder();
        if (xml.preprocessing != null) {
            builder = builder.regArima(XmlRegArimaSpec.unmarshal(xml.preprocessing));
        } else {
            builder.regArima(RegArimaSpec.DEFAULT_DISABLED);
        }
        if (xml.decomposition != null) {
            builder = builder.x11(XmlX11Spec.unmarshal(xml.decomposition));
        }
        if (xml.benchmarking != null) {
            builder = builder.benchmarking(XmlCholetteSpec.unmarshal(xml.benchmarking));
        }
        return builder.build();
    }

    public static XmlX13Spec marshal(X13Spec v) {
        XmlX13Spec xml = new XmlX13Spec();
        xml.preprocessing = XmlRegArimaSpec.marshal(v.getRegArima());
        xml.decomposition = XmlX11Spec.marshal(v.getX11());
        xml.benchmarking = XmlCholetteSpec.marshal(v.getBenchmarking());
        return xml;
    }
}
