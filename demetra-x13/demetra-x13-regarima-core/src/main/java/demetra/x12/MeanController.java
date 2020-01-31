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


package demetra.x12;

import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.regular.IRegressionModule;
import jdplus.regarima.regular.ProcessingResult;
import jdplus.regarima.RegArimaUtility;
import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.ModelEstimation;
import jdplus.regarima.regular.RegArimaModelling;
import jdplus.sarima.SarimaModel;


/**
 *
 * @author Jean Palate
 */
public class MeanController  implements IRegressionModule{
    
    static final double CVAL0=1.96, CVAL1=1.6;

    private final double cval;
    private final double eps=1e-5;

    public MeanController(double cval){
        this.cval=cval;
    }

    public ProcessingResult test(RegArimaModelling context) {
        
        ModelDescription desc=context.getDescription();
        RegArimaEstimation<SarimaModel> est=context.getEstimation();
        boolean mean=desc.isMean();
        if (!mean){
            desc=new ModelDescription(desc);
            desc.setMean(true);
            est=null;
        }
        if (est == null){
            est=desc.estimate(RegArimaUtility.processor(desc.getArimaComponent().defaultMapping(), true, eps));
        }
            double t=est.getConcentratedLikelihood().tstat(0, 0, false);
        boolean nmean=Math.abs(t) > cval;
        if (nmean == mean) {
            return ProcessingResult.Unchanged;
        }
        else{
            context.getDescription().setMean(nmean);
            context.clearEstimation();
            return ProcessingResult.Changed;
        }
   }
}
