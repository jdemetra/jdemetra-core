/*
* Copyright 2013 National Bank of Belgium
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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ArrayState extends BaseArrayState {

    /**
     * Cholesky factor of P, the covariance of the state prediction a(t+1|t)
     */
    public SubMatrix S;

    /**
     * 
     * @param n
     * @param hasdata
     */
    public ArrayState(final int n, final boolean hasdata)
    {
	super(n, hasdata);
	S = new Matrix(n, n).subMatrix();
    }

    public ArrayState(final SubMatrix s, final boolean hasdata)
    {
	super(s.getRowsCount(), hasdata);
	S = s;
    }
    
    /**
     * 
     * @param state
     */
    public void copy(final ArrayState state)
    {
	super.copy(state);
	S.copy(state.S);

    }
}
