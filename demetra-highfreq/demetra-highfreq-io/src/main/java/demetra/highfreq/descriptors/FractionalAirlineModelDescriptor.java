/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.descriptors;

import demetra.data.DoubleSeq;
import demetra.descriptors.stats.LikelihoodStatisticsDescriptor;
import demetra.highfreq.FractionalAirlineModel;
import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.modelling.OutlierDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineModelDescriptor {
        private static final String PARAMETERS = "parameters", LL = "likelihood", PCOV = "pcov", SCORE = "score",
                B = "b", T = "t", UNSCALEDBVAR = "unscaledbvar", OUTLIERS = "outliers"
                , LIN="lin", REGRESSORS="regressors";

        private static final InformationMapping<FractionalAirlineModel> MAPPING = new InformationMapping<>(FractionalAirlineModel.class);

         public boolean contains(String id) {
            return MAPPING.contains(id);
        }

         public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        public <T> T getData(FractionalAirlineModel model, String id, Class<T> tclass) {
            return MAPPING.getData(model, id, tclass);
        }

        public static final InformationMapping<FractionalAirlineModel> getMapping() {
            return MAPPING;
        }

        static {
            MAPPING.delegate(LL, LikelihoodStatisticsDescriptor.getMapping(), r -> r.getStatistics());
            MAPPING.set(PCOV, MatrixType.class, source -> source.getParametersCovariance());
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
                return source.getRegarima().getX();
            });            
            MAPPING.set(LIN, double[].class, source
                    -> {
                return source.getLinearized();
            });
            
        }

}
