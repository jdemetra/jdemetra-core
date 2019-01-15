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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
public class AutoModel implements IPreprocessingModule {

    private final DifferencingModule iddiff;
    private final ArmaModule amdid;
  
    public double getEpsilon(){
        return iddiff.getEpsilon();
    }

    public void setEpsilon(double val){
        iddiff.setEpsilon(val);
        amdid.setEpsilon(val);
    }
    
    public boolean isBalanced(){
        return amdid.isBalanced();
    }

    public void setBalanced(boolean balanced) {
        this.amdid.setBalanced(balanced);
    }

    public boolean isMixed(){
        return amdid.isMixed();
    }

    public void setMixed(boolean mixed) {
        this.amdid.setMixed(mixed);
    }

    public AutoModel() {
        iddiff = new DifferencingModule();
        amdid = new ArmaModule();
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        int freq = context.description.getFrequency();
        if (context.estimation == null) {
            return ProcessingResult.Failed;
        }
        SarimaSpecification curspec = context.description.getSpecification();
        SarimaSpecification nspec = new SarimaSpecification(curspec.getFrequency());
        boolean nmean = false;
        try {
            // get residuals
            DataBlock res = context.estimation.getLinearizedData();
            iddiff.process(res, freq, 0, 0);
            int nd = iddiff.getD(), nbd = iddiff.getBD();
            nmean = iddiff.isMeanCorrection();

            DataBlock dres;
            if (nd != 0 || nbd != 0) {
                BackFilter ur = iddiff.getDifferencingFilter();
                dres = new DataBlock(res.getLength() - ur.getDegree());
                ur.filter(res, dres);
            } else {
                dres = res;
            }
//            if (nmean) {
//                dres.sub(dres.sum() / dres.getLength());
//            }

            SarmaSpecification rsltSpec = amdid.select(dres, curspec.getFrequency(),
                    curspec.getFrequency() == 2 ? 1 : 2, 1, nd, nbd);
            nspec.copy(rsltSpec);
            nspec.setD(nd);
            nspec.setBD(nbd);
        } catch (RuntimeException err) {
            nspec.airline();
        }
        boolean changed = false;
        if (!curspec.equals(nspec)) {
            context.description.setSpecification(nspec);
            changed = true;
        }
        if (nmean != context.description.isEstimatedMean()) {
            context.description.setMean(nmean);
            changed = true;
        }
        if (changed) {
            context.estimation = null;
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    static SarimaSpecification calcmaxspec(final int freq, final int inic, final int d,
            final int bd) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        spec.setBD(bd);
        spec.setP(2);
        spec.setQ(2);
        if (spec.getFrequency() > 1) {
//                    if (bd == 0) {
            spec.setBP(1);
//                    }
            spec.setBQ(1);
        }
        return spec;
    }

}
