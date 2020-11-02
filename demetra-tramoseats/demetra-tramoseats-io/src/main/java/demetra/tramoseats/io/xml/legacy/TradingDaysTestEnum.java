/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.xml.legacy;

import demetra.tramo.RegressionTestType;
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
@XmlType(name = "TradingDaysTestEnum")
@XmlEnum
public enum TradingDaysTestEnum {

    @XmlEnumValue("JointF")
    JOINT_F("JointF"),
    T("T");
    private final String value;

    TradingDaysTestEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TradingDaysTestEnum fromValue(String v) {
        for (TradingDaysTestEnum c : TradingDaysTestEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static class Adapter extends XmlAdapter<TradingDaysTestEnum, RegressionTestType> {

        @Override
        public RegressionTestType unmarshal(TradingDaysTestEnum v) throws Exception {
            if (v == null) {
                return RegressionTestType.None;
            }
            switch (v) {
                case JOINT_F:
                    return RegressionTestType.Joint_F;
                case T:
                    return RegressionTestType.Separate_T;
                default:
                    return RegressionTestType.None;
            }
        }

        @Override
        public TradingDaysTestEnum marshal(RegressionTestType v) throws Exception {
            switch (v) {
                case Joint_F:
                    return TradingDaysTestEnum.JOINT_F;
                case Separate_T:
                    return TradingDaysTestEnum.T;
                default:
                    return null;
            }

        }
    }
}
