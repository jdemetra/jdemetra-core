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
import demetra.arima.SarimaProcess;
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

    static final InformationMapping<SarimaProcess> MAPPING = new InformationMapping<>(SarimaProcess.class);

    static {
        MAPPING.set(P, Integer.class, source -> source.getPhi().degree());
        MAPPING.set(D, Integer.class, source -> source.getD());
        MAPPING.set(Q, Integer.class, source -> source.getTheta().degree());
        MAPPING.set(BP, Integer.class, source -> source.getBphi().degree());
        MAPPING.set(BQ, Integer.class, source -> source.getBtheta().degree());
        MAPPING.set(BD, Integer.class, source -> source.getBd());
        MAPPING.set(PARAMETERS, double[].class,
                source -> {
                    SarimaSpecification spec = source.specification();
                    double[] all = new double[spec.getParametersCount()];
                    int pos = 0;
                    for (int i = 1; i <= spec.getP(); ++i) {
                        all[pos++] = -source.getPhi().get(i);
                    }
                    for (int i = 1; i <= spec.getQ(); ++i) {
                        all[pos++] = source.getTheta().get(i);
                    }
                    for (int i = 1; i <= spec.getBp(); ++i) {
                        all[pos++] = -source.getBphi().get(i);
                    }
                    for (int i = 1; i <= spec.getBq(); ++i) {
                        all[pos++] = source.getBtheta().get(i);
                    }
                    return all;
                });
        MAPPING.setArray(PHI, 1, 12, Double.class,
                (source, i) -> i >source.getPhi().degree() ? 0 : source.getPhi().get(i));
        MAPPING.setArray(BPHI, 1, 12, Double.class,
                (source, i) -> i >source.getBphi().degree() ? 0 : source.getBphi().get(i));
        MAPPING.setArray(THETA, 1, 12, Double.class,
                (source, i) -> i >source.getTheta().degree() ? 0 : source.getTheta().get(i));
        MAPPING.setArray(BTHETA, 1, 12, Double.class,
                (source, i) -> i >source.getBtheta().degree() ? 0 : source.getBtheta().get(i));

    }

    public InformationMapping<SarimaProcess> getMapping() {
        return MAPPING;
    }

}
