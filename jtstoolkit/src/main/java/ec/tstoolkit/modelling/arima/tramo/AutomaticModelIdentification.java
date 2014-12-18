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
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class AutomaticModelIdentification extends AbstractTramoModule implements IPreprocessingModule {

    private final IPreprocessingModule diff_;
    private final IPreprocessingModule arma_;

    public AutomaticModelIdentification() {
        diff_ = new DifferencingModule();
        arma_ = new ArmaModule();
    }

    public AutomaticModelIdentification(IPreprocessingModule differencing, IPreprocessingModule arma) {
        diff_ = differencing;
        arma_ = arma;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (context.estimation == null){
            ModelEstimation estimation =new ModelEstimation(context.description.buildRegArima(),
                    context.description.getLikelihoodCorrection());
            estimation.compute(getMonitor(), context.description.getArimaComponent().getFreeParametersCount());
        }
        ProcessingResult drslt = diff_.process(context);
        ProcessingResult arslt = arma_.process(context);
        if (drslt == ProcessingResult.Changed) {
            if (arslt.isProcessed()) {
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Failed;
            }
        } else {
            return arslt;
        }
    }
}
