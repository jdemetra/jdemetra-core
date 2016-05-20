/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
public class DifferencingModule extends DemetraModule implements IPreprocessingModule {

    public static final int MAXD = 2, MAXBD = 1;
    public static final double EPS = 1e-5;
    
    private int d, bd;
    private boolean mean;

    private static double removeMean(DataBlock data) {
        int n = data.getLength();
        double m = data.sum() / n;
        data.sub(m);
        return m;
    }

    private DataBlock data_ = null;

    /**
     *
     */
    public DifferencingModule() {
    }

    private void calc() {
        if (data_ == null ) {
            return;
        }
     }

    /**
     *
     */
    public void clear() {
        data_ = null;
     }

    /**
     *
     * @return
     */
    public int getBD() {
        return bd;
    }

    public boolean isMean() {
        return mean;
    }

    /**
     *
     * @return
     */
    public int getD() {
        return d;
    }

    /**
     *
     * @return
     */
    public BackFilter getDifferencingFilter() {
        return null;
    }

    /**
     *
     * @param context
     * @return
     */
    @Override
    public ProcessingResult process(ModellingContext context) {
        try{
            // correct data for estimated outliers...
            int xcount = context.estimation.getRegArima().getXCount();
            int xout = context.description.getOutliers().size();

            DataBlock res = context.estimation.getCorrectedData(xcount - xout, xcount);
            SarimaSpecification nspec = context.description.getSpecification();
           // get residuals
            int freq=nspec.getFrequency();
            process(freq, res, nspec.getD(), nspec.getBD());
            boolean changed = false;
            if (nspec.getD() != d || nspec.getBD() != bd) {
                changed = true;
                SarimaSpecification cspec = new SarimaSpecification(freq);
                cspec.setD(d);
                cspec.setBD(bd);
                context.description.setSpecification(cspec);
                context.estimation = null;
            }
            if (mean != context.description.isMean()) {
                changed = true;
                context.description.setMean(mean);
                context.estimation = null;
            }
//            addDifferencingInfo(context, d, bd, mean);
            return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;

        } catch (RuntimeException err) {
            context.description.setAirline(context.hasseas);
            context.estimation = null;
            return ProcessingResult.Failed;
        }
     }

    private void process(int freq, DataBlock res, int d, int bd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   private static final String DIFFERENCING = "Differencing";
}
