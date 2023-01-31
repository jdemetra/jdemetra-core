/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stl.io.information;

import demetra.data.WeightFunction;
import demetra.information.InformationSet;
import demetra.stl.StlSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class StlSpecMapping {

    public final String MUL = "mul", INNER = "innerloops", OUTER = "outerloops",
            RTHRESHOLD = "robustthreshold", RWEIGHTS = "robustweights", TREND = "trend", SEAS = "seas";

    InformationSet write(StlSpec spec, boolean verbose) {
        if (spec == null) {
            return null;
        }
        InformationSet info = new InformationSet();
        info.set(MUL, spec.isMultiplicative());
        info.set(INNER, spec.getInnerLoopsCount());
        info.set(OUTER, spec.getOuterLoopsCount());
        if (verbose || spec.getRobustWeightThreshold() != StlSpec.RWTHRESHOLD) {
            info.set(RTHRESHOLD, spec.getRobustWeightThreshold());
        }
        if (verbose || spec.getRobustWeightFunction() != StlSpec.RWFUNCTION) {
            info.set(RWEIGHTS, spec.getRobustWeightFunction().name());
        }
        info.set(TREND, LoessSpecMapping.write(spec.getTrendSpec(), verbose));
        info.set(SEAS, SeasonalSpecMapping.write(spec.getSeasonalSpec(), verbose));
        return info;
    }

    StlSpec read(InformationSet info) {
        if (info == null) {
            return null;
        }
        StlSpec.Builder builder = StlSpec.builder()
                .multiplicative(info.get(MUL, Boolean.class))
                .innerLoopsCount(info.get(INNER, Integer.class))
                .outerLoopsCount(info.get(OUTER, Integer.class))
                .trendSpec(LoessSpecMapping.read(info.getSubSet(TREND)))
                .seasonalSpec(SeasonalSpecMapping.read(info.getSubSet(SEAS)));

        Double rt = info.get(RTHRESHOLD, Double.class);
        if (rt != null) {
            builder.robustWeightThreshold(rt);
        }
        String rfn = info.get(RWEIGHTS, String.class);
        if (rfn != null) {
            builder.robustWeightFunction(WeightFunction.valueOf(rfn));
        }
        return builder.build();
    }

}
