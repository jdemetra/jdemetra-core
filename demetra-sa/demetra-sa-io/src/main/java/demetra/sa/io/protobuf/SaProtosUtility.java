/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sa.io.protobuf;

import demetra.data.Iterables;
import demetra.modelling.ComponentInformation;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.sa.ComponentType;
import demetra.sa.DefaultSaDiagnostics;
import demetra.sa.EstimationPolicy;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SeriesDecomposition;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.sa.benchmarking.SaBenchmarkingSpec;
import demetra.sa.diagnostics.CombinedSeasonalityTest;
import demetra.timeseries.TsData;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.sa.diagnostics.GenericSaTests;
import jdplus.sa.tests.CombinedSeasonality;

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

    public ModellingProtos.TsComponent convert(SeriesDecomposition decomp, ComponentType type) {
        TsData s = decomp.getSeries(type, ComponentInformation.Value);
        TsData fs = decomp.getSeries(type, ComponentInformation.Forecast);
        TsData bs = decomp.getSeries(type, ComponentInformation.Backcast);
        TsData es = decomp.getSeries(type, ComponentInformation.Stdev);
        TsData efs = decomp.getSeries(type, ComponentInformation.StdevForecast);
        TsData ebs = decomp.getSeries(type, ComponentInformation.StdevBackcast);

        TsData S = TsData.concatenate(bs, s, fs);
        TsData ES = TsData.concatenate(ebs, es, efs);

        ModellingProtos.TsComponent.Builder builder = ModellingProtos.TsComponent.newBuilder()
                .setData(ToolkitProtosUtility.convert(S))
                .setNbcasts(bs == null ? 0 : bs.length())
                .setNfcasts(fs == null ? 0 : fs.length());
        if (ES != null) {
            builder.addAllStde(Iterables.of(ES.getValues()));
        }

        return builder.build();
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

    public SaProtos.VarianceDecomposition convert(StationaryVarianceDecomposition var) {
        return SaProtos.VarianceDecomposition.newBuilder()
                .setCycle(var.getC())
                .setSeasonal(var.getS())
                .setIrregular(var.getI())
                .setCalendar(var.getCalendar())
                .setOthers(var.getP())
                .setTotal(var.total())
                .build();
    }

    public SaProtos.Diagnostics of(GenericSaTests tests, StationaryVarianceDecomposition var) {
        return SaProtos.Diagnostics.newBuilder()
                .setSeasonalFtestOnIrregular(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnIrregular().fTest()))
                .setSeasonalFtestOnSa(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnSa().fTest()))
                .setSeasonalQtestOnIrregular(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnIrregular().qsTest()))
                .setSeasonalQtestOnSa(ToolkitProtosUtility.convert(tests.residualSeasonalityTestsOnSa().qsTest()))
                .setTdFtestOnIrregular(ToolkitProtosUtility.convert(tests.residualTradingDaysTests().irrTest(false)))
                .setTdFtestOnSa(ToolkitProtosUtility.convert(tests.residualTradingDaysTests().saTest(false)))
                .setVarianceDecomposition(SaProtosUtility.convert(var))
                .build();

    }

    public SaProtos.EstimationPolicy convert(EstimationPolicyType policy) {
        switch (policy) {
            case Complete:
                return SaProtos.EstimationPolicy.POLICY_COMPLETE;
            case Outliers_StochasticComponent:
                return SaProtos.EstimationPolicy.POLICY_ARIMA;
            case Outliers:
                return SaProtos.EstimationPolicy.POLICY_OUTLIERS;
            case LastOutliers:
                return SaProtos.EstimationPolicy.POLICY_LASTOUTLIERS;
            case FreeParameters:
                return SaProtos.EstimationPolicy.POLICY_FREE_PARAMETERS;
            case FixedAutoRegressiveParameters:
                return SaProtos.EstimationPolicy.POLICY_FIXED_AUTOREGRESSIVEPARAMETERS;
            case FixedParameters:
                return SaProtos.EstimationPolicy.POLICY_FIXED_PARAMETERS;
            case Fixed:
                return SaProtos.EstimationPolicy.POLICY_FIXED;
            case Current:
                return SaProtos.EstimationPolicy.POLICY_CURRENT;
            default:
                return SaProtos.EstimationPolicy.UNRECOGNIZED;
        }
    }

    public EstimationPolicyType convert(SaProtos.EstimationPolicy policy) {
        switch (policy) {
            case POLICY_COMPLETE:
                return EstimationPolicyType.Complete;
            case POLICY_ARIMA:
                return EstimationPolicyType.Outliers_StochasticComponent;
            case POLICY_OUTLIERS:
                return EstimationPolicyType.Outliers;
            case POLICY_LASTOUTLIERS:
                return EstimationPolicyType.LastOutliers;
            case POLICY_FREE_PARAMETERS:
                return EstimationPolicyType.FreeParameters;
            case POLICY_FIXED_AUTOREGRESSIVEPARAMETERS:
                return EstimationPolicyType.FixedAutoRegressiveParameters;
            case POLICY_FIXED_PARAMETERS:
                return EstimationPolicyType.FixedParameters;
            case POLICY_FIXED:
                return EstimationPolicyType.Fixed;
            case POLICY_CURRENT:
                return EstimationPolicyType.Current;
            default:
                return EstimationPolicyType.None;
        }
    }
    
    public SaProtos.IdentifiableSeasonality convert(CombinedSeasonalityTest.IdentifiableSeasonality seas){
        switch (seas){
            case None: return SaProtos.IdentifiableSeasonality.SA_NONE;
            case ProbablyNone: return SaProtos.IdentifiableSeasonality.SA_PROBABLY_NONE;
            case Present: return SaProtos.IdentifiableSeasonality.SA_PRESENT;
            default: return SaProtos.IdentifiableSeasonality.SA_UNKNOWN;
        }
    }
    
    public SaProtos.CombinedSeasonalityTest convert(CombinedSeasonality cs){
        return SaProtos.CombinedSeasonalityTest.newBuilder()
                .setKruskalWallis(ToolkitProtosUtility.convert(cs.getNonParametricTestForStableSeasonality().build()))
                .setStableSeasonality(ToolkitProtosUtility.convert(cs.getStableSeasonalityTest()))
                .setEvolutiveSeasonality(ToolkitProtosUtility.convert(cs.getEvolutiveSeasonalityTest()))
                .setSeasonality(convert(cs.getSummary()))
                .build();
    }

}
