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


package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.maths.linearfilters.BackFilter;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class StationaryTransformation {

    /**
     *
     */
    public final ILinearModel stationaryModel;

    /**
     *
     */
    public final BackFilter unitRoots;

    /**
     *
     * @param stationaryModel
     * @param unitRoots
     */
    public StationaryTransformation(final ILinearModel stationaryModel,
	    final BackFilter unitRoots) {
	this.stationaryModel = stationaryModel;
	this.unitRoots = unitRoots;
    }
}
