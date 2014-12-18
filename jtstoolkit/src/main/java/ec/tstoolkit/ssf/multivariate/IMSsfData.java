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

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface IMSsfData
{

    /**
     *
     * @param v
     * @return
     */
    int count(int v);

    /**
     *
     * @param v
     * @param pos
     * @return
     */
    double get(int v, int pos);

    /**
     *
     * @return
     */
    double[] getInitialState();

    /**
     *
     * @return
     */
    int getVarsCount();

    /**
     *
     * @return
     */
    boolean hasData();

    /**
     *
     * @param v
     * @param pos
     * @return
     */
    boolean isMissing(int v, int pos);

    /**
     *
     * @param v
     * @return
     */
    int obsCount(int v);
}
