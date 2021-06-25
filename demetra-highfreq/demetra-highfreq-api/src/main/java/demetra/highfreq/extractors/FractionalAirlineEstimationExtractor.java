/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.extractors;

import demetra.highfreq.FractionalAirlineEstimation;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class FractionalAirlineEstimationExtractor extends InformationMapping<FractionalAirlineEstimation> {

    @Override
    public Class<FractionalAirlineEstimation> getSourceClass() {
        return FractionalAirlineEstimation.class;
    }

    private static final String PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score",
            B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", LIN = "lin", REGRESSORS = "regressors", Y = "y", BNAMES = "variables";

    public FractionalAirlineEstimationExtractor() {
        delegate(LL, LikelihoodStatistics.class, r -> r.getLikelihood());
        set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
        set(PARAMETERS, double[].class, source -> source.getParameters().toArray());
        set(SCORE, double[].class, source -> source.getScore().toArray());
        set(B, double[].class, source -> source.getCoefficients().toArray());
        set(T, double[].class, source -> source.tstats());
        set(BVAR, MatrixType.class, source -> source.getCoefficientsCovariance());
        set(OUTLIERS, String[].class, source -> {
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
        set(REGRESSORS, MatrixType.class, source -> source.getX());
        set(LIN, double[].class, source -> source.linearized());
        set(Y, double[].class, source -> source.getY());
        set(BNAMES, String[].class, source -> {
            int nx = source.getNx();
            if (nx == 0) {
                return null;
            }
            String[] names = new String[nx];
            OutlierDescriptor[] outliers = source.getOutliers();
            int no = outliers == null ? 0 : outliers.length;
            for (int i = 0; i < nx - no; ++i) {
                names[i] = "x-" + (i + 1);
            }
            for (int i = nx - no, j = 0; i < nx; ++i, ++j) {
                names[i] = outliers[j].toString();
            }

            return names;
        });

    }
}
