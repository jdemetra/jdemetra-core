/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.extractors;

import demetra.data.DoubleSeq;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineModelExtractor {

    private static final String PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score",
            B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", LIN = "lin", REGRESSORS = "regressors", Y = "y", BNAMES = "variables";

    private static final InformationMapping<FractionalAirlineEstimation> MAPPING = new InformationMapping<>(FractionalAirlineEstimation.class);

    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    public Map<String, Class> getDictionary() {
        Map<String, Class> dic = new LinkedHashMap<>();
        MAPPING.fillDictionary(null, dic, true);
        return dic;
    }

    public <T> T getData(FractionalAirlineEstimation model, String id, Class<T> tclass) {
        return MAPPING.getData(model, id, tclass);
    }

    public static final InformationMapping<FractionalAirlineEstimation> getMapping() {
        return MAPPING;
    }

    static {
        MAPPING.delegate(LL, LikelihoodStatisticsExtractor.getMapping(), r -> r.getLikelihood());
        MAPPING.set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
        MAPPING.set(PARAMETERS, double[].class, source -> source.getParameters());
        MAPPING.set(SCORE, double[].class, source -> source.getScore());
        MAPPING.set(B, double[].class, source -> source.getCoefficients());
        MAPPING.set(T, double[].class, source -> source.tstats());
        MAPPING.set(BVAR, MatrixType.class, source -> source.getCoefficientsCovariance());
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
        MAPPING.set(REGRESSORS, MatrixType.class, source -> source.getX());
        MAPPING.set(LIN, double[].class, source -> source.linearized());
        MAPPING.set(Y, double[].class, source -> source.getY());
        MAPPING.set(BNAMES, String[].class, source -> {
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
