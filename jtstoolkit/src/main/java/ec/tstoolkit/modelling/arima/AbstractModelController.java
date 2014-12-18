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

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractModelController implements IModelController {

    private IModelEstimator estimator_;
    private PreprocessingModel refmodel_;

    @Override
    public PreprocessingModel getReferenceModel() {
        return refmodel_;
    }

    @Override
    public void setReferenceModel(PreprocessingModel model) {
        refmodel_ = model;
    }

    @Override
    public IModelEstimator getEstimator() {
        return estimator_;
    }

    @Override
    public void setEstimator(IModelEstimator estimator) {
        estimator_ = estimator;
    }

    /**
     * 
     * @param context
     * @return True if the mean is significant 
     */
    protected boolean checkMean(ModellingContext context) {
        if (!context.description.isMean()) {
            return true;
        }
        double ser = context.estimation.getLikelihood().getBSer(0, true, context.description.getArimaComponent().getFreeParametersCount());
        return Math.abs(context.estimation.getLikelihood().getB()[0] / ser) >= 1.96;
    }

    protected boolean estimate(ModellingContext context, boolean checkmean) {
        if (!estimator_.estimate(context)) {
            return false;
        }
        if (checkmean) {
            if (!checkMean(context)) {
                context.description.setMean(false);

                if (!estimator_.estimate(context)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected void transferInformation(ModellingContext from, ModellingContext to){
            to.description = from.description;
            to.estimation = from.estimation;
        to.information.clear();
        to.information.copy(from.information);
    }
}
