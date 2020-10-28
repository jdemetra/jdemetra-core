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
import demetra.seats.DecompositionSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DecompositionSpecMapping {

    public final String ADMISS = "admiss",
            METHOD = "method",
            EPSPHI = "epsphi",
            RMOD = "rmod",
            SMOD = "smod",
            SMOD1 = "stsmod",
            XL = "xl",
            NPRED = "npred", NBACK = "nback";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ADMISS), Boolean.class);
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, EPSPHI), Double.class);
        dic.put(InformationSet.item(prefix, RMOD), Double.class);
        dic.put(InformationSet.item(prefix, SMOD), Double.class);
        dic.put(InformationSet.item(prefix, SMOD1), Double.class);
        dic.put(InformationSet.item(prefix, XL), Double.class);
        dic.put(InformationSet.item(prefix, NPRED), Integer.class);
        dic.put(InformationSet.item(prefix, NBACK), Integer.class);
    }

    public InformationSet write(DecompositionSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSeasTolerance() != DecompositionSpec.DEF_EPSPHI) {
            info.add(EPSPHI, spec.getSeasTolerance());
        }
        if (verbose || spec.getTrendBoundary() != DecompositionSpec.DEF_RMOD) {
            info.add(RMOD, spec.getTrendBoundary());
        }
        if (verbose || spec.getSeasBoundary() != DecompositionSpec.DEF_SMOD) {
            info.add(SMOD, spec.getSeasBoundary());
        }
        if (verbose || spec.getSeasBoundaryAtPi() != DecompositionSpec.DEF_SMOD1) {
            info.add(SMOD1, spec.getSeasBoundaryAtPi());
        }
        if (verbose || spec.getXlBoundary() != DecompositionSpec.DEF_XL) {
            info.add(XL, spec.getXlBoundary());
        }
        if (verbose || spec.getApproximationMode() != DecompositionSpec.ModelApproximationMode.Legacy) {
            info.add(ADMISS, spec.getApproximationMode().name());
        }
        if (verbose || spec.getMethod() != DecompositionSpec.ComponentsEstimationMethod.Burman) {
            info.add(METHOD, spec.getMethod().name());
        }
        if (verbose || spec.getForecastCount() != DecompositionSpec.DEF_FORECASTS) {
            info.add(NPRED, spec.getForecastCount());
        }
        if (verbose || spec.getBackcastCount() != DecompositionSpec.DEF_BACKCASTS) {
            info.add(NBACK, spec.getBackcastCount());
        }
        return info;
    }

    public DecompositionSpec read(InformationSet info) {
        if (info == null) {
            return DecompositionSpec.DEFAULT;
        }
        DecompositionSpec.Builder builder = DecompositionSpec.builder();
        Double eps = info.get(EPSPHI, Double.class);
        if (eps != null) {
            builder = builder.seasTolerance(eps);
        }
        Double rmod = info.get(RMOD, Double.class);
        if (rmod != null) {
            builder = builder.trendBoundary(rmod);
        }
        Double smod = info.get(SMOD, Double.class);
        if (smod != null) {
            builder = builder.seasBoundary(smod);
        }
        Double smod1 = info.get(SMOD1, Double.class);
        if (smod1 != null) {
            builder = builder.seasBoundaryAtPi(smod1);
        }
        Double xl = info.get(XL, Double.class);
        if (xl != null) {
            builder = builder.xlBoundary(xl);
        }
        Integer p = info.get(NPRED, Integer.class);
        if (p != null) {
            builder = builder.forecastCount(p);
        }
        Integer b = info.get(NBACK, Integer.class);
        if (b != null) {
            builder = builder.backcastCount(b);
        }
        String admiss = info.get(ADMISS, String.class);
        if (admiss != null) {
            builder = builder.approximationMode(DecompositionSpec.ModelApproximationMode.valueOf(admiss));
        }
        String method = info.get(METHOD, String.class);
        if (method != null) {
            builder = builder.method(DecompositionSpec.ComponentsEstimationMethod.valueOf(method));
        }
        return builder.build();
    }

}
