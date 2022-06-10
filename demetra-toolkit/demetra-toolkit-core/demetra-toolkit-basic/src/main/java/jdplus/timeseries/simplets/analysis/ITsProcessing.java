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

package jdplus.timeseries.simplets.analysis;

import demetra.information.Explorable;
import demetra.timeseries.TsDomain;
import nbbrd.design.Development;


/**
 * A TSProcessing is an algorithm able to produce an output for any (suitable)
 * time span.
 * @author Jean Palate
 * @param <I>
 */
@Development(status = Development.Status.Preliminary)
public interface ITsProcessing<I extends Explorable >{
    /**
     *
     * @param domain
     * @return
     */
    I process(TsDomain domain);
}
