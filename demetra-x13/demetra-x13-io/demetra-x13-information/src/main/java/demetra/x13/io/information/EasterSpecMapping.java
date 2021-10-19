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

import demetra.data.Parameter;
import demetra.information.InformationSet;
import demetra.regarima.ChangeOfRegimeSpec;
import demetra.regarima.EasterSpec;
import demetra.regarima.RegressionTestSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class EasterSpecMapping {

    final String TYPE = "type", PARAM = "param", TEST = "test", CHANGEOFREGIME = "changeofregime", COEF = "coef";

    String varName() {
        return "easter";
    }

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, PARAM), Integer.class);
        dic.put(InformationSet.item(prefix, TEST), String.class);
        dic.put(InformationSet.item(prefix, CHANGEOFREGIME), String.class);
        dic.put(InformationSet.item(prefix, COEF), Parameter.class);
    }

    void writeLegacy(InformationSet regInfo, EasterSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return;
        }
        InformationSet easterInfo = regInfo.subSet(RegressionSpecMapping.MH + 1);
        writeProperties(easterInfo, spec, verbose);
        Parameter coef = spec.getCoefficient();
        RegressionSpecMapping.set(regInfo, varName(), coef);
    }

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
        if (verbose || spec.getTest() != RegressionTestSpec.None) {
            easterInfo.add(TEST, spec.getTest().name());
        }
//        if (spec.getChangeOfRegime() != null) {
//            easterInfo.add(CHANGEOFREGIME, spec.getChangeOfRegime().toString());
//        }
    }

    EasterSpec readLegacy(InformationSet regInfo) {
        InformationSet easterInfo = regInfo.getSubSet(RegressionSpecMapping.MH + 1);
        if (easterInfo == null) {
            return EasterSpec.DEFAULT_UNUSED;
        }
        EasterSpec.Builder builder = EasterSpec.builder();
        readProperties(easterInfo, builder);
        return builder.coefficient(RegressionSpecMapping.coefficientOf(regInfo, varName()))
                .build();
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
        EasterSpec.Type mtype = EasterSpec.Type.Unused;
        if (type != null) {
            mtype = EasterSpec.Type.valueOf(type);
        }
        Integer w = easterInfo.get(PARAM, Integer.class);
        String test = easterInfo.get(TEST, String.class);
        RegressionTestSpec rtest = RegressionTestSpec.None;
        if (test != null) {
            rtest = RegressionTestSpec.valueOf(test);
        }
        String cr = easterInfo.get(CHANGEOFREGIME, String.class);
        ChangeOfRegimeSpec cor = null;
        if (cr != null) {
            cor = ChangeOfRegimeSpec.fromString(cr);
        }

        builder.duration(w == null ? EasterSpec.DEF_EASTERDUR : w)
                .type(mtype)
                .test(rtest)
//                .changeOfRegime(cor)
                ;
    }
}
