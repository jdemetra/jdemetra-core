/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.fractionalairline.MultiPeriodicAirlineMapping;
import demetra.information.InformationMapping;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.OutliersDetectionModule;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import demetra.processing.ProcResults;
import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class PeriodicAirline {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        RegArimaModel<ArimaModel> regarima;
        ConcentratedLikelihoodWithMissing concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        OutlierDescriptor[] outliers;
        Matrix parametersCovariance;
        double[] score;
        double[] parameters;
        double[] linearized;

        private static final String PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score",
                B = "b", T = "t", UNSCALEDBVAR = "unscaledbvar", MEAN = "mean", OUTLIERS = "outliers"
                , LIN="lin", REGRESSORS="regressors";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.delegate(LL, LikelihoodStatisticsDescriptor.getMapping(), r -> r.statistics);
            MAPPING.set(PCOV, Matrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
            MAPPING.set(PARAMETERS, double[].class, source -> source.getParameters());
            MAPPING.set(B, double[].class, source
                    -> {
                DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                return b.toArray();
            });
            MAPPING.set(T, double[].class, source
                    -> {
                int nhp = source.getParameters().length;
                return source.getConcentratedLogLikelihood().tstats(nhp, true);
            });
            MAPPING.set(MEAN, Double.class, source
                    -> {
                if (source.getRegarima().isMean()) {
                    DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                    int mpos = source.getRegarima().getMissingValuesCount();
                    return b.get(mpos);
                } else {
                    return 0.0;
                }
            });
            MAPPING.set(UNSCALEDBVAR, MatrixType.class, source -> source.getConcentratedLogLikelihood().unscaledCovariance());
            MAPPING.set(OUTLIERS, String[].class, source -> {
                OutlierDescriptor[] o = source.getOutliers();
                if (o == null) {
                    return null;
                }
                String[] no = new String[o.length];
                for (int i = 0; i < o.length; ++i) {
                    no[i] = o[i].toString();
                }
                return no;
            });
            MAPPING.set(REGRESSORS, MatrixType.class, source
                    -> {
                List<DoubleSeq> x = source.regarima.getX();
                int n=source.regarima.getY().length(), m=x.size();
                double[] all=new double[n*m];
                int pos=0;
                for (DoubleSeq xcur: x){
                    xcur.copyTo(all, pos);
                    pos+=n;
                }
                return MatrixType.of(all, n, m);
            });
            
            MAPPING.set(LIN, double[].class, source
                    -> {
                return source.getLinearized();
            });
            
        }

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

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

    }

    public Results process(double[] y, Matrix x, boolean mean, double[] periods, String[] outliers, double cv) {
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(periods, true, false);
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(DoubleSeq.of(y))
                .addX(Matrix.of(x))
                .arima(mapping.getDefault())
                .meanCorrection(mean);
        OutlierDescriptor[] o = null;
        if (outliers != null) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .mapping(mapping)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(outliers);
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();
            od.setCriticalValue(cv);
            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima);
            int[][] io = od.getOutliers();
            o=new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length);
                factories[cur[1]].fill(cur[0], xcur);
                o[i]=new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        }
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping)
                .precision(1e-9)
                .build();
        RegArimaEstimation rslt = finalProcessor.process(builder.build());
        return Results.builder()
                .concentratedLogLikelihood(rslt.getConcentratedLikelihood())
                .parameters(mapping.parametersOf((ArimaModel) rslt.getModel().arima()).toArray())
                .regarima(rslt.getModel())
                .parametersCovariance(rslt.getMax().asymptoticCovariance())
                .score(rslt.getMax().getScore())
                .statistics(rslt.statistics())
                .outliers(o)
                .linearized(rslt.linearizedSeries().toArray())
                .build();
    }

    private IOutlierFactory[] factories(String[] code) {
        List<IOutlierFactory> fac = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            switch (code[i]) {
                case "ao":
                case "AO":
                    fac.add(AdditiveOutlierFactory.FACTORY);
                    break;
                case "wo":
                case "WO":
                    fac.add(SwitchOutlierFactory.FACTORY);
                    break;
                case "ls":
                case "LS":
                    fac.add(LevelShiftFactory.FACTORY_ZEROENDED);
                    break;
            }
        }
        return fac.toArray(new IOutlierFactory[fac.size()]);
    }
}
