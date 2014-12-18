/*
 * Copyright 2013-2014 National Bank of Belgium
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

import ec.tstoolkit.modelling.arima.ModelStatistics;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.AbstractModelController;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
public class RegularUnderDifferencingTest2 extends AbstractModelController {

    @Override
    public ProcessingResult process(ModellingContext context) {
        if (context.description.getFrequency() <= 2) {
            return ProcessingResult.Unprocessed;
        }
        ModelStatistics stats = new ModelStatistics(context.tmpModel());
        if (stats.ljungBoxPvalue >= .005) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(context)) {
            return ProcessingResult.Unchanged;
        }
        ModellingContext ncontext=buildNewModel(context);
        PreprocessingModel nmodel = ncontext.tmpModel();
        if (nmodel == null) {
            return ProcessingResult.Failed;
        }
        if (new ModelComparator().compare(nmodel, context.tmpModel()) < 0) {
//            setReferenceModel(smodel);
            transferInformation(ncontext, context);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    private boolean needProcessing(ModellingContext context) {
        double[] y = context.estimation.getLikelihood().getResiduals();
        int npos0 = 0;
        int imax = Math.min(24, y.length - 1);
        double[] ac = DescriptiveStatistics.ac(imax, y);
        for (int i = 0; i < 12; ++i) {
            if (ac[i] > 0) {
                ++npos0;
            }
        }
        int npos1 = npos0;
        for (int i = 12; i < ac.length; ++i) {
            if (ac[i] > 0) {
                ++npos1;
            }
        }
        return npos0 >= context.description.getFrequency() || npos0 >= 9 || npos1 >= 17;
    }

    private ModellingContext buildNewModel(ModellingContext context) {
        ModellingContext ncontext = new ModellingContext();
        ModelDescription ndesc = context.description.clone();
        SarimaSpecification spec = ndesc.getSpecification();
        if (spec.getD() == 2) {
            if (spec.getP() == 3) {
                return null;
            }
            spec.setP(spec.getP() + 1);
            ndesc.setSpecification(spec);
            ndesc.setMean(true);
        } else {
            if (spec.getQ() == 3) {
                return null;
            }
            spec.setQ(spec.getQ() + 1);
            spec.setD(spec.getD() + 1);
            ndesc.setSpecification(spec);
            ndesc.setMean(false);
        }
        ncontext.description = ndesc;
        // estimate the new model
        if (!estimate(ncontext, true)) {
            return null;
        }
        return ncontext;
    }

}
