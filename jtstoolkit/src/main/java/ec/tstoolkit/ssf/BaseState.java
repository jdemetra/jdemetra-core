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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BaseState {

    /**
     *
     */
    public static final AtomicLong fnCalls = new AtomicLong(0);

    /**
     *
     */
    public DataBlock A;

    /**
     *
     */
    public double e;

    /**
     *
     */
    public static final double EPS = 1e-6;

    public static final double ZERO=1e-9;
   /**
     * 
     */
    protected BaseState()
    {
    }

    /**
     * 
     * @param state
     */
    protected BaseState(final BaseState state)
    {
	e = state.e;
	A = state.A.deepClone();
    }

    /**
     * 
     * @param n
     */
    protected BaseState(final int n)
    {
	A = new DataBlock(n);
    }

    /**
     * 
     * @param state
     */
    public void copy(final BaseState state)
    {
	e = state.e;
	A = state.A.deepClone();
    }

    /**
     * 
     * @return
     */
    public boolean isMissing()
    {
	return Double.isNaN(e);
    }
}
