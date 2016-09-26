/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.regression;

import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.TsVariableDescriptor.UserComponentType;
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
    "variable"
})
public class XmlUserVariable
        extends XmlModifiableRegressionVariable {

    @XmlElement(name = "Variable", required = true)
    @XmlSchemaType(name = "IDREF")
    protected String variable;
    @XmlAttribute(name = "effect", required = true)
    protected UserComponentType effect;

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
    public UserComponentType getEffect() {
        return effect;
    }

    /**
     * Sets the value of the effect property.
     *
     * @param value allowed object is {@link ComponentEnum }
     *
     */
    public void setEffect(UserComponentType value) {
        this.effect = value;
    }

    // For compatiility with existing specifications
    public static class LegacyAdapter extends XmlAdapter<XmlUserVariable, TsVariableDescriptor> {

        @Override
        public TsVariableDescriptor unmarshal(XmlUserVariable v){
            TsVariableDescriptor desc = new TsVariableDescriptor();
            desc.setName(v.variable);
            if (v.effect != null) {
                desc.setEffect(v.effect);
            }
            if (v.modifier != null) {
                if (v.modifier.size() == 1 && v.modifier.get(0) instanceof XmlLaggedVariable) {
                    XmlLaggedVariable lv = (XmlLaggedVariable) v.modifier.get(0);
                    desc.setLags(lv.FirstLag, lv.LastLag);
                }
            }
            return desc;
        }

        @Override
        public XmlUserVariable marshal(TsVariableDescriptor v) {
            XmlUserVariable xvar = new XmlUserVariable();
            xvar.setVariable(v.getName());
            xvar.setEffect(v.getEffect());
            if (v.getFirstLag() != 0 || v.getLastLag() != 0) {
                XmlLaggedVariable xl = new XmlLaggedVariable();
                xl.FirstLag = v.getFirstLag();
                xl.LastLag = v.getLastLag();
                xvar.getModifiers().add(xl);
            }
            return xvar;
        }

    }

    private static final LegacyAdapter DESC_ADAPTER = new LegacyAdapter();

    public static final LegacyAdapter getLegacyAdapter() {
        return DESC_ADAPTER;
    }
}
