/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.protobuf;

import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.timeseries.TsData;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

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

    public SaProtos.SaDecomposition convert(SeriesDecomposition decomp) {
        SaProtos.SaDecomposition.Builder builder = SaProtos.SaDecomposition.newBuilder();
        // Series

        return builder
                .setSeries(convert(decomp, ComponentType.Series))
                .setSeasonallyAdjusted(convert(decomp, ComponentType.SeasonallyAdjusted))
                .setTrend(convert(decomp, ComponentType.Trend))
                .setSeasonal(convert(decomp, ComponentType.Seasonal))
                .setIrregular(convert(decomp, ComponentType.Irregular))
                .build();
    }

    public SaProtos.Component convert(SeriesDecomposition decomp, ComponentType type) {
        TsData s = decomp.getSeries(type, ComponentInformation.Value);
        TsData fs = decomp.getSeries(type, ComponentInformation.Forecast);
        TsData bs = decomp.getSeries(type, ComponentInformation.Backcast);
        TsData es = decomp.getSeries(type, ComponentInformation.Stdev);
        TsData efs = decomp.getSeries(type, ComponentInformation.StdevForecast);
        TsData ebs = decomp.getSeries(type, ComponentInformation.StdevBackcast);

        TsData S = TsData.concatenate(bs, s, fs);
        TsData ES = TsData.concatenate(ebs, es, efs);

        return SaProtos.Component.newBuilder()
                .setData(ToolkitProtosUtility.convert(S))
                .setStde(ToolkitProtosUtility.convert(ES))
                .setNbcasts(bs == null ? 0 : bs.length())
                .setNfcasts(fs == null ? 0 : fs.length())
                .build();
    }

    public SaProtos.ComponentType convert(ComponentType type) {
        switch (type) {
            case Series:
                return SaProtos.ComponentType.SERIES;
            case SeasonallyAdjusted:
                return SaProtos.ComponentType.SEASONALLYADJUSTED;
            case Trend:
                return SaProtos.ComponentType.TREND;
            case Seasonal:
                return SaProtos.ComponentType.SEASONAL;
            case Irregular:
                return SaProtos.ComponentType.IRREGULAR;
            default:
                return SaProtos.ComponentType.UNDEFINED;
        }
    }
    

}
