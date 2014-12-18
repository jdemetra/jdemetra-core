/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.disaggregation.documents;

import ec.tss.disaggregation.processors.DentonProcessor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsAggregationType;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class DentonSpecification implements IProcSpecification, Cloneable {

    public static final String MUL = "multiplicative", DIFF = "differencing", MOD = "modified", TYPE = "type";

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(mul_ ? "Mul. " : "Add. ").append("denton ");
        builder.append(" (D=").append(diff_).append(", mod=").append(mod_).append(')');
        return builder.toString();
    }
    
    private boolean mul_ = true, mod_ = true;
    private int diff_ = 1;
    private TsAggregationType type_ = TsAggregationType.Average;

    public boolean isMultiplicative() {
        return mul_;
    }

    public void setMultiplicative(boolean mul) {
        this.mul_ = mul;
    }

    public boolean isModifiedDenton() {
        return mod_;
    }

    public void setModifiedDenton(boolean mod) {
        this.mod_ = mod;
    }

    public int getDifferencingOrder() {
        return diff_;
    }

    public void setDifferencingOrder(int diff) {
        this.diff_ = diff;
    }

    public TsAggregationType getAggregationType() {
        return type_;
    }

    public void setAggregationType(TsAggregationType type) {
        type_ = type;
    }

    @Override
    public DentonSpecification clone() {
        try {
            return (DentonSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ALGORITHM, DentonProcessor.DESCRIPTOR);
        info.set(MUL, mul_);
        if (!mod_ || verbose) {
            info.set(MOD, mod_);
        }
        if (diff_ != 1 || verbose) {
            info.set(DIFF, diff_);
        }
        if (type_ != TsAggregationType.Sum || verbose) {
            info.set(TYPE, type_.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        Boolean mul = info.get(MUL, Boolean.class);
        if (mul != null) {
            mul_ = mul;
        }
        Boolean mod = info.get(MOD, Boolean.class);
        if (mod != null) {
            mod_ = mod;
        }
        Integer diff = info.get(DIFF, Integer.class);
        if (diff != null) {
            diff_ = diff;
        }
        String type = info.get(TYPE, String.class);
        if (type != null) {
            type_ = TsAggregationType.valueOf(type);
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DentonSpecification && equals((DentonSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.mul_ ? 1 : 0);
        hash = 13 * hash + (this.mod_ ? 1 : 0);
        hash = 13 * hash + this.diff_;
        hash = 13 * hash + Objects.hashCode(this.type_);
        return hash;
    }

    public boolean equals(DentonSpecification spec) {
        return spec.mod_ == mod_ && spec.mul_ == mul_ && spec.diff_ == diff_ && spec.type_ == type_;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, MUL), Boolean.class);
        dic.put(InformationSet.item(prefix, MOD), Boolean.class);
        dic.put(InformationSet.item(prefix, DIFF), Integer.class);
        dic.put(InformationSet.item(prefix, TYPE), String.class);
    }
}
