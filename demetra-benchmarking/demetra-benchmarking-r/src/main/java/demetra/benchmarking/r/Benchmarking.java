/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.benchmarking.r;

import demetra.benchmarking.multivariate.ContemporaneousConstraint;
import demetra.benchmarking.multivariate.MultivariateCholette;
import demetra.benchmarking.multivariate.MultivariateCholetteSpec;
import demetra.benchmarking.multivariate.TemporalConstraint;
import demetra.benchmarking.univariate.CholetteSpec;
import demetra.benchmarking.univariate.DentonSpec;
import demetra.benchmarking.univariate.Cholette;
import demetra.benchmarking.univariate.CubicSpline;
import demetra.benchmarking.univariate.CubicSplineSpec;
import demetra.benchmarking.univariate.Denton;
import demetra.benchmarking.univariate.GrowthRatePreservation;
import demetra.benchmarking.univariate.GrpSpec;
import demetra.data.AggregationType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import demetra.util.r.Dictionary;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Benchmarking {

    public TsData denton(TsData source, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion, int pos) {
        DentonSpec spec = DentonSpec
                .builder()
                .differencing(differencing)
                .multiplicative(multiplicative)
                .modified(modified)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .build();
        return Denton.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData denton(int nfreq, TsData bench, int differencing, boolean multiplicative, boolean modified, String conversion, int pos) {
        DentonSpec spec = DentonSpec
                .builder()
                .differencing(differencing)
                .multiplicative(multiplicative)
                .modified(modified)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .build();
        return Denton.benchmark(TsUnit.ofAnnualFrequency(nfreq), bench.cleanExtremities(), spec);
    }

    public TsData cholette(TsData source, TsData bench, double rho, double lambda, String bias, String conversion, int pos) {
        CholetteSpec spec = CholetteSpec.builder()
                .rho(rho)
                .lambda(lambda)
                .aggregationType(AggregationType.valueOf(conversion))
                .observationPosition(pos-1)
                .bias(CholetteSpec.BiasCorrection.valueOf(bias))
                .build();
        return Cholette.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData grp(TsData source, TsData bench, String conversion, int pos, double eps, int iter, boolean denton) {
        AggregationType type = AggregationType.valueOf(conversion);
        GrpSpec spec=GrpSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .maxIter(iter)
                .precision(eps)
                .dentonInitialization(denton)
                .build();
        return GrowthRatePreservation.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData cubicSpline(TsData source, TsData bench, String conversion, int pos) {
        AggregationType type = AggregationType.valueOf(conversion);
        CubicSplineSpec spec=CubicSplineSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .build();
        return CubicSpline.benchmark(source.cleanExtremities(), bench.cleanExtremities(), spec);
    }

    public TsData cubicSpline(int nfreq, TsData bench, String conversion, int pos) {
        AggregationType type = AggregationType.valueOf(conversion);
        CubicSplineSpec spec=CubicSplineSpec.builder()
                .aggregationType(type)
                .observationPosition(pos-1)
                .build();
        return CubicSpline.benchmark(TsUnit.ofAnnualFrequency(nfreq), bench.cleanExtremities(), spec);
    }

    public Dictionary multiCholette(Dictionary input, String[] temporalConstraints, String[] contemporaneousConstraints, double rho, double lambda) {
        MultivariateCholetteSpec.Builder builder = MultivariateCholetteSpec.builder()
                .rho(rho)
                .lambda(lambda);
        if (temporalConstraints != null) {
            for (int i = 0; i < temporalConstraints.length; ++i) {
                if (temporalConstraints[i].length() > 0) {
                    builder.temporalConstraint(TemporalConstraint.parse(temporalConstraints[i]));
                }
            }
        }
        if (contemporaneousConstraints != null) {
            for (int i = 0; i < contemporaneousConstraints.length; ++i) {
                if (contemporaneousConstraints[i].length() > 0) {
                    builder.contemporaneousConstraint(ContemporaneousConstraint.parse(contemporaneousConstraints[i]));
                }
            }
        }
        Map<String, TsData> rslt = MultivariateCholette.benchmark(input.data(), builder.build());
        return Dictionary.of(rslt);
    }
}
