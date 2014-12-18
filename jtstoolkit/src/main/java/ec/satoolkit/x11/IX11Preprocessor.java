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


package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;

/**
 * The pre-processor module is in charge of the extension of the original series.
 * It must generate the series a1a and b1, using the series a1.
 * The complete pre-processing of the series is realized by means of the Reg-Arima
 * component.
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IX11Preprocessor extends IX11Algorithm {
    /**
     * Starting from the series a1 (in a-tables), the preprocessing will complete 
     * the series a1a (in a-tables)(if forecasts) and b1 (in b-tables).
     * @param info The information set that contains the input (a1) as well as 
     * the output ([a1a], b1)
     */
    void preprocess(InformationSet info);
}
