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

import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaModelling;


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
        ModelEstimation est=context.getEstimation();
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
            context.setEstimation(null);
            return ProcessingResult.Changed;
        }
   }
}
