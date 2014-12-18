/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tss.disaggregation.documents;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class UniCholetteSpecification extends CholetteSpecification implements Cloneable {

    public static final String FREQ = "freq", TYPE = "type";

    private TsFrequency agg_ = TsFrequency.Undefined;
    private TsAggregationType type_ = TsAggregationType.Average;
    

   @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("Cholette ");
        builder.append(" (rho=").append(getRho()).append(", lambda=").append(getLambda()).append(')');
        return builder.toString();
    }

    @Override
    public UniCholetteSpecification clone() {
        return (UniCholetteSpecification) super.clone();
    }

    public boolean equals(UniCholetteSpecification other) {
        return super.equals(other) && 
                agg_== other.agg_ && type_ == other.type_;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof UniCholetteSpecification && equals((UniCholetteSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 67 * hash + (this.getAggregationFrequency() != null ? this.getAggregationFrequency().hashCode() : 0);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = super.write(verbose);
        if (info == null) {
            return null;
        }
        info.set(FREQ, agg_.intValue());
        info.set(TYPE, type_.name());
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (!super.read(info)) {
            return false;
        }
        Integer f = info.get(FREQ, Integer.class);
        if (f != null) {
            agg_=TsFrequency.valueOf(f);
        }
        String t = info.get(TYPE, String.class);
        if (t != null) {
            type_=TsAggregationType.valueOf(t);
        }
        return true;
    }

    /**
     * @return the agg
     */
    public TsFrequency getAggregationFrequency() {
        return agg_;
    }

    /**
     * @param agg the agg to set
     */
    public void setAggregationFrequency(TsFrequency agg) {
        this.agg_ = agg;
    }

     public TsAggregationType getAggregationType() {
        return type_;
    }

    public void setAggregationType(TsAggregationType type) {
        type_ = type;
    }

   public static void fillDictionary(String prefix, Map<String, Class> dic) {
        CholetteSpecification.fillDictionary(prefix, dic);
        dic.put(FREQ, Integer.class);
        dic.put(TYPE, String.class);
    }
}
