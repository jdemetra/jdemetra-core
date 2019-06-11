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
package demetra.descriptors.arima;

import demetra.information.InformationMapping;
import demetra.arima.SarimaModel;
import demetra.arima.SarimaSpecification;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SarimaDescriptor {

    final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters",
            PHI="phi", THETA="theta",BPHI="bphi", BTHETA="btheta",
            PERIOD = "period";

    static final InformationMapping<SarimaModel> MAPPING = new InformationMapping<>(SarimaModel.class);

    static {
        MAPPING.set(P, Integer.class, source -> source.getP());
        MAPPING.set(D, Integer.class, source -> source.getD());
        MAPPING.set(Q, Integer.class, source -> source.getQ());
        MAPPING.set(BP, Integer.class, source -> source.getBp());
        MAPPING.set(BQ, Integer.class, source -> source.getBq());
        MAPPING.set(BD, Integer.class, source -> source.getBd());
        MAPPING.set(PARAMETERS, double[].class,
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
                    for (int i = 1; i <= spec.getBp(); ++i) {
                        all[pos++] = -source.bphi(i);
                    }
                    for (int i = 1; i <= spec.getBq(); ++i) {
                        all[pos++] = source.btheta(i);
                    }
                    return all;
                });
        MAPPING.setArray(PHI, 1, 12, Double.class,
                (source, i) -> i >source.getP() ? 0 : source.phi(i));
        MAPPING.setArray(BPHI, 1, 12, Double.class,
                (source, i) -> i >source.getBp() ? 0 : source.bphi(i));
        MAPPING.setArray(THETA, 1, 12, Double.class,
                (source, i) -> i >source.getQ() ? 0 : source.theta(i));
        MAPPING.setArray(BTHETA, 1, 12, Double.class,
                (source, i) -> i >source.getBq() ? 0 : source.btheta(i));

    }

    public InformationMapping<SarimaModel> getMapping() {
        return MAPPING;
    }

}
