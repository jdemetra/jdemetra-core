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
                    double[] phi=source.getPhi(), bphi=source.getBphi()
                            , th=source.getTheta(), bth=source.getBtheta();
                    int n=phi.length+bphi.length+th.length+bth.length;
                    int pos = 0;
                    double[] all = new double[n];
                    for (int i = 0; i < phi.length; ++i) {
                        all[pos++] = -phi[i];
                    }
                    for (int i = 0; i < bphi.length; ++i) {
                        all[pos++] = -bphi[i];
                    }
                    for (int i = 0; i < th.length; ++i) {
                        all[pos++] = -th[i];
                    }
                    for (int i = 0; i < bth.length; ++i) {
                        all[pos++] = -bth[i];
                    }
                    return all;
                });
        MAPPING.set(PHI, double[].class, source -> source.getPhi());
        MAPPING.set(BPHI, double[].class, source -> source.getBphi());
        MAPPING.set(THETA, double[].class, source -> source.getTheta());
        MAPPING.set(BTHETA, double[].class, source -> source.getBtheta());
    }

    public InformationMapping<SarimaModel> getMapping() {
        return MAPPING;
    }

}
