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
package demetra.sa.io.information;

import demetra.information.InformationSet;
import demetra.modelling.regular.SeriesSpec;
import demetra.timeseries.TimeSelector;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class SeriesSpecMapping {

    final String SPAN = "span", PRELIMINARYCHECK = "preliminarycheck";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SPAN), TimeSelector.class);
       dic.put(InformationSet.item(prefix, PRELIMINARYCHECK), Boolean.class);
    }

    InformationSet write(SeriesSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, spec.getSpan());
        }
         if (verbose || spec.isPreliminaryCheck() != SeriesSpec.DEF_CHECK) {
            info.add(PRELIMINARYCHECK, spec.isPreliminaryCheck());
        }
        return info;
    }

    SeriesSpec read(InformationSet info) {

        if (info == null) {
            return SeriesSpec.DEFAULT;
        }

        SeriesSpec.Builder builder = SeriesSpec.builder();
        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder.span(span);
        }
        Boolean preliminaryChecks = info.get(PRELIMINARYCHECK, Boolean.class);
        if (preliminaryChecks != null) {
            builder.preliminaryCheck(preliminaryChecks);
        }
        return builder.build();
    }

}
