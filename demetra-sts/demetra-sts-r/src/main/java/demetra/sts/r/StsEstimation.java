/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.sts.r;

import demetra.data.Parameter;
import demetra.information.InformationMapping;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BsmData;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import jdplus.sts.SsfBsm;
import jdplus.sts.internal.BsmKernel;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.sts.SeasonalModel;
import jdplus.math.matrices.Matrix;
import static jdplus.timeseries.simplets.TsDataToolkit.add;
import static jdplus.timeseries.simplets.TsDataToolkit.subtract;
import demetra.information.Explorable;
import demetra.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class StsEstimation {

    @lombok.Value
    @lombok.Builder
    public static class Results implements Explorable {

        TsData y, t, s, i;
        BsmData bsm;
        DiffuseConcentratedLikelihood likelihood;
        Matrix parametersCovariance;
        double[] score;
        int nparams;

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa",
                UCM = "ucm", UCARIMA = "ucarima", BSM = "bsm",
                LVAR = "levelvar", SVAR = "slopevar", SEASVAR = "seasvar", CVAR = "cyclevar", NVAR = "noisevar",
                CDUMP = "cycledumpingfactor", CLENGTH = "cyclelength",
                LL = "likelihood", PCOV = "pcov", SCORE = "score";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class getSourceClass() {
                return Results.class;
            }
        };

        static {
            MAPPING.set(LVAR, Double.class, source -> source.getBsm().getLevelVar());
            MAPPING.set(SVAR, Double.class, source -> source.getBsm().getSlopeVar());
            MAPPING.set(CVAR, Double.class, source -> source.getBsm().getCycleVar());
            MAPPING.set(SEASVAR, Double.class, source -> source.getBsm().getSeasonalVar());
            MAPPING.set(NVAR, Double.class, source -> source.getBsm().getNoiseVar());
            MAPPING.set(CDUMP, Double.class, source -> source.getBsm().getCycleDumpingFactor());
            MAPPING.set(CLENGTH, Double.class, source -> source.getBsm().getCycleLength());
            MAPPING.set(Y, TsData.class, source -> source.getY());
            MAPPING.set(T, TsData.class, source -> source.getT());
            MAPPING.set(S, TsData.class, source -> source.getS());
            MAPPING.set(I, TsData.class, source -> source.getI());
            MAPPING.set(SA, TsData.class, source -> subtract(source.getY(), source.getS()));
            MAPPING.delegate(LL, DiffuseLikelihoodStatistics.class, r -> r.getLikelihood().stats(0, r.getNparams()));
            MAPPING.set(PCOV, Matrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }
    }

    public Results process(TsData y, int level, int slope, int cycle, int noise, String seasmodel) {
        SeasonalModel sm = SeasonalModel.valueOf(seasmodel);
        BsmSpec mspec = BsmSpec.builder()
                .level(of(level), of(slope))
                .cycle(cycle != 0)
                .noise(of(noise))
                .seasonal(sm)
                .build();

        BsmKernel monitor = new BsmKernel(null);
        if (!monitor.process(y.getValues(), y.getTsUnit().ratioOf(TsUnit.YEAR), mspec)) {
            return null;
        }

        BsmData bsm = monitor.result(true);
        SsfBsm ssf = SsfBsm.of(bsm);
        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, new SsfData(y.getValues()), true, true);

        TsData t = null, c = null, s = null, seas = null, n = null;
        TsPeriod start = y.getStart();
        mspec = monitor.finalSpecification(true);
        if (mspec.hasLevel()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Level);
            t = TsData.ofInternal(start, sr.getComponent(pos).toArray());
        }
        if (mspec.hasSlope()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Slope);
            s = TsData.ofInternal(start, sr.getComponent(pos).toArray());
        }
        if (mspec.hasCycle()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Cycle);
            c = TsData.ofInternal(start, sr.getComponent(pos).toArray());
        }
        if (mspec.hasSeasonal()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Seasonal);
            seas = TsData.ofInternal(start, sr.getComponent(pos).toArray());
        }
        if (mspec.hasNoise()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Noise);
            n = TsData.ofInternal(start, sr.getComponent(pos).toArray());
        }

        IFunctionPoint ml = monitor.maxLikelihoodFunction();
        IFunctionDerivatives derivatives = new NumericalDerivatives(ml, false);
        int ndf = y.length();
        double objective = ml.getValue();
        Matrix hessian = derivatives.hessian();
        double[] score = derivatives.gradient().toArray();
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < score.length; ++i) {
            score[i] *= (-.5 * ndf) / objective;
        }

        return Results.builder()
                .likelihood(monitor.getLikelihood())
                .bsm(bsm)
                .y(y)
                .t(t)
                .s(seas)
                .i(add(n, c))
                .parametersCovariance(hessian)
                .score(score)
                .nparams(score.length)
                .build();
    }

    private Parameter of(int p) {
        if (p == 0) {
            return Parameter.zero();
        } else if (p > 0) {
            return Parameter.undefined();
        } else {
            return null;
        }
    }
}
