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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.IModelBuilder;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultModelBuilder implements IModelBuilder {

    @Override
    public boolean initialize(ModellingContext context) {
        ModelDescription model = context.description;
        model.setAirline(context.hasseas);
        model.setMean(true);
        return true;
    }
}
