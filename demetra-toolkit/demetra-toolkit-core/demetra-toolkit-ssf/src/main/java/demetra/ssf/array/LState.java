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
package demetra.ssf.array;

import jdplus.data.DataBlock;
import demetra.design.Development;
import jdplus.maths.matrices.FastMatrix;


/**
 * Represents a gaussian vector, with its mean and covariance matrix.
 * The way information must be interpreted is given by the state info.
 * This is similar to the NRV (normal random vector) of Snyder/Forbes (apart from 
 * the additional info)
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LState {
    
    public static final double ZERO= 1e-9;

    /**
     * a is the state vector. a(t|t-1)
     */
    public final DataBlock a;

    /**
     * L is the Cholesky factor of the covariance of the state vector (P(t|t-1)). 
     */
    public final FastMatrix L;


    /**
     *
     *
     * @param dim
     */
    public LState(final FastMatrix L) {
        a = DataBlock.make(L.getRowsCount());
        this.L = L;
    }

    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("mean:\r\n").append(a).append("\r\n");
        builder.append("Cholesky factor of the covariance:\r\n").append(L);
        return builder.toString();
   }
    
}
