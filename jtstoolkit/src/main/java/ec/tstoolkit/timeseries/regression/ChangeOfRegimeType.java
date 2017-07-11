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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 * @deprecated Since 2.2.0 Use TsVariableWindow
 */
@Deprecated
@Development(status = Development.Status.Release)
public enum ChangeOfRegimeType {
    /**
     * The values of the series are replaced by 0 up to a specified date. 
     * For a specified TsFrequency, the period that contains the given date is taken
     * into account. The place of the date in the period has no impact. That period is not set to 0.
     */
    ZeroStarted,
    /**
     * The values of the series are replaced by 0 after a specified date.
     * For a specified TsFrequency, the period that contains the given date is taken
     * into account. The place of the date in the period has no impact. That period is set to 0.
     */
    ZeroEnded
}
