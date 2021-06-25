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

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import jdplus.arima.IArimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class GenericArimaExtractor extends InformationMapping<IArimaModel> {

    public final static String AR = "ar", // Stationary auto-regressive polynomial
            DELTA = "delta", // Differencing polynomial
            MA = "ma", // Moving average polynomial
            VAR = "var"; // Innovation variance

    public GenericArimaExtractor() {
        set(AR, double[].class, source -> source.getStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        set(DELTA, double[].class, source -> source.getNonStationaryAr().asPolynomial().coefficients().drop(1, 0).toArray());
        set(MA, double[].class, source -> source.getMa().asPolynomial().coefficients().drop(1, 0).toArray());
        set(VAR, Double.class, source -> source.getInnovationVariance());
    }

    @Override
    public Class getSourceClass() {
        return IArimaModel.class;
    }

}
