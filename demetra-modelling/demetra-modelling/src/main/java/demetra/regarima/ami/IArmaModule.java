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
package demetra.regarima.ami;

import demetra.data.DoubleSequence;
import demetra.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */

public interface IArmaModule {
    
    /**
     * 
     * @param data The stationary data
     * @param period The period of the series
     * @param d The regular differencing applied on the original data to get the stationary series
     * @param bd The seasonal differencing applied on the original data to get the stationary series
     * @param seas The fact that seasonal part should be considered or not
     * @return The best ARMA specification
     */
    SarmaSpecification process(DoubleSequence data, int period, int d, int bd, boolean seas);
}
