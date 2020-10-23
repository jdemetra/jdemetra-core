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
import demetra.timeseries.TimeSelector;
import demetra.tramo.OutlierSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OutliersMapping {
    public final String SPAN = "span",
            TYPES = "types",
            VA = "va",
            EML = "eml",
            DELTATC = "deltatc";
    
    public InformationSet write(OutlierSpec spec,boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        TimeSelector span = spec.getSpan();
        if (verbose || span.getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, span);
        }
//        if (!types_.isEmpty()) {
//            String[] types = new String[types_.size()];
//            for (int i = 0; i < types.length; ++i) {
//                types[i] = types_.get(i).name();
//            }
//            info.add(TYPES, types);
//        }
        double cv = spec.getCriticalValue();
        if (verbose || cv != 0) {
            info.add(VA, cv);
        }
        boolean eml = spec.isMaximumLikelihood();
        if (verbose || eml != OutlierSpec.DEF_EML) {
            info.add(EML, eml);
        }
        double tc = spec.getDeltaTC();
        if (verbose || tc != OutlierSpec.DEF_DELTATC) {
            info.add(DELTATC, tc);
        }
        return info;
    }
}
