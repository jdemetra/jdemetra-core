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
import demetra.arima.ArimaProcess;
import demetra.arima.UcarimaProcess;
import demetra.descriptors.arima.UcarimaDescriptor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import demetra.information.InformationMapping;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.linearfilters.BackFilter;
import static demetra.r.AirlineDecomposition.ucm;
import demetra.regarima.GlsArimaProcessor;
import demetra.arima.estimation.IArimaMapping;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.univariate.SsfData;
import demetra.ucarima.UcarimaModel;
import demetra.ucarima.ssf.SsfUcarima;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineDecomposition {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        double[] y, t, s, i, sa, n;
        ArimaProcess arima;
        UcarimaProcess ucarima;
        ConcentratedLikelihoodWithMissing concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        FastMatrix parametersCovariance;
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

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa", N="n",
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
            MAPPING.set(N, double[].class, source -> source.getN());
            MAPPING.set(SA, double[].class, source -> {
                double[] y = source.getY().clone(), s = source.getS();
                for (int i = 0; i < y.length; ++i) {
                    y[i] -= s[i];
                }
                return y;
            });
            MAPPING.delegate(UCARIMA, UcarimaDescriptor.getMapping(), source -> source.getUcarima());
            MAPPING.set(UCM, UcarimaProcess.class, source -> source.getUcarima());
            MAPPING.delegate(LL, LikelihoodStatisticsDescriptor.getMapping(), r -> r.statistics);
            //MAPPING.set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
            MAPPING.set(PARAMETERS, double[].class, source -> source.getParameters());
            //MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }
    }

    public Results process(double[] s, double period, boolean adjust, boolean sn) {
        int iperiod = (int) period;
        if (period - iperiod < 1e-9) {
            period = iperiod;
            adjust = false;
        }
        PeriodicAirlineMapping mapping = new PeriodicAirlineMapping(period, adjust, false);

        GlsArimaProcessor.Builder<ArimaModel> builder = GlsArimaProcessor.builder(ArimaModel.class);
        builder.mapping(mapping)
                .minimizer(new LevenbergMarquardtMinimizer())
                .precision(1e-12)
                .useMaximumLikelihood(true)
                .useParallelProcessing(true)
                .build();
        ArimaModel arima = mapping.getDefault();
        RegArimaModel<ArimaModel> regarima
                = RegArimaModel.builder(ArimaModel.class)
                        .y(DoubleSeq.copyOf(s))
                        .arima(arima)
                        .build();
        GlsArimaProcessor<ArimaModel> monitor = builder.build();
        RegArimaEstimation<ArimaModel> rslt = monitor.process(regarima);
        arima = rslt.getModel().arima();
        double[] p = mapping.parametersOf(arima).toArray();
        UcarimaModel ucm = ucm(rslt.getModel().arima(), sn);

        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);

        ArimaProcess sum = ArimaModel.of(ucm.getModel()).toType(null);
        UcarimaProcess ucmt;
        if (sn) {
            ArimaProcess mn = ArimaModel.of(ucm.getComponent(0)).toType("noise");
            ArimaProcess ms = ArimaModel.of(ucm.getComponent(1)).toType("signal");
            ucmt= new UcarimaProcess(sum, new ArimaProcess[]{ms, mn});
            
        } else {
            ArimaProcess mt = ArimaModel.of(ucm.getComponent(0)).toType("trend");
            ArimaProcess ms = ArimaModel.of(ucm.getComponent(1)).toType("seasonal");
            ArimaProcess mi = ArimaModel.of(ucm.getComponent(2)).toType("irregular");
            ucmt= new UcarimaProcess(sum, new ArimaProcess[]{mt, ms, mi}); 
        }
        int[] pos = ssf.componentsPosition();
        if (sn)
        return Results.builder()
                .y(s)
                .s(ds.item(pos[1]).toArray())
                .n(ds.item(pos[0]).toArray())
                .ucarima(ucmt)
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood())
                .parameters(p)
                .arima(arima.toType("arima"))
                .statistics(rslt.statistics(0))
                .build();
            else
        return Results.builder()
                .y(s)
                .t(ds.item(pos[0]).toArray())
                .s(ds.item(pos[1]).toArray())
                .i(ds.item(pos[2]).toArray())
                .ucarima(ucmt)
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood())
                .parameters(p)
                .arima(arima.toType("arima"))
                .statistics(rslt.statistics(0))
                .build();

    }

}

class PeriodicAirlineMapping implements IArimaMapping<ArimaModel> {

    private final double f0, f1;
    private final int p0;
    private final boolean adjust;
    private final boolean stationary;

    private PeriodicAirlineMapping(double f0, double f1, int p0, boolean adjust, boolean stationary) {
        this.f0 = f0;
        this.f1 = f1;
        this.p0 = p0;
        this.adjust = adjust;
        this.stationary = stationary;
    }

    public PeriodicAirlineMapping(double period) {
        this(period, true, false);
    }

    public PeriodicAirlineMapping(double period, boolean adjust, boolean stationary) {
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
    public ArimaModel map(DoubleSeq p) {
        double th = p.get(0), bth = p.get(1);
        double[] ma = new double[]{1, -th};
        double[] dma = new double[adjust ? p0 + 2 : p0 + 1];
        dma[0] = 1;
        if (adjust) {
//            double[] d = new double[p0 + 2];
//            d[0] = 1;
//            d[p0] = -f0;
//            d[p0 + 1] = -f1;
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
    public DoubleSeq parametersOf(ArimaModel t) {
        BackFilter ma = t.getMa();
        double[] p = new double[2];
        p[0] = -ma.get(1);
        if (adjust) {
            p[1] = -ma.get(p0) / f0;
        } else {
            p[1] = -ma.get(p0);
        }
        return DoubleSeq.copyOf(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        return inparams.allMatch(x -> Math.abs(x) < .999);
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
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

    @Override
    public DoubleSeq getDefaultParameters() {
        return Doubles.of(new double[]{.9, .9});
    }

    @Override
    public IArimaMapping<ArimaModel> stationaryMapping() {
        return stationary ? this : new PeriodicAirlineMapping(f0, f1, p0, adjust, true);
    }
}
