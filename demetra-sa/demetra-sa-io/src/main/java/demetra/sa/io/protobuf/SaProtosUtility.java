/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.protobuf;

import demetra.sa.benchmarking.SaBenchmarkingSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaProtosUtility {

    public SaBenchmarkingSpec.Target convert(SaProtos.BenchmarkingTarget t) {
        switch (t) {
            case BENCH_TARGET_ORIGINAL:
                return SaBenchmarkingSpec.Target.Original;
            default:
                return SaBenchmarkingSpec.Target.CalendarAdjusted;
        }
    }

    public SaProtos.BenchmarkingTarget convert(SaBenchmarkingSpec.Target t) {
        switch (t) {
            case Original:
                return SaProtos.BenchmarkingTarget.BENCH_TARGET_ORIGINAL;
            case CalendarAdjusted:
                return SaProtos.BenchmarkingTarget.BENCH_TARGET_CALENDARADJUSTED;
            default:
                return SaProtos.BenchmarkingTarget.UNRECOGNIZED;
        }
    }

    public SaProtos.BenchmarkingBias convert(SaBenchmarkingSpec.BiasCorrection t) {
        switch (t) {
            case None:
                return SaProtos.BenchmarkingBias.BENCH_BIAS_NONE;
            case Additive:
                return SaProtos.BenchmarkingBias.BENCH_BIAS_ADDITIVE;
            case Multiplicative:
                return SaProtos.BenchmarkingBias.BENCH_BIAS_MULTIPLICATIVE;
            default:
                return SaProtos.BenchmarkingBias.UNRECOGNIZED;
        }
    }

    public SaBenchmarkingSpec.BiasCorrection convert(SaProtos.BenchmarkingBias t) {
        switch (t) {
            case BENCH_BIAS_ADDITIVE:
                return SaBenchmarkingSpec.BiasCorrection.Additive;
            case BENCH_BIAS_MULTIPLICATIVE:
                return SaBenchmarkingSpec.BiasCorrection.Multiplicative;
            default:
                return SaBenchmarkingSpec.BiasCorrection.None;
        }
    }

    public SaBenchmarkingSpec convert(SaProtos.BenchmarkingSpec spec) {
        return SaBenchmarkingSpec.builder()
                .enabled(spec.getEnabled())
                .target(convert(spec.getTarget()))
                .lambda(spec.getLambda())
                .rho(spec.getRho())
                .biasCorrection(convert(spec.getBias()))
                .forecast(spec.getForecast())
                .build();
    }

    public void fill(SaBenchmarkingSpec spec, SaProtos.BenchmarkingSpec.Builder builder) {
        builder.setEnabled(spec.isEnabled())
                .setTarget(convert(spec.getTarget()))
                .setLambda(spec.getLambda())
                .setRho(spec.getRho())
                .setBias(convert(spec.getBiasCorrection()))
                .setForecast(spec.isForecast());
    }

    public SaProtos.BenchmarkingSpec convert(SaBenchmarkingSpec spec) {
        SaProtos.BenchmarkingSpec.Builder builder = SaProtos.BenchmarkingSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

}
