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

package ec.satoolkit;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * A pre-processing filter must extract from a pre-processing model the
 * information that will be used in the decomposition step and in the final
 * (reconciliation) step. Basically, the necessary information should be limited
 * to the part of the series that will be decomposed and to the parts of the
 * series that will be associated with the different components. Default
 * implementations will be straightforward. More sophisticated implementations
 * could include automatic corrections for seasonal effects, non standard
 * associations of the regression variables... The current implementation still
 * consider separate bias corrections. Such effects will be probably disappear
 * in future versions
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface IPreprocessingFilter {

    /**
     * Is the filter initialized?
     *
     * @return True if the filter has been initialized, false otherwise
     */
    boolean isInitialized();

    /**
     * Sets the filter with a pre-processing model.
     *
     * @param model The model that will be filtered
     * @return True if the model can be filtered, false otherwise
     */
    boolean process(PreprocessingModel model);

    /**
     * Gets the series that will be decomposed. Typically, the "linearized"
     * series (or series corrected for regression effects)
     *
     * @param transformed True if the (log-...)transformed series is returned,
     * false otherwise (series in the original scale).
     * @return A new time series is returned.
     */
    @NewObject
    TsData getCorrectedSeries(boolean transformed);

    /**
     * Gets the forecasts of the corrected series
     *
     * @param transformed True if the (log-...)transformed series is returned,
     * false otherwise (series in the original scale).
     * @return A new time series is returned.
     */
    @NewObject
    TsData getCorrectedForecasts(boolean transformed);

    /**
     * Gets the backcasts of the corrected series
     *
     * @param transformed True if the (log-...)transformed series is returned,
     * false otherwise (series in the original scale).
     * @return A new time series is returned.
     */
    @NewObject
    TsData getCorrectedBackcasts(boolean transformed);
    /**
     * Gets the correction that will be associated to a given component.
     *
     * @param domain The domain for which the correction will be computed.
     * @param type The type of the component. 
     * @param transformed True if the (log-...)transformed series is returned,
     * false otherwise (series in the original scale).
     * @return A new time series is returned. May be null (no correction for the 
     * requested component).
     */
    @NewObject
    TsData getCorrection(TsDomain domain, ComponentType type, boolean transformed);

    @Deprecated
    double getBiasCorrection(ComponentType type);
    //TsData filter(String id, TsData data);
}
