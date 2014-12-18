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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsfBase;

/**
 * Generic interface of multivariate state space forms
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IMultivariateSsf extends ISsfBase
{
    /**
     *
     * @return
     */
    int getVarsCount();

    // information
    /**
     *
     * @param pos
     * @param v
     * @return
     */
    boolean hasZ(int pos, int v);

    /**
     *
     * @param pos
     * @param K
     * @param lm
     */
    void L(int pos, SubMatrix K, SubMatrix lm);

    /**
     *
     * @param pos
     * @param M
     */
    void MT(int pos, SubMatrix M);

    /**
     *
     * @param pos
     * @param M
     */
    void TM(int pos, SubMatrix M);

    // backward operations
    /**
     *
     * @param pos
     * @param v
     * @param w
     * @param vm
     * @param d
     */
    void VpZdZ(int pos, int v, int w, SubMatrix vm, double d);

    /**
     *
     * @param pos
     * @param v
     * @param x
     * @param d
     */
    void XpZd(int pos, int v, DataBlock x, double d);

    /**
     *
     * @param pos
     * @param v
     * @param z
     */
    void Z(int pos, int v, DataBlock z);

    // Matrix version
    /**
     *
     * @param pos
     * @param zm
     */
    void Z(int pos, SubMatrix zm);

    /**
     *
     * @param pos
     * @param v
     * @param m
     * @param x
     */
    void ZM(int pos, int v, SubMatrix m, DataBlock x);

    /**
     *
     * @param pos
     * @param m
     * @param zm
     */
    void ZM(int pos, SubMatrix m, SubMatrix zm);

    /**
     *
     * @param pos
     * @param v
     * @param w
     * @param vm
     * @return
     */
    double ZVZ(int pos, int v, int w, SubMatrix vm);

    /**
     *
     * @param pos
     * @param v
     * @param zvz
     */
    void ZVZ(int pos, SubMatrix v, SubMatrix zvz);

    // forward operations
    /**
     *
     * @param pos
     * @param v
     * @param x
     * @return
     */
    double ZX(int pos, int v, DataBlock x);

    /**
     *
     * @param pos
     * @param x
     * @param y
     * @return
     */
    void ZX(int pos, DataBlock x, DataBlock zx);

}
