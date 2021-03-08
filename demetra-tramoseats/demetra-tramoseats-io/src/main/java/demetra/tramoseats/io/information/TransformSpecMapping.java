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
package demetra.tramoseats.io.information;

import demetra.information.InformationSet;
import demetra.modelling.TransformationType;
import demetra.timeseries.TimeSelector;
import demetra.tramo.TransformSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TransformSpecMapping {

    public final String SPAN = "span",
            FN = "function",
            FCT = "fct",
            UNITS = "units",
            PRELIMINARYCHECK = "preliminarycheck";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, FN), String.class);
        dic.put(InformationSet.item(prefix, FCT), Double.class);
        dic.put(InformationSet.item(prefix, UNITS), Boolean.class);
        dic.put(InformationSet.item(prefix, SPAN), TimeSelector.class);
    }

    public InformationSet write(TransformSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, spec.getSpan());
        }
        if (verbose || spec.getFunction() != TransformationType.None) {
            info.add(FN, spec.getFunction().name());
        }
        if (verbose || spec.getFct() != TransformSpec.DEF_FCT) {
            info.add(FCT, spec.getFct());
        }
        if (verbose || !spec.isPreliminaryCheck()) {
            info.add(PRELIMINARYCHECK, spec.isPreliminaryCheck());
        }
        return info;
    }

    public TransformSpec read(InformationSet info) {
        if (info == null) {
            return TransformSpec.DEFAULT;
        }
        TransformSpec.Builder builder = TransformSpec.builder();
        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder = builder.span(span);
        }
        String fn = info.get(FN, String.class);
        if (fn != null) {
            builder = builder.function(TransformationType.valueOf(fn));
        }
        Double fct = info.get(FCT, Double.class);
        if (fct != null) {
            builder = builder.fct(fct);
        }
        Boolean preliminaryChecks = info.get(PRELIMINARYCHECK, Boolean.class);
        if (preliminaryChecks != null) {
            builder = builder.preliminaryCheck(preliminaryChecks);
        }
        return builder.build();
    }

}
