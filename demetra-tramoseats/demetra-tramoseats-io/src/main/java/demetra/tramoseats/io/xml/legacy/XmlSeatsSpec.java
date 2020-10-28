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
package demetra.tramoseats.io.xml.legacy;

import demetra.sa.io.xml.legacy.XmlDecompositionSpec;
import demetra.seats.DecompositionSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>
 * Java class for SeatsSpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="SeatsSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/sa}DecompositionSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EpsPhi" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minInclusive value="0"/&gt;
 *               &lt;maxInclusive value="5.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RMod" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.0"/&gt;
 *               &lt;maxExclusive value="1.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SMod" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.0"/&gt;
 *               &lt;maxExclusive value="1.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="StSMod" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.0"/&gt;
 *               &lt;maxExclusive value="1.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="XL" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.5"/&gt;
 *               &lt;maxExclusive value="1.0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="ForceModel" type="{ec/eurostat/jdemetra/sa/tramoseats}ApproximationModeEnum" minOccurs="0"/&gt;
 *         &lt;element name="Method" type="{ec/eurostat/jdemetra/sa/tramoseats}SeatsEstimationMethodEnum" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SeatsSpecType", propOrder = {
    "epsPhi",
    "rMod",
    "sMod",
    "stSMod",
    "xl",
    "forceModel",
    "method"
})
public class XmlSeatsSpec
        extends XmlDecompositionSpec {

    @XmlElement(name = "EpsPhi", defaultValue = "2.0")
    protected Double epsPhi;
    @XmlElement(name = "RMod", defaultValue = "0.5")
    protected Double rMod;
    @XmlElement(name = "SMod", defaultValue = "0.8")
    protected Double sMod;
    @XmlElement(name = "StSMod", defaultValue = "0.8")
    protected Double stSMod;
    @XmlElement(name = "XL", defaultValue = "0.95")
    protected Double xl;
    @XmlElement(name = "ForceModel", defaultValue = "Legacy")
    @XmlSchemaType(name = "NMTOKEN")
    protected DecompositionSpec.ModelApproximationMode forceModel;
    @XmlElement(name = "Method", defaultValue = "Burman")
    @XmlSchemaType(name = "NMTOKEN")
    protected DecompositionSpec.ComponentsEstimationMethod method;

    /**
     * Gets the value of the epsPhi property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getEpsPhi() {
        return epsPhi;
    }

    /**
     * Sets the value of the epsPhi property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setEpsPhi(Double value) {
        if (value != null && value == DecompositionSpec.DEF_EPSPHI) {
            this.epsPhi = null;
        } else {
            this.epsPhi = value;
        }
    }

    /**
     * Gets the value of the rMod property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getRMod() {
        return rMod;
    }

    /**
     * Sets the value of the rMod property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setRMod(Double value) {
        if (value != null && value == DecompositionSpec.DEF_RMOD) {
            this.rMod = null;
        } else {
            this.rMod = value;
        }
    }

    /**
     * Gets the value of the sMod property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getSMod() {
        return sMod;
    }

    /**
     * Sets the value of the sMod property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setSMod(Double value) {
        if (value != null && value == DecompositionSpec.DEF_SMOD) {
            this.sMod = null;
        } else {
            this.sMod = value;
        }
    }

    /**
     * Gets the value of the stSMod property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getStSMod() {
        return stSMod;
    }

    /**
     * Sets the value of the stSMod property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setStSMod(Double value) {
        if (value != null && value == DecompositionSpec.DEF_SMOD1) {
            this.stSMod = null;
        } else {
            this.stSMod = value;
        }
    }

    /**
     * Gets the value of the xl property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getXL() {
        return xl;
    }

    /**
     * Sets the value of the xl property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setXL(Double value) {
        if (value != null && value == DecompositionSpec.DEF_XL) {
            this.xl = null;
        } else {
            this.xl = value;
        }
    }

    /**
     * Gets the value of the forceModel property.
     *
     * @return possible object is {@link ApproximationModeEnum }
     *
     */
    public DecompositionSpec.ModelApproximationMode getForceModel() {
        return forceModel;
    }

    /**
     * Sets the value of the forceModel property.
     *
     * @param value allowed object is {@link ApproximationModeEnum }
     *
     */
    public void setForceModel(DecompositionSpec.ModelApproximationMode value) {
        if (value == DecompositionSpec.ModelApproximationMode.Legacy) {
            this.forceModel = null;
        } else {
            this.forceModel = value;
        }
    }

    /**
     * Gets the value of the method property.
     *
     * @return possible object is {@link SeatsEstimationMethodEnum }
     *
     */
    public DecompositionSpec.ComponentsEstimationMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value allowed object is {@link SeatsEstimationMethodEnum }
     *
     */
    public void setMethod(DecompositionSpec.ComponentsEstimationMethod value) {
        if (value == DecompositionSpec.ComponentsEstimationMethod.Burman) {
            this.method = null;
        } else {
            this.method = value;
        }
    }

    public boolean isDefault() {
        return epsPhi == null && rMod == null && sMod == null && stSMod == null && xl == null && forceModel == null && method == null;
    }

    public static DecompositionSpec unmarshal(XmlSeatsSpec xml) {
        if (xml == null || xml.isDefault())
            return DecompositionSpec.DEFAULT;
            
        DecompositionSpec.Builder builder = DecompositionSpec.builder();
        if (xml.epsPhi != null) {
            builder = builder.seasTolerance(xml.epsPhi);
        }
        if (xml.rMod != null) {
            builder = builder.trendBoundary(xml.rMod);
        }
        if (xml.sMod != null) {
            builder = builder.seasBoundary(xml.sMod);
        }
        if (xml.stSMod != null) {
            builder = builder.seasBoundaryAtPi(xml.stSMod);
        }
        if (xml.xl != null) {
            builder = builder.xlBoundary(xml.xl);
        }
        if (xml.forceModel != null) {
            builder = builder.approximationMode(xml.forceModel);
        }
        if (xml.method != null) {
            builder = builder.method(xml.method);
        }
        return builder.build();
    }

    public static boolean marshal(@NonNull DecompositionSpec v, @NonNull XmlSeatsSpec xml){
        if (v.isDefault()) {
            return true;
        }
        xml.setEpsPhi(v.getSeasTolerance());
        xml.setRMod(v.getTrendBoundary());
        xml.setSMod(v.getSeasBoundary());
        xml.setStSMod(v.getSeasBoundaryAtPi());
        xml.setXL(v.getXlBoundary());
        xml.setForceModel(v.getApproximationMode());
        xml.setMethod(v.getMethod());
        return true;
    };

}
