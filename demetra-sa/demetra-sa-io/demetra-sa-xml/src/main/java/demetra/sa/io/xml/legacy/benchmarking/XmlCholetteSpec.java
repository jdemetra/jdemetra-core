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
package demetra.sa.io.xml.legacy.benchmarking;

import demetra.sa.benchmarking.SaBenchmarkingSpec;
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
    
    public boolean isDefault(){
        return this.method == null && this.target == null && this.userTarget == null;
    }
    
    public static SaBenchmarkingSpec unmarshal(XmlCholetteSpec xml) {
        if (xml == null){
            return SaBenchmarkingSpec.DEFAULT_DISABLED;
        }
        if (xml.isDefault())
            return SaBenchmarkingSpec.DEFAULT_ENABLED;
        SaBenchmarkingSpec.Builder builder = SaBenchmarkingSpec.builder();
        if (xml.method != null) {
            if (xml.method.getBiasCorrection() != null) {
                builder=builder.biasCorrection(xml.method.getBiasCorrection());
            }
            builder=builder.lambda(xml.method.getLambda())
                    .rho(xml.method.getRho());
         }
        if (xml.target != null)
            builder.target(xml.target);
        return builder.build();
    };
    
    public static final XmlCholetteSpec marshal(SaBenchmarkingSpec v) {
        if (!v.isEnabled()) {
            return null;
        }
        XmlCholetteSpec xml = new XmlCholetteSpec();
        XmlUnivariateCholetteMethod xc = new XmlUnivariateCholetteMethod();
        xc.setBiasCorrection(v.getBiasCorrection());
        xc.setLambda(v.getLambda());
        xc.setRho(v.getRho());
        xml.setMethod(xc);
        xml.setTarget(v.getTarget());
        return xml;
    };
}
