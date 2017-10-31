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


package demetra.x11;

import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.timeseries.simplets.TsData;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ITrendCycleComputer extends IX11Algorithm {

    /**
     *
     * @param step
     * @param s
     * @param info
     * @return
     */
    TsData doFinalFiltering(X11Step step, TsData s, InformationSet info);

    /**
     * Default filter. Used in the first step of the different parts
     * 
     * @param step
     * @param s
     * @param info
     * @return
     */
    TsData doInitialFiltering(X11Step step, TsData s, InformationSet info);
}
