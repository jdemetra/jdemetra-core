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

import demetra.data.Parameter;
import demetra.information.InformationSet;
import demetra.modelling.regular.EasterSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class EasterSpecMapping {

    final String TYPE = "type", PARAM = "param", TEST = "test", COEF = "coef";

    InformationSet write(EasterSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet easterInfo = new InformationSet();
        writeProperties(easterInfo, spec, verbose);
        Parameter coef = spec.getCoefficient();
        if (coef != null) {
            easterInfo.set(COEF, coef);
        }
        return easterInfo;
    }

    private void writeProperties(InformationSet easterInfo, EasterSpec spec, boolean verbose) {
        easterInfo.add(TYPE, spec.getType().name());
        if (verbose || spec.getDuration() != 0) {
            easterInfo.add(PARAM, spec.getDuration());
        }
        if (verbose || !spec.isTest()) {
            easterInfo.add(TEST, spec.isTest());
        }
    }

    EasterSpec read(InformationSet easterInfo) {
        if (easterInfo == null) {
            return EasterSpec.DEFAULT_UNUSED;
        }
        EasterSpec.Builder builder = EasterSpec.builder();
        readProperties(easterInfo, builder);
        Parameter c = easterInfo.get(COEF, Parameter.class);
        return builder.coefficient(c)
                .build();
    }

    private void readProperties(InformationSet easterInfo, EasterSpec.Builder builder) {
        String type = easterInfo.get(TYPE, String.class);
        EasterSpec.Type mtype = EasterSpec.Type.UNUSED;
        if (type != null) {
            mtype = EasterSpec.Type.valueOf(type);
        }
        Integer w = easterInfo.get(PARAM, Integer.class);
        Boolean test = easterInfo.get(TEST, Boolean.class);
        boolean rtest = false;
        if (test != null) {
            rtest = test;
        }

        builder.duration(w == null ? EasterSpec.DEF_IDUR : w)
                .type(mtype)
                .test(rtest);
    }
}
