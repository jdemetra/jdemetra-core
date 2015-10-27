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


package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
public class MeanController  implements IPreprocessingModule{
    
    static final double CVAL0=1.96, CVAL1=1.6;

    private final double cval_;

    public MeanController(double cval){
        cval_=cval;
    }

    public ProcessingResult process(ModellingContext context) {
        
        ModelDescription desc=context.description;
        ModelEstimation est=context.estimation;
        boolean mean=desc.isMean();
        if (!mean){
            desc=desc.clone();
            desc.setMean(true);
            est=null;
        }
        if (est == null){
            IParametricMapping<SarimaModel> mapping=X13Preprocessor.createDefaultMapping(context.description);
        RegArimaEstimator monitor=new RegArimaEstimator(mapping);
            est=new ModelEstimation(desc.buildRegArima(), desc.getLikelihoodCorrection());
            if (! est.compute(monitor, mapping.getDim())) {
                return ProcessingResult.Failed;
            }
        }
        double ser=est.getLikelihood().getBSer(0, false, 0);
        double mu=est.getLikelihood().getB()[0];
        double t=Math.abs(mu/ser);
        boolean nmean=t > cval_;
        if (nmean == mean) {
            return ProcessingResult.Unchanged;
        }
        else{
            context.description.setMean(nmean);
            context.estimation=null;
            return ProcessingResult.Changed;
        }
   }
}
