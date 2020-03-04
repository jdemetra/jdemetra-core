/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.extractors;

import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.information.InformationMapping;
import demetra.toolkit.extractors.UcarimaExtractor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class FractionalAirlineDecompositionExtractor {
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        public <T> T getData(FractionalAirlineDecomposition decomposition, String id, Class<T> tclass) {
            return MAPPING.getData(decomposition, id, tclass);
        }

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa",
                UCARIMA = "ucarima", ARIMA = "arima",
                LL = "likelihood", PCOV = "pcov", SCORE = "score";

        public static final InformationMapping<FractionalAirlineDecomposition> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<FractionalAirlineDecomposition> MAPPING = new InformationMapping<>(FractionalAirlineDecomposition.class);

        static {
            MAPPING.set(Y, double[].class, source -> source.getY());
            MAPPING.set(T, double[].class, source -> source.getT());
            MAPPING.set(S, double[].class, source -> source.getS());
            MAPPING.set(I, double[].class, source -> source.getI());
            MAPPING.set(SA, double[].class, source -> source.getSa());
            MAPPING.delegate(UCARIMA, UcarimaExtractor.getMapping(), source -> source.getUcarima());
        }
}
