/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.regression.TsContextVariable;
import demetra.toolkit.io.xml.legacy.core.XmlTsData;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * <p>
 * Java class for UserVariableType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="UserVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}ModifiableRegressionVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Variable" type="{http://www.w3.org/2001/XMLSchema}IDREF"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="effect" use="required" type="{ec/eurostat/jdemetra/core}ComponentEnum" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserVariableType", propOrder = {
    "data",
    "variable"
})
public class XmlUserVariable
        extends XmlModifiableRegressionVariable {

    @XmlElement(name = "Data")
    private XmlTsData data;
    @XmlElement(name = "Variable")
    @XmlSchemaType(name = "NMTOKEN")
    protected String variable;
    @XmlAttribute(name = "effect", required = true)
    protected String effect;

    /**
     * @return the data
     */
    public XmlTsData getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(XmlTsData data) {
        this.data = data;
    }

    /**
     * Gets the value of the variable property.
     *
     * @return possible object is {@link Object }
     *
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Sets the value of the variable property.
     *
     * @param value allowed object is {@link Object }
     *
     */
    public void setVariable(String value) {
        this.variable = value;
    }

    /**
     * Gets the value of the effect property.
     *
     * @return possible object is {@link ComponentEnum }
     *
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Sets the value of the effect property.
     *
     * @param value allowed object is {@link ComponentEnum }
     *
     */
    public void setEffect(String value) {
        this.effect = value;
    }

    // For compatiility with existing specifications
    public static class LegacyAdapter extends XmlAdapter<XmlUserVariable, TsContextVariable> {

        @Override
        public TsContextVariable unmarshal(XmlUserVariable v){
            TsContextVariable.Builder builder = TsContextVariable.builder().name(v.variable);
            if (v.effect != null) {
                builder.attribute(v.effect);
            }
            if (v.modifier != null) {
                if (v.modifier.size() == 1 && v.modifier.get(0) instanceof XmlLags) {
                    XmlLags lv = (XmlLags) v.modifier.get(0);
                    builder=builder.firstLag(lv.FirstLag).lastLag(lv.LastLag);
                }
            }
            return builder.build();
        }

        @Override
        public XmlUserVariable marshal(TsContextVariable v) {
            XmlUserVariable xvar = new XmlUserVariable();
            xvar.setVariable(v.getName());
            xvar.setEffect(v.getAttributes().get(0));
            if (v.getFirstLag() != 0 || v.getLastLag() != 0) {
                XmlLags xl = new XmlLags();
                xl.FirstLag = v.getFirstLag();
                xl.LastLag = v.getLastLag();
                xvar.getModifiers().add(xl);
            }
            return xvar;
        }

    }

    private static final LegacyAdapter LEGACY_ADAPTER = new LegacyAdapter();

    public static final LegacyAdapter getLegacyAdapter() {
        return LEGACY_ADAPTER;
    }
    
}
