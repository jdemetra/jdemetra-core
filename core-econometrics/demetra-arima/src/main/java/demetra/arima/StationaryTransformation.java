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


package demetra.arima;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.maths.linearfilters.BackFilter;


/**
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class StationaryTransformation<S extends ILinearModel> {

    /**
     *
     */
    private final S stationaryModel;

    /**
     *
     */
    private final BackFilter unitRoots;

    /**
     *
     * @param stationaryModel
     * @param unitRoots
     */
    public StationaryTransformation(final S stationaryModel,
	    final BackFilter unitRoots) {
	this.stationaryModel = stationaryModel;
	this.unitRoots = unitRoots;
    }

    /**
     * @return the stationaryModel
     */
    public S getStationaryModel() {
        return stationaryModel;
    }

    /**
     * @return the unitRoots
     */
    public BackFilter getUnitRoots() {
        return unitRoots;
    }
}
