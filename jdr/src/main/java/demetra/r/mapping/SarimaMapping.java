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
package demetra.r.mapping;

import demetra.information.InformationMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
public class SarimaMapping {

    private final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", RPARAMETERS = "rparameters",
            PERIOD = "period";

    private static final InformationMapping<SarimaModel> MAPPING = new InformationMapping<>(SarimaModel.class);

    static {
        MAPPING.set(P, Integer.class, source -> source.getRegularAROrder());
    }

    static {
        MAPPING.set(D, Integer.class, source -> source.getRegularDifferenceOrder());
    }

    static {
        MAPPING.set(Q, Integer.class, source -> source.getRegularMAOrder());
    }

    static {
        MAPPING.set(BP, Integer.class, source -> source.getSeasonalAROrder());
    }

    static {
        MAPPING.set(BP, Integer.class, source -> source.getSeasonalDifferenceOrder());
    }

    static {
        MAPPING.set(BQ, Integer.class, source -> source.getSeasonalMAOrder());
    }

    static {
        MAPPING.set(PARAMETERS, double[].class, source -> source.parameters().toArray());
    }

    static {
        MAPPING.set(RPARAMETERS, double[].class,
                source -> {
                    SarimaSpecification spec = source.specification();
                    double[] all = new double[spec.getParametersCount()];
                    int pos = 0;
                    for (int i = 1; i <= spec.getP(); ++i) {
                        all[pos++] = -source.phi(i);
                    }
                    for (int i = 1; i <= spec.getQ(); ++i) {
                        all[pos++] = source.theta(i);
                    }
                    for (int i = 1; i <= spec.getBP(); ++i) {
                        all[pos++] = -source.bphi(i);
                    }
                    for (int i = 1; i <= spec.getBQ(); ++i) {
                        all[pos++] = source.btheta(i);
                    }
                    return all;
                });
    }

    public static InformationMapping<SarimaModel> getMapping() {
        return MAPPING;
    }

}
