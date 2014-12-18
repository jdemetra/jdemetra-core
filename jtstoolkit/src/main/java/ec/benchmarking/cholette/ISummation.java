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

package ec.benchmarking.cholette;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISummation {
    // / <summary>
    // / rslt = B' A^-1 B
    // / </summary>
    // / <param name="X"></param>
    // / <returns></returns>

    /**
     *
     * @param A
     * @return
     */
    Matrix BAB(IVariance A);

    /**
     *
     * @param i
     * @param x
     * @return
     */
    double Btz(int i, DataBlock x);

    /**
     *
     * @param i
     * @param x
     * @return
     */
    double Bx(int i, DataBlock x);

    /**
     *
     * @return
     */
    int dim();

    /**
     *
     * @return
     */
    int sdim();
}
