/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.extractors;

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
            B = "b", T = "t", BVAR = "bvar", OUTLIERS = "outliers", LIN = "lin", REGRESSORS = "regressors";

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
        MAPPING.delegate(LL, LikelihoodStatisticsExtractor.getMapping(), r -> r.getRegarima().getLikelihood());
        MAPPING.set(PCOV, MatrixType.class, source -> source.getRegarima().getParameters().getCovariance());
        MAPPING.set(PARAMETERS, double[].class, source -> source.getRegarima().getParameters().getValues());
        MAPPING.set(B, double[].class, source
                -> {
            return source.getRegarima().getCoefficients().getValues();
        });
        MAPPING.set(T, double[].class, source
                -> {
            double[] b = source.getRegarima().getCoefficients().getValues();
            if (b == null) {
                return null;
            }
            DoubleSeq v = source.getRegarima().getCoefficients().getCovariance().diagonal();
            double[] t = b.clone();
            for (int i = 0; i < t.length; ++i) {
                t[i] /= Math.sqrt(v.get(i));
            }
            return t;
        });
        MAPPING.set(BVAR, MatrixType.class, source -> source.getRegarima().getCoefficients().getCovariance());
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
            return source.getRegarima().getX();
        });
        MAPPING.set(LIN, double[].class, source
                -> {
            return source.getRegarima().linearized();
        });

    }

}
