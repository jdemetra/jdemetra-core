/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.extractors;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.tempdisagg.univariate.ResidualsDiagnostics;
import jdplus.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.Variable;
import nbbrd.service.ServiceProvider;
import demetra.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class TemporalDisaggregationExtractor extends InformationMapping<TemporalDisaggregationResults> {

    public final String LIKELIHOOD = "likelihood", DISAGG = "disagg", EDISAGG = "edisagg",
            RES = "residuals", ML = "ml", COEFF = "coeff", COVAR = "covar", REGEFFECT = "regeffect", SPART = "smoothingpart",
            REGNAMES = "regnames", PARAMETER = "parameter", EPARAMETER = "eparameter";

    public TemporalDisaggregationExtractor() {
        set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        set(REGEFFECT, TsData.class, source -> source.getRegressionEffects());
        set(COEFF, double[].class, source -> source.getCoefficients().toArray());
        set(COVAR, Matrix.class, source -> source.getCoefficientsCovariance());
        set(REGNAMES, String[].class, source -> {
            Variable[] vars = source.getIndicators();
            int n = vars == null ? 0 : vars.length;
            if (n == 0) {
                return null;
            }
            String[] names = new String[n];
            for (int i = 0; i < names.length; ++i) {
                names[i] = vars[i].getName();
            }
            return names;
        });
        set(PARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            double[] p = source.getMaximum().getParameters();
            return p.length == 0 ? Double.NaN : p[0];
        });
        set(EPARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            Matrix H = source.getMaximum().getHessian();
            return (H == null || H.isEmpty()) ? Double.NaN : Math.sqrt(1 / source.getMaximum().getHessian().get(0, 0));
        });
        set(SPART, Double.class, source -> {
            TsData re = source.getRegressionEffects();
            if (re == null)
                return null;
            DoubleSeq T = source.getDisaggregatedSeries().getValues();
            DoubleSeq R = re.getValues();
            DoubleSeq S = DoublesMath.subtract(T, R);
            double vart = T.ssq();
            double vars = S.ssq();
            return Math.sqrt(vars / vart);
        });
        delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getLikelihood());
        delegate(RES, ResidualsDiagnostics.class, source -> source.getResidualsDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return TemporalDisaggregationResults.class;
    }

}
