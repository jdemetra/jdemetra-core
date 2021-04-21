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
import demetra.tramo.AutoModelSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class AutoModelSpecMapping {

    final String ENABLED = "enabled",
            PCR = "pcr",
            UB1 = "ub1",
            UB2 = "ub2",
            TSIG = "tsig",
            PC = "pc",
            CANCEL = "cancel",
            FAL = "fal",
            AMICOMPARE = "compare";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, CANCEL), Double.class);
        dic.put(InformationSet.item(prefix, UB1), Double.class);
        dic.put(InformationSet.item(prefix, UB2), Double.class);
        dic.put(InformationSet.item(prefix, TSIG), Double.class);
        dic.put(InformationSet.item(prefix, PC), Double.class);
        dic.put(InformationSet.item(prefix, PCR), Double.class);
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, FAL), Boolean.class);
        dic.put(InformationSet.item(prefix, AMICOMPARE), Boolean.class);
    }

    InformationSet write(AutoModelSpec spec, boolean verbose) {
        if (!spec.isEnabled()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (spec.isDefault())
            return info;
        // ALWAYS ENABLED
//        info.set(ENABLED, true);
//        if (spec.isDefault()) {
//            return info;
//        }
        double pcr = spec.getPcr();
        if (verbose || pcr != AutoModelSpec.DEF_PCR) {
            info.set(PCR, pcr);
        }
        double pc = spec.getPc();
        if (verbose || pc != AutoModelSpec.DEF_PC) {
            info.set(PC, pc);
        }
        double ub1 = spec.getUb1();
        if (verbose || ub1 != AutoModelSpec.DEF_UB1) {
            info.set(UB1, ub1);
        }
        double ub2 = spec.getUb2();
        if (verbose || ub2 != AutoModelSpec.DEF_UB2) {
            info.set(UB2, ub2);
        }
        double cancel = spec.getCancel();
        if (verbose || cancel != AutoModelSpec.DEF_CANCEL) {
            info.set(CANCEL, cancel);
        }
        boolean fal = spec.isAcceptDefault();
        if (verbose || fal != AutoModelSpec.DEF_FAL) {
            info.set(FAL, fal);
        }
        boolean amiCompare = spec.isAmiCompare();
        if (verbose || amiCompare != AutoModelSpec.DEF_AMICOMPARE) {
            info.set(AMICOMPARE, amiCompare);
        }
        double tsig = spec.getTsig();
        if (verbose || tsig != AutoModelSpec.DEF_TSIG) {
            info.set(TSIG, tsig);
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
        AutoModelSpec.Builder builder = AutoModelSpec.builder().enabled(true);
        Double pcr = info.get(PCR, Double.class);
        if (pcr != null) {
            builder.pcr(pcr);
        }
        Double ub1 = info.get(UB1, Double.class);
        if (ub1 != null) {
            builder.ub1(ub1);
        }
        Double ub2 = info.get(UB2, Double.class);
        if (ub2 != null) {
            builder.ub2(ub2);
        }
        Double cancel = info.get(CANCEL, Double.class);
        if (cancel != null) {
            builder.cancel(cancel);
        }
        Double pc = info.get(PC, Double.class);
        if (pc != null) {
            builder.pc(pc);
        }
        Double tsig = info.get(TSIG, Double.class);
        if (tsig != null) {
            builder.tsig(tsig);
        }
        Boolean ami = info.get(AMICOMPARE, Boolean.class);
        if (ami != null) {
            builder.amiCompare(ami);
        }
        Boolean fal = info.get(FAL, Boolean.class);
        if (fal != null) {
            builder.acceptDefault(fal);
        }

        return builder.build();
    }

}
