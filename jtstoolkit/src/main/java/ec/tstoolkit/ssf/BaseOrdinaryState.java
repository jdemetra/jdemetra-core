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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BaseOrdinaryState extends BaseState {

    /**
     *
     */
    public DataBlock C;
    /**
     *
     */
    public double f;

    /**
     *
     */
    protected BaseOrdinaryState() {
    }

    /**
     *
     * @param state
     */
    protected BaseOrdinaryState(final BaseOrdinaryState state) {
        super(state);
        C = state.C.deepClone();
        f = state.f;
    }

    /**
     *
     * @param n
     * @param hasdata
     */
    protected BaseOrdinaryState(final int n, final boolean hasdata) {
        super(hasdata ? n : 0);
        C = new DataBlock(n);
    }

    /**
     *
     * @param state
     */
    public void copy(final BaseOrdinaryState state) {
        super.copy(state);
        C = state.C.deepClone();
        f = state.f;
    }
}
