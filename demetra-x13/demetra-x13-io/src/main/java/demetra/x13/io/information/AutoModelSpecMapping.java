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
import demetra.regarima.AutoModelSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class AutoModelSpecMapping {

    final String ENABLED = "enabled",
            ACCEPTDEFAULT = "acceptdefault",
            MIXED = "mixed",
            BALANCED = "balanced",
            CHECKMU = "checkmu",
            HR = "hrinitial",
            LJUNGBOXLIMIT = "ljungboxlimit",
            REDUCECV = "reducecv",
            UB1 = "ub1",
            UB2 = "ub2",
            CANCEL = "cancel",
            ARMALIMIT = "armalimit",
            UBFINAL = "ubfinal",
            PERCENTRSE = "percentRSE",
            ARMA = "arma",
            DIFF = "diff";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, CANCEL), Double.class);
        dic.put(InformationSet.item(prefix, UB1), Double.class);
        dic.put(InformationSet.item(prefix, UB2), Double.class);
        dic.put(InformationSet.item(prefix, ARMALIMIT), Double.class);
        dic.put(InformationSet.item(prefix, UBFINAL), Double.class);
        dic.put(InformationSet.item(prefix, LJUNGBOXLIMIT), Double.class);
        dic.put(InformationSet.item(prefix, REDUCECV), Double.class);
        dic.put(InformationSet.item(prefix, PERCENTRSE), Double.class);
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, ACCEPTDEFAULT), Boolean.class);
        dic.put(InformationSet.item(prefix, MIXED), Boolean.class);
        dic.put(InformationSet.item(prefix, CHECKMU), Boolean.class);
        dic.put(InformationSet.item(prefix, BALANCED), Boolean.class);
        dic.put(InformationSet.item(prefix, HR), Boolean.class);
        OrderSpecMapping.fillDictionary(InformationSet.item(prefix, ARMA), dic);
        OrderSpecMapping.fillDictionary(InformationSet.item(prefix, DIFF), dic);
    }

    InformationSet write(AutoModelSpec spec, boolean verbose) {
        if (!spec.isEnabled()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (spec.isDefault()) {
            return info;
        }
        if (verbose || spec.isAcceptDefault() != AutoModelSpec.DEF_ACCEPTDEF) {
            info.add(ACCEPTDEFAULT, spec.isAcceptDefault());
        }
        if (verbose || spec.isMixed() != AutoModelSpec.DEF_MIXED) {
            info.add(MIXED, spec.isMixed());
        }
        if (verbose || spec.isBalanced() != AutoModelSpec.DEF_BALANCED) {
            info.add(BALANCED, spec.isBalanced());
        }
        if (verbose || spec.isHannanRissannen() != AutoModelSpec.DEF_HR) {
            info.add(HR, spec.isHannanRissannen());
        }
        if (verbose || spec.isCheckMu() != AutoModelSpec.DEF_CHECKMU) {
            info.add(CHECKMU, spec.isCheckMu());
        }
        if (verbose || spec.getLjungBoxLimit() != AutoModelSpec.DEF_LJUNGBOX) {
            info.add(LJUNGBOXLIMIT, spec.getLjungBoxLimit());
        }
        if (verbose || spec.getPredcv() != AutoModelSpec.DEF_PREDCV) {
            info.add(REDUCECV, spec.getPredcv());
        }
        if (verbose || spec.getUb1() != AutoModelSpec.DEF_UB1) {
            info.add(UB1, spec.getUb1());
        }
        if (verbose || spec.getUb2() != AutoModelSpec.DEF_UB2) {
            info.add(UB2, spec.getUb2());
        }
        if (verbose || spec.getCancel() != AutoModelSpec.DEF_CANCEL) {
            info.add(CANCEL, spec.getCancel());
        }
        if (verbose || spec.getArmaSignificance() != AutoModelSpec.DEF_TSIG) {
            info.add(ARMALIMIT, spec.getArmaSignificance());
        }
        if (verbose || spec.getUbfinal() != AutoModelSpec.DEF_UBFINAL) {
            info.add(UBFINAL, spec.getUbfinal());
        }
//        if (verbose || spec.g != AutoModelSpec.DEF_FCT) {
//            info.add(PERCENTRSE, fct_);
//        }
        if (verbose || spec.getOrder() != null) {
            if (spec.getOrder() != null) {
                InformationSet osinfo = OrderSpecMapping.write(spec.getOrder(), verbose);
                if (osinfo != null) {
                    info.add(ARMA, osinfo);
                }
            }
        }
        if (verbose || spec.getDiff() != null) {
            if (spec.getDiff() != null) {
                InformationSet osinfo = OrderSpecMapping.write(spec.getDiff(), verbose);
                if (osinfo != null) {
                    info.add(DIFF, osinfo);
                }
            }
        }
        return info;
    }

    AutoModelSpec read(InformationSet info) {
        if (info == null) {
            return AutoModelSpec.DEFAULT_DISABLED;
        }
        if (info.items().isEmpty()) {
            return AutoModelSpec.DEFAULT_ENABLED;
        }

        Boolean fal = info.get(ACCEPTDEFAULT, Boolean.class);
        Boolean mixed = info.get(MIXED, Boolean.class);
        Boolean balanced = info.get(BALANCED, Boolean.class);
        Boolean hr = info.get(HR, Boolean.class);
        Boolean mu = info.get(CHECKMU, Boolean.class);
        Double pcr = info.get(LJUNGBOXLIMIT, Double.class);
        Double pc = info.get(REDUCECV, Double.class);
        Double ub1 = info.get(UB1, Double.class);
        Double ub2 = info.get(UB2, Double.class);
        Double cancel = info.get(CANCEL, Double.class);
        Double tsig = info.get(ARMALIMIT, Double.class);
        Double ubf = info.get(UBFINAL, Double.class);
        return AutoModelSpec.builder()
                .enabled(true)
                .acceptDefault(fal == null ? AutoModelSpec.DEF_ACCEPTDEF : fal)
                .mixed(mixed == null ? AutoModelSpec.DEF_MIXED : mixed)
                .balanced(balanced == null ? AutoModelSpec.DEF_BALANCED : balanced)
                .hannanRissannen(hr == null ? AutoModelSpec.DEF_HR : hr)
                .checkMu(mu == null ? AutoModelSpec.DEF_CHECKMU : mu)
                .ljungBoxLimit(pcr == null ? AutoModelSpec.DEF_LJUNGBOX : pcr)
                .predcv(pc == null ? AutoModelSpec.DEF_PREDCV : pc)
                .ub1(ub1 == null ? AutoModelSpec.DEF_UB1 : ub1)
                .ub2(ub2 == null ? AutoModelSpec.DEF_UB2 : ub2)
                .cancel(cancel == null ? AutoModelSpec.DEF_CANCEL : cancel)
                .armaSignificance(tsig == null ? AutoModelSpec.DEF_TSIG : tsig)
                .ubfinal(ubf == null ? AutoModelSpec.DEF_UBFINAL : ubf)
                .order(OrderSpecMapping.read(info.getSubSet(ARMA)))
                .diff(OrderSpecMapping.read(info.getSubSet(DIFF)))
                .build();
    }

}
