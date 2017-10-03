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
import demetra.data.DoubleSequence;


/**
 * Support parameterization by a set of real values. Parameters are ordered.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IParametric extends Cloneable {

    /**
     * 
     * @param idx
     * @return
     */
    double get(int idx);

    /**
     * 
     * @return
     */
    DoubleSequence getParameters();

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
    void setParameters(DoubleSequence parameters);
}
