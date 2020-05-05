/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.extractors;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.tempdisagg.univariate.TemporalDisaggregationResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.Variable;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TemporalDisaggregationExtractor {

    public final String LIKELIHOOD = "likelihood", DISAGG = "disagg", EDISAGG = "edisagg",
            RES = "residuals", ML = "ml", COEFF = "coeff", COVAR = "covar", REGEFFECT = "regeffect", SPART="smoothingpart",
            REGNAMES = "regnames", PARAMETER = "parameter", EPARAMETER = "eparameter";

    private final InformationMapping<TemporalDisaggregationResults> MAPPING = new InformationMapping<>(TemporalDisaggregationResults.class);

    static {
        MAPPING.set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        MAPPING.set(EDISAGG, TsData.class, source -> source.getStdevDisaggregatedSeries());
        MAPPING.set(REGEFFECT, TsData.class, source -> source.getRegressionEffects());
        MAPPING.set(COEFF, double[].class, source -> source.getCoefficients().toArray());
        MAPPING.set(COVAR, MatrixType.class, source -> source.getCoefficientsCovariance());
        MAPPING.set(REGNAMES, String[].class, source -> {
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
        MAPPING.set(PARAMETER, Double.class, source -> {
           if (source.getMaximum() == null)
                return Double.NaN;
            double[] p = source.getMaximum().getParameters();
            return p.length == 0 ? Double.NaN : p[0];
        });
        MAPPING.set(EPARAMETER, Double.class, source -> {
            if (source.getMaximum() == null)
                return Double.NaN;
            MatrixType H = source.getMaximum().getHessian();
            return (H == null || H.isEmpty()) ? Double.NaN : Math.sqrt(1 / source.getMaximum().getHessian().get(0, 0));
        });
        MAPPING.set(SPART, Double.class, source -> {
            DoubleSeq T = source.getDisaggregatedSeries().getValues();
            DoubleSeq R = source.getRegressionEffects().getValues();
            DoubleSeq S= DoublesMath.subtract(T, R);
            double vart=T.ssq();
            double vars=S.ssq();
            return Math.sqrt(vars/vart);          
        });
        MAPPING.delegate(LIKELIHOOD, LikelihoodStatisticsExtractor.getMapping(), source -> source.getLikelihood());
        MAPPING.delegate(RES, ResidualsDiagnosticsExtractor.getMapping(), source -> source.getResidualsDiagnostics());
    }

    public InformationMapping<TemporalDisaggregationResults> getMapping() {
        return MAPPING;
    }

}
