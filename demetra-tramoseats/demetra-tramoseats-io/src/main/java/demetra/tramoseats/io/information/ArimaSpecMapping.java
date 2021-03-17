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

import demetra.modelling.implementations.SarimaSpec;
import demetra.data.Parameter;
import demetra.information.InformationSet;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ArimaSpecMapping {

    public static final String MEAN = "mean", MU = "mu",
            THETA = "theta", D = "d", PHI = "phi",
            BTHETA = "btheta", BD = "bd", BPHI = "bphi";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, MEAN), Boolean.class);
        dic.put(InformationSet.item(prefix, MU), Parameter.class);
        dic.put(InformationSet.item(prefix, D), Integer.class);
        dic.put(InformationSet.item(prefix, BD), Integer.class);
        dic.put(InformationSet.item(prefix, THETA), Parameter[].class);
        dic.put(InformationSet.item(prefix, PHI), Parameter[].class);
        dic.put(InformationSet.item(prefix, BTHETA), Parameter[].class);
        dic.put(InformationSet.item(prefix, BPHI), Parameter[].class);
    }

    public InformationSet write(SarimaSpec spec, boolean verbose) {
        if (SarimaSpec.airline().equals(spec)) {
            return null;
        }
        InformationSet info = new InformationSet();
        demetra.data.Parameter[] phi = spec.getPhi();
        if (phi.length > 0) {
            info.add(PHI, phi);
        }
        int d = spec.getD();
        if (verbose || d != 1) {
            info.add(D, d);
        }
        demetra.data.Parameter[] th = spec.getTheta();
        if (th.length > 0) {
            info.add(THETA, th);
        }
        demetra.data.Parameter[] bphi = spec.getBphi();
        if (bphi.length > 0) {
            info.add(BPHI, bphi);
        }
        int bd = spec.getBd();
        if (verbose || bd != 1) {
            info.add(BD, bd);
        }
        demetra.data.Parameter[] bth = spec.getBtheta();
        if (bth.length > 0) {
            info.add(BTHETA, bth);
        }
        return info;
    }

    public SarimaSpec read(InformationSet info) {
        if (info == null) {
            return SarimaSpec.airline();
        }
        // default values
        SarimaSpec.Builder builder = SarimaSpec.builder();
        Integer d = info.get(D, Integer.class);
        if (d != null) {
            builder.d(d);
        }
        Integer bd = info.get(BD, Integer.class);
        if (bd != null) {
            builder.bd(bd);
        }
        return builder.phi(info.get(PHI, Parameter[].class))
                .theta(info.get(THETA, Parameter[].class))
                .bphi(info.get(BPHI, Parameter[].class))
                .btheta(info.get(BTHETA, Parameter[].class))
                .build();
    }
}
