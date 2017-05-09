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

package demetra.maths.functions;

import demetra.design.Development;
import demetra.data.Doubles;


/**
 * Support parameterization by a set of real values. Parameters are ordered.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IParametric extends Cloneable {
    // / <summary>Number of parameters</summary>
    // / <value>The number of parameters. 0 if the object can not be
    // parameterized.</value>

    /**
     * 
     * @return
     */
    IParametric exemplar();

    // / <summary>Parameter of the object</summary>
    // / <value>The specified parameter.</value>
    // / <remarks>The index of each parameter must belong to the range [0,
    // ParametersCount[</remarks>
    /**
     * 
     * @param idx
     * @return
     */
    double get(int idx);

    // / <value>The full set of the parameters. Null if the object is not
    // parametriseable.</value>
    // / <summary>Full set of the (ordered) parameters.</summary>
    /**
     * 
     * @return
     */
    Doubles getParameters();

    /**
     * 
     * @return
     */
    int getParametersCount();

    /**
     * 
     * @param idx
     * @param value
     */
    void set(int idx, double value);

    /**
     * 
     * @param parameters
     */
    void setParameters(Doubles parameters);
}
