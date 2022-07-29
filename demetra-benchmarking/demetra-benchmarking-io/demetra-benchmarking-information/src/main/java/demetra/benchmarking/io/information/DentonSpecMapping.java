/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.benchmarking.io.information;

import demetra.DemetraVersion;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class DentonSpecMapping {
    public final String MUL = "multiplicative", DIFF = "differencing", MOD = "modified", TYPE = "type", POS="position", FREQ = "defaultfrequency";
    
    public static final InformationSetSerializer<DentonSpec> SERIALIZER = new InformationSetSerializer<DentonSpec>() {
        @Override
        public InformationSet write(DentonSpec object, boolean verbose) {
            return DentonSpecMapping.write(object, verbose);
        }

        @Override
        public DentonSpec read(InformationSet info) {
            return DentonSpecMapping.read(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public DentonSpec read(InformationSet info) {
        if (info == null) {
            return DentonSpec.DEFAULT;
        }
        
        DentonSpec.Builder builder = DentonSpec.builder();
        Boolean mul=info.get(MUL, Boolean.class);
        if (mul != null)
            builder.multiplicative(mul);
        Integer diff=info.get(DIFF, Integer.class);
        if (diff != null)
            builder.differencing(diff);
        Boolean mod=info.get(MOD, Boolean.class);
        if (mod != null)
            builder.modified(mod);
        String type=info.get(TYPE, String.class);
        if (type != null)
            builder.aggregationType(AggregationType.valueOf(type));
        Integer p=info.get(POS, Integer.class);
        if (p != null)
            builder.observationPosition(p);
        Integer freq=info.get(FREQ, Integer.class);
        if (freq != null)
            builder.defaultPeriod(freq);
        return builder.build();
    }
    
    public InformationSet write(DentonSpec spec, boolean verbose) {
        InformationSet info =new InformationSet();
        info.set(MUL, spec.isMultiplicative());
        info.set(DIFF, spec.getDifferencing());
        info.set(MOD, spec.isModified());
        info.set(TYPE, spec.getAggregationType().name());
        info.set(POS, spec.getObservationPosition());
        info.set(FREQ, spec.getDefaultPeriod());
        return info;
    }
    
}
