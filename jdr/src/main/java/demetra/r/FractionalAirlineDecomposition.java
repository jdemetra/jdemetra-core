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
package demetra.r;

import demetra.arima.ArimaModel;
import demetra.arima.ArimaType;
import demetra.arima.UcarimaType;
import demetra.arima.mapping.UcarimaInfo;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.data.DataBlock;
import demetra.data.DataBlockStorage;
import demetra.data.DoubleSequence;
import demetra.information.InformationMapping;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.mapping.LikelihoodInfo;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.Matrix;
import demetra.processing.IProcResults;
import static demetra.r.AirlineDecomposition.ucm;
import demetra.regarima.GlsArimaProcessor;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.SsfData;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements IProcResults {

        double[] y, t, s, i, sa;
        ArimaType arima;
        UcarimaType ucarima;
        ConcentratedLikelihood concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        Matrix parametersCovariance;
        double[] parameters, score;

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
                UCM = "ucm", UCARIMA = "ucarima",
                LL = "likelihood", PCOV = "pcov", SCORE = "score", PARAMETERS = "parameters";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.set(Y, double[].class, source -> source.getY());
            MAPPING.set(T, double[].class, source -> source.getT());
            MAPPING.set(S, double[].class, source -> source.getS());
            MAPPING.set(I, double[].class, source -> source.getI());
            MAPPING.set(SA, double[].class, source -> {
                double[] y=source.getY().clone(), s=source.getS();
                for (int i=0; i<y.length; ++i){
                    y[i]-=s[i];
                }
                return y;
            });
            MAPPING.delegate(UCARIMA, UcarimaInfo.getMapping(), source -> source.getUcarima());
            MAPPING.set(UCM, UcarimaType.class, source -> source.getUcarima());
            MAPPING.delegate(LL, LikelihoodInfo.getMapping(), r -> r.statistics);
            //MAPPING.set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
            MAPPING.set(PARAMETERS, double[].class, source -> source.getParameters());
            //MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }
    }

    public Results process(double[] s, double period, boolean adjust) {
        PeriodicMapping mapping = new PeriodicMapping(period, adjust, false);
        
        GlsArimaProcessor.Builder<ArimaModel> builder=GlsArimaProcessor.builder();
        builder.mapping(model-> new PeriodicMapping(period, adjust, model.isStationary()))
                .minimizer(new LevenbergMarquardtMinimizer())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.builder(ArimaModel.class)
                        .y(DoubleSequence.of(s))
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima);
        arima = rslt.getModel().arima();
        double[] p=mapping.map(arima).toArray();
        UcarimaModel ucm = ucm(rslt.getModel().arima());

        ucm = ucm.simplify();
        SsfUcarima ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);

        ArimaType sum = ArimaModel.copyOf(ucm.getModel()).toType(null);
        ArimaType mt = ArimaModel.copyOf(ucm.getComponent(0)).toType("trend");
        ArimaType ms = ArimaModel.copyOf(ucm.getComponent(1)).toType("seasonal");
        ArimaType mi = ArimaModel.copyOf(ucm.getComponent(2)).toType("irregular");

        return Results.builder()
                .y(s)
                .t(ds.item(ssf.getComponentPosition(0)).toArray())
                .s(ds.item(ssf.getComponentPosition(1)).toArray())
                .i(ds.item(ssf.getComponentPosition(2)).toArray())
                .ucarima(new UcarimaType(sum, new ArimaType[]{mt, ms, mi}))
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood())
                .parameters(p)
                .arima(arima.toType("arima"))
                .statistics(rslt.statistics(0))
                .build();

    }

}

class PeriodicMapping implements IParametricMapping<ArimaModel> {

    private final double f0, f1;
    private final int p0;
    private boolean adjust = true;
    protected boolean stationary;

    PeriodicMapping(double period, boolean adjust, boolean stationary) {
        this.adjust = adjust;
        this.stationary = stationary;
        if (adjust) {
            p0 = (int) period;
            f1 = period - p0;
            f0 = 1 - f1;
        } else {
            p0 = (int) (period + .5);
            f1 = f0 = 0;
        }
    }

    @Override
    public ArimaModel map(DoubleSequence p) {
        double th = p.get(0), bth = p.get(1);
        double[] ma = new double[]{1, -th};
        double[] dma = new double[adjust ? p0 + 2 : p0 + 1];
        dma[0] = 1;
        if (adjust) {
            double[] d = new double[p0 + 2];
            d[0] = 1;
            d[p0] = -f0;
            d[p0 + 1] = -f1;
            dma[p0] = -f0 * bth;
            dma[p0 + 1] = -f1 * bth;
            BackFilter fma = BackFilter.ofInternal(ma).times(BackFilter.ofInternal(dma));
            double[] s = new double[p0 + 1];
            for (int i = 0; i < p0; ++i) {
                s[i] = 1;
            }
            s[p0] = f1;
            if (stationary) {
                return new ArimaModel(BackFilter.ofInternal(s), BackFilter.ONE, fma, 1);
            } else {
                return new ArimaModel(BackFilter.ofInternal(s), BackFilter.ofInternal(1, -2, 1), fma, 1);
            }

        } else {
            double[] d = new double[p0 + 1];
            d[0] = 1;
            d[p0] = -1;
            dma[p0] = -bth;
            BackFilter fma = BackFilter.ofInternal(ma).times(BackFilter.ofInternal(dma));
            if (stationary) {
                return new ArimaModel(BackFilter.ONE, BackFilter.ONE, fma, 1);
            } else {
                return new ArimaModel(BackFilter.ONE, BackFilter.D1.times(BackFilter.ofInternal(d)), fma, 1);
            }

        }
    }

    @Override
    public DoubleSequence map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[2];
        p[0] = -ma.get(1);
        if (adjust) {
            p[1] = -ma.get(p0) / f0;
        } else {
            p[1] = -ma.get(p0);
        }
        return DoubleSequence.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        return inparams.allMatch(x -> Math.abs(x) < .999);
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        return 1e-6;
    }

    @Override
    public int getDim() {
        return 2;
    }

    @Override
    public double lbound(int idx) {
        return -1;
    }

    @Override
    public double ubound(int idx) {
        return 1;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        boolean changed = false;
        double p = ioparams.get(0);
        if (Math.abs(p) >= .999) {
            ioparams.set(0, 1 / p);
            changed = true;
        }
        p = ioparams.get(1);
        if (Math.abs(p) >= .999) {
            ioparams.set(1, 1 / p);
            changed = true;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "p" + idx;
    }

    /**
     * @return the adjust
     */
    public boolean isAdjust() {
        return adjust;
    }

    /**
     * @param adjust the adjust to set
     */
    public void setAdjust(boolean adjust) {
        this.adjust = adjust;
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        return DoubleSequence.of(.9, .9);
    }
}
