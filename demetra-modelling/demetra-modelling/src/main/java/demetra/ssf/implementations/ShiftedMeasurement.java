/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.implementations;

import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 * Shifted measurement: Zshift(t) = Z(pos + shift)
 *
 * @author Jean Palate
 */
public class ShiftedMeasurement implements ISsfMeasurement {

    private final ShiftedLoading loading;
    private final ShiftedError error;

    public ShiftedMeasurement(ISsfMeasurement m, int shift) {
        this.loading = new ShiftedLoading(m.loading(), shift);
        ISsfError e = m.error();
        error = e == null ? null : new ShiftedError(e, shift);
    }

    @Override
    public ISsfLoading loading() {
        return loading;
    }

    @Override
    public ISsfError error() {
        return error;
    }

}
