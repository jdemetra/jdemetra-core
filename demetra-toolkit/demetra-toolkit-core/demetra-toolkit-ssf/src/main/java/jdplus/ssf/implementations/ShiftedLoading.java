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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.maths.matrices.FastMatrix;

/**
 * Shifted measurement: Zshift(t) = Z(pos + shift) 
 *
 * @author Jean Palate
 */
public class ShiftedLoading implements ISsfLoading {

    private final ISsfLoading loading;
    private final int shift;
    
    public ShiftedLoading(ISsfLoading loading, int shift) {
        this.loading = loading;
        this.shift = shift;
    }
    
    @Override
    public boolean isTimeInvariant() {
        return loading.isTimeInvariant();
    }
    
    @Override
    public void Z(int pos, DataBlock z) {
        loading.Z(pos + shift, z);
    }
    
    @Override
    public double ZX(int pos, DataBlock b) {
        return loading.ZX(pos + shift, b);
    }
    
    @Override
    public double ZVZ(int pos, FastMatrix V) {
        return loading.ZVZ(pos + shift, V);
    }
    
    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
        loading.VpZdZ(pos + shift, V, d);
    }
    
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        loading.XpZd(pos + shift, x, d);
    }

}
