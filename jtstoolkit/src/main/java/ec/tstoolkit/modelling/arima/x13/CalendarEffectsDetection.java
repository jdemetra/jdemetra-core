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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.AICcComparator;
import ec.tstoolkit.modelling.arima.IModelComparator;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModelBuilder;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class CalendarEffectsDetection implements IPreprocessingModule {

    private IModelComparator comparer_;
    private ModelEstimation td_, ntd_;
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    private void clear() {
        td_ = null;
        ntd_ = null;
    }

    private void addInfo(ModelDescription desc, InformationSet information) {
//        InformationSet subset = information.subSet(PreprocessingDictionary.CALENDAR);
//        if (td_ != null) {
//            subset.set("stats_td", td_.getStatistics());
//            subset.add("td", td_.getStatistics().AICC);
//        }
//        if (ntd_ != null) {
//            subset.set("stats_ntd", ntd_.getStatistics());
//            subset.add("ntd", ntd_.getStatistics().AICC);
//        }
//        subset.set("count", Variable.usedVariablesCount(desc.getCalendars()));
    }

    /**
     * Creates a new calendar effects detection module
     *
     */
    public CalendarEffectsDetection() {
        comparer_ = new AICcComparator();
    }

    public CalendarEffectsDetection(IModelComparator cmp) {
        comparer_ = cmp;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {

        IParametricMapping<SarimaModel> mapping = X13Preprocessor.createDefaultMapping(context.description);
        RegArimaEstimator monitor = new RegArimaEstimator(mapping);
        monitor.setPrecision(eps_);

        clear();
        // builds models with and without td
        ModelDescription tddesc = context.description.clone();
        if (!PreprocessingModelBuilder.updateCalendar(tddesc, true)) {
            td_ = context.estimation;
        }
        ModelDescription ntddesc = context.description.clone();
        if (!PreprocessingModelBuilder.updateCalendar(ntddesc, false)) {
            ntd_ = context.estimation;
        }
        int nhp = mapping.getDim();
        if (td_ == null) {
            try {
                td_ = new ModelEstimation(tddesc.buildRegArima(), tddesc.getLikelihoodCorrection());
                td_.compute(monitor, nhp);
            } catch (Exception err) {
                td_ = null;
            }
        }
        if (ntd_ == null) {
            try {
                ntd_ = new ModelEstimation(ntddesc.buildRegArima(), ntddesc.getLikelihoodCorrection());
                ntd_.compute(monitor, nhp);
            } catch (Exception err) {
                ntd_ = null;
            }
        }

        boolean changed = false;
        if (comparer_.compare(ntd_, td_) == 0) {
            if (context.estimation != td_) {
                changed = true;
            }
            context.description = tddesc;
            context.estimation = td_;
        } else {

            if (context.estimation != ntd_) {
                changed = true;
            }
            context.description = ntddesc;
            context.estimation = ntd_;
        }

        addInfo(context.description, context.information);
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }
}
