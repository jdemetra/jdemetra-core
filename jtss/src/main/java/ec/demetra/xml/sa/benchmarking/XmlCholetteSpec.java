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
package ec.demetra.xml.sa.benchmarking;

import ec.demetra.xml.benchmarking.XmlUnivariateCholetteMethod;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CholetteType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="CholetteType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/sa/benchmarking}BenchmarkingType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Method" type="{ec/eurostat/jdemetra/benchmarking}UnivariateCholetteMethodType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CholetteType", propOrder = {
    "method"
})
public class XmlCholetteSpec
        extends XmlBenchmarkingSpec {
    
    @XmlElement(name = "Method", required = true)
    protected XmlUnivariateCholetteMethod method;

    /**
     * Gets the value of the method property.
     *
     * @return possible object is {@link UnivariateCholetteMethodType }
     *
     */
    public XmlUnivariateCholetteMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value allowed object is {@link UnivariateCholetteMethodType }
     *
     */
    public void setMethod(XmlUnivariateCholetteMethod value) {
        this.method = value;
    }
    
    public static final InPlaceXmlUnmarshaller<XmlCholetteSpec, SaBenchmarkingSpec> UNMARSHALLER = (XmlCholetteSpec xml, SaBenchmarkingSpec v) -> {
        if (xml == null){
            v.setEnabled(false);
            return true;
        }
        v.setEnabled(true);
        if (xml.method != null) {
            if (xml.method.getBiasCorrection() != null) {
                v.setBias(xml.method.getBiasCorrection());
            }
            v.setLambda(xml.method.getLambda());
            v.setRho(xml.method.getRho());
         }
        if (xml.target != null)
            v.setTarget(xml.target);
        return true;
    };
    
    public static final IXmlMarshaller<XmlCholetteSpec, SaBenchmarkingSpec> MARSHALLER = (SaBenchmarkingSpec v) -> {
        if (!v.isEnabled()) {
            return null;
        }
        XmlCholetteSpec xml = new XmlCholetteSpec();
        XmlUnivariateCholetteMethod xc = new XmlUnivariateCholetteMethod();
        xc.setBiasCorrection(v.getBias());
        xc.setLambda(v.getLambda());
        xc.setRho(v.getRho());
        xml.setMethod(xc);
        xml.setTarget(v.getTarget());
        return xml;
    };
}
