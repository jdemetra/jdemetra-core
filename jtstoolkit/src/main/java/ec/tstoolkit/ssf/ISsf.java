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
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Generic interface of univariate state space forms
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsf extends ISsfBase {

    /**
     *
     * @param pos
     * @param k
     * @param lm Sub-matrix that will receive L. It is the responsibility of the
     * caller to set that sub-matrix to 0.
     */
    void L(int pos, DataBlock k, SubMatrix lm);

    // backward operations
    /**
     *
     * @param pos
     * @param vm
     * @param d
     */
    void VpZdZ(int pos, SubMatrix vm, double d);

    /**
     *
     * @param pos
     * @param x
     * @param d
     */
    void XpZd(int pos, DataBlock x, double d);

    /**
     *
     * @param pos
     * @param x. It is the responsibility of the caller to set x to 0 before
     * calling this method
     */
    void Z(int pos, DataBlock x);

    /**
     *
     * @param pos
     * @param m
     * @param x
     */
    void ZM(int pos, SubMatrix m, DataBlock x);

    /**
     *
     * @param pos
     * @param vm
     * @return
     */
    double ZVZ(int pos, SubMatrix vm);

    // forward operations
    /**
     *
     * @param pos
     * @param x
     * @return
     */
    double ZX(int pos, DataBlock x);
}
