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

import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for BenchmarkingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BenchmarkingType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Target" type="{ec/eurostat/jdemetra/sa/benchmarking}TargetEnum"/&gt;
 *           &lt;element name="UserTarget" type="{http://www.w3.org/2001/XMLSchema}IDREF"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BenchmarkingType", propOrder = {
    "target",
    "userTarget"
})
@XmlSeeAlso({
})
public abstract class XmlBenchmarkingSpec {

    @XmlElement(name = "Target")
    @XmlSchemaType(name = "NMTOKEN")
    protected SaBenchmarkingSpec.Target target;
    @XmlElement(name = "UserTarget")
    @XmlSchemaType(name = "NMTOKEN")
    protected String userTarget;

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link TargetEnum }
     *     
     */
    public SaBenchmarkingSpec.Target getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetEnum }
     *     
     */
    public void setTarget(SaBenchmarkingSpec.Target value) {
        this.target = value;
    }

    /**
     * Gets the value of the userTarget property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getUserTarget() {
        return userTarget;
    }

    /**
     * Sets the value of the userTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setUserTarget(String value) {
        this.userTarget = value;
    }

}
