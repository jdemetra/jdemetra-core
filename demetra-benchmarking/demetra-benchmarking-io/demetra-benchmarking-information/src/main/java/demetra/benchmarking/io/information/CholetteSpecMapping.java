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
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.data.AggregationType;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class CholetteSpecMapping {
    public final String LAMBDA = "lambda",
            RHO = "rho", BIAS="bias", TYPE = "type", FREQ = "defaultfrequency", POS="position";
    
    public static final InformationSetSerializer<CholetteSpec> SERIALIZER = new InformationSetSerializer<CholetteSpec>() {
        @Override
        public InformationSet write(CholetteSpec object, boolean verbose) {
            return CholetteSpecMapping.write(object, verbose);
        }

        @Override
        public CholetteSpec read(InformationSet info) {
            return CholetteSpecMapping.read(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public CholetteSpec read(InformationSet info) {
        if (info == null) {
            return CholetteSpec.DEFAULT;
        }
        
        CholetteSpec.Builder builder = CholetteSpec.builder();
        Double rho=info.get(RHO, Double.class);
        if (rho != null)
            builder.rho(rho);
        Double lambda=info.get(LAMBDA, Double.class);
        if (lambda != null)
            builder.lambda(lambda);
        String bias=info.get(BIAS, String.class);
        if (bias != null)
            builder.bias(CholetteSpec.BiasCorrection.valueOf(bias));
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
    
    public InformationSet write(CholetteSpec spec, boolean verbose) {
        InformationSet info =new InformationSet();
        info.set(RHO, spec.getRho());
        info.set(LAMBDA, spec.getLambda());
        info.set(BIAS, spec.getBias().name());
        info.set(TYPE, spec.getAggregationType().name());
        info.set(POS, spec.getObservationPosition());
        info.set(FREQ, spec.getDefaultPeriod());
        return info;
    }
    
}
