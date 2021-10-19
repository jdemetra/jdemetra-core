/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.xml.legacy;

import demetra.regarima.RegressionTestSpec;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * <p>
 * Java class for TradingDaysTestEnum.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * <
 * pre>
 * &lt;simpleType name="TradingDaysTestEnum"&gt; &lt;restriction
 * base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt; &lt;enumeration
 * value="JointF"/&gt; &lt;enumeration value="T"/&gt; &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 *
 */
@XmlType(name = "RegressionTestEnum")
@XmlEnum
public enum RegressionTestEnum {

    @XmlEnumValue("None")
    NONE("None"),
    ADD("Add"),
    REMOVE("Remove");
    private final String value;

    RegressionTestEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RegressionTestEnum fromValue(String v) {
        for (RegressionTestEnum c : RegressionTestEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static class Adapter extends XmlAdapter<RegressionTestEnum, RegressionTestSpec> {

        @Override
        public RegressionTestSpec unmarshal(RegressionTestEnum v) throws Exception {
            if (v == null) {
                return RegressionTestSpec.None;
            }
            switch (v) {
                case ADD:
                    return RegressionTestSpec.Add;
                case REMOVE:
                    return RegressionTestSpec.Remove;
                default:
                    return RegressionTestSpec.None;
            }
        }

        @Override
        public RegressionTestEnum marshal(RegressionTestSpec v) throws Exception {
            switch (v) {
                case Add:
                    return RegressionTestEnum.ADD;
                case Remove:
                    return RegressionTestEnum.REMOVE;
                default:
                    return null;
            }

        }
    }
}
