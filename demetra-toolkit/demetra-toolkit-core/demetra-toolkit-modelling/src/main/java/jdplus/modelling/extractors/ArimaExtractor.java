/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.modelling.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import jdplus.arima.IArimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class ArimaExtractor {

    public final static String AR="ar", // Stationary auto-regressive polynomial
            DELTA="delta",  // Differencing polynomial
            MA="ma",  // Moving average polynomial
            VAR = "var"; // Innovation variance
    

    private final InformationMapping<IArimaModel> MAPPING = new InformationMapping<>(IArimaModel.class);

    static {
        MAPPING.set(AR, double[].class, source->source.getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        MAPPING.set(DELTA, double[].class, source->source.getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        MAPPING.set(MA, double[].class, source->source.getMa().asPolynomial().coefficients().drop(1, 0).toArray());
        MAPPING.set(VAR, Double.class, source->source.getInnovationVariance());
    }

    public InformationMapping<IArimaModel> getMapping() {
        return MAPPING;
    }


}
