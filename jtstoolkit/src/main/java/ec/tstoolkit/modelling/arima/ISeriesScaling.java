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


package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.design.Development;

/**
 * Rescale a time series.
 * The rescaling of the original series should always be done, to avoid possible
 * numerical problems in the estimation steps. Moreover, rescaling should suppress
 * unexpected behavior (other models) when the units of a series are changed.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public interface ISeriesScaling {
    public static final String INFO_SUBSET="transformation";

    boolean process(ModellingContext context);

}
