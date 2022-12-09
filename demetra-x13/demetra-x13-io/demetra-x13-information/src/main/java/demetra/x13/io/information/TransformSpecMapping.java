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
package demetra.x13.io.information;

import demetra.information.InformationSet;
import demetra.modelling.TransformationType;
import demetra.regarima.TransformSpec;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class TransformSpecMapping {

    final String FN = "function",
            ADJUST = "adjust",
            //            UNITS = "units",
            AICDIFF = "aicdiff",
            OUTLIERS="outliers",
            //POWER = "power",
            CONST = "const";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ADJUST), String.class);
        dic.put(InformationSet.item(prefix, AICDIFF), Double.class);
        dic.put(InformationSet.item(prefix, FN), String.class);
        dic.put(InformationSet.item(prefix, CONST), Double.class);
    }

    InformationSet write(TransformSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getFunction() != TransformationType.None) {
            info.add(FN, spec.getFunction().name());
        }
        if (verbose || spec.getAdjust() != TransformSpec.DEF_ADJUST) {
            info.add(ADJUST, spec.getAdjust().name());
        }
        if (verbose || spec.getAicDiff() != TransformSpec.DEF_AICDIFF) {
            info.add(AICDIFF, spec.getAicDiff());
        }
        if (verbose || spec.isOutliersCorrection() != TransformSpec.DEF_OUTLIERS) {
            info.add(OUTLIERS, spec.isOutliersCorrection());
        }
        if (verbose || spec.getConstant() != 0) {
            info.add(CONST, spec.getConstant());
        }
        return info;
    }

    TransformSpec read(InformationSet info) {
        if (info == null) {
            return TransformSpec.DEFAULT;
        }
        TransformSpec.Builder builder = TransformSpec.builder();
        String fn = info.get(FN, String.class);
        if (fn != null) {
            builder = builder.function(TransformationType.valueOf(fn));
        }
        String adjust = info.get(ADJUST, String.class);
        if (adjust != null) {
            builder.adjust(LengthOfPeriodType.valueOf(adjust));
        }
        Double aic = info.get(AICDIFF, Double.class);
        if (aic != null) {
            builder.aicDiff(aic);
        }
        Boolean outliers = info.get(OUTLIERS, Boolean.class);
        if (outliers != null) {
            builder = builder.outliersCorrection(outliers);
        }
        Double cnt = info.get(CONST, Double.class);
        if (cnt != null) {
            builder.constant(cnt);
        }
        return builder.build();
    }

}
