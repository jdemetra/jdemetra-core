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

package jdplus.sa;

import demetra.design.Development;
import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface SeriesDecomposition {
    /**
     *
     * @return
     */
    DecompositionMode getMode();

    /**
     *
     * @param cmp
     * @param info
     * @return
     */
    TsData getSeries(ComponentType cmp, ComponentInformation info);
}
