/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.information;

import demetra.information.InformationSet;
import demetra.sa.benchmarking.SaBenchmarkingSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaBenchmarkingSpecMapping {

    public static final String ENABLED = "enabled",
            TARGET = "target",
            FORECAST = "forecast",
            LAMBDA = "lambda",
            RHO = "rho",
            BIAS = "bias";

    public InformationSet write(SaBenchmarkingSpec spec, boolean verbose) {
        InformationSet info;
        if (verbose || spec.isEnabled()) {
            info = new InformationSet();
            info.add(ENABLED, spec.isEnabled());
        } else {
            return null;
        }
        if (verbose || spec.isForecast()) {
            info.add(FORECAST, spec.isForecast());
        }
        // The target is always specified
        info.add(TARGET, spec.getTarget().name());
        if (verbose || spec.getLambda() != SaBenchmarkingSpec.DEF_LAMBDA) {
            info.add(LAMBDA, spec.getLambda());
        }
        if (verbose || spec.getRho() != SaBenchmarkingSpec.DEF_RHO) {
            info.add(RHO, spec.getRho());
        }
        if (verbose || spec.getBiasCorrection() != SaBenchmarkingSpec.BiasCorrection.None) {
            info.add(BIAS, spec.getBiasCorrection().name());
        }
        return info;
    }

    public SaBenchmarkingSpec read(InformationSet info) {
        if (info == null) {
            return SaBenchmarkingSpec.DEFAULT_DISABLED;
        }
        // When the benchmarking is unused, we stop here. The other properties
        // are the defaults
        Boolean enabled = info.get(ENABLED, Boolean.class);
        if (enabled == null || !enabled) {
            return SaBenchmarkingSpec.DEFAULT_DISABLED;
        }
        SaBenchmarkingSpec.Builder builder = SaBenchmarkingSpec.builder()
                .enabled(true);
        Boolean f = info.get(FORECAST, Boolean.class);
        if (f != null) {
            builder.forecast(f);
        }
        Double rho = info.get(RHO, Double.class);
        if (rho != null) {
            builder.rho(rho);
        }
        Double lambda = info.get(LAMBDA, Double.class);
        if (lambda != null) {
            builder.lambda(lambda);
        }
        String target = info.get(TARGET, String.class);
        if (target != null) {
            builder.target(SaBenchmarkingSpec.Target.valueOf(target));
        } else {
            // In old specs, the default was Original
            // In new specs, the target will be specified
            builder.target(SaBenchmarkingSpec.Target.Original);
        }

        String bias = info.get(BIAS, String.class);
        if (bias != null) {
            builder.biasCorrection(SaBenchmarkingSpec.BiasCorrection.valueOf(bias));
        }
        return builder.build();
    }

}
