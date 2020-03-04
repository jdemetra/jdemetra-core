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
package demetra.toolkit.extractors;

import demetra.arima.SarimaModel;
import demetra.information.InformationMapping;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SarimaExtractor {

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
            PERIOD = "period", NAME = "name";

    private final InformationMapping<SarimaModel> MAPPING = new InformationMapping<>(SarimaModel.class);

    static {
        MAPPING.set(P, Integer.class, source -> source.getP());
        MAPPING.set(D, Integer.class, source -> source.getD());
        MAPPING.set(Q, Integer.class, source -> source.getQ());
        MAPPING.set(PERIOD, Integer.class, source -> source.getPeriod());
        MAPPING.set(BP, Integer.class, source -> source.getBp());
        MAPPING.set(BD, Integer.class, source -> source.getBd());
        MAPPING.set(BQ, Integer.class, source -> source.getBq());
        MAPPING.set(PARAMETERS, double[].class, source -> source.parameters(true));
        MAPPING.set(PARAMETERS2, double[].class, source -> source.parameters(false));
        MAPPING.set(PHI, double[].class, source -> source.getPhi());
        MAPPING.set(BPHI, double[].class, source -> source.getBphi());
        MAPPING.set(THETA, double[].class, source -> source.getTheta());
        MAPPING.set(BTHETA, double[].class, source -> source.getBtheta());
        MAPPING.set(NAME, String.class, source->source.getName());
    }

    public InformationMapping<SarimaModel> getMapping() {
        return MAPPING;
    }


}
