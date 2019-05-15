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

import jd.data.DataBlock;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.univariate.ISsfMeasurement;
import jd.maths.matrices.FastMatrix;

/**
 * Shifted measurement: Zshift(t) = Z(pos + shift) 
 *
 * @author Jean Palate
 */
public class ShiftedError implements ISsfError {

    private final ISsfError error;
    private final int shift;
    
    public ShiftedError(ISsfError error, int shift) {
        this.error = error;
        this.shift = shift;
    }
    
    @Override
    public boolean isTimeInvariant() {
        return error.isTimeInvariant();
    }
    
    
    @Override
    public double at(int pos) {
        return error.at(pos + shift);
    }

}
