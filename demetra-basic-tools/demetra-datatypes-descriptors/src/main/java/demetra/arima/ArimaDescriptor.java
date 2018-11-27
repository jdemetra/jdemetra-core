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
package demetra.arima;

import demetra.arima.ArimaType;
import demetra.design.Development;
import demetra.information.InformationMapping;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Release)
public class ArimaDescriptor {

    final static String AR="ar", // Stationary auto-regressive polynomial
            DELTA="delta",  // Differencing polynomial
            MA="ma",  // Moving average polynomial
            VAR = "var", // Innovation variance
            DESC = "desc"; // Optional description/name

    static final InformationMapping<ArimaType> MAPPING = new InformationMapping<>(ArimaType.class);

    static {
        MAPPING.set(AR, double[].class, source->source.getAr().toArray());
        MAPPING.set(DELTA, double[].class, source->source.getDelta().toArray());
        MAPPING.set(MA, double[].class, source->source.getMa().toArray());
        MAPPING.set(VAR, Double.class, source->source.getInnovationVariance());
        MAPPING.set(DESC, String.class, source->source.getName());
    }

    public InformationMapping<ArimaType> getMapping() {
        return MAPPING;
    }

}
