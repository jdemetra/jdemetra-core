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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.IModelEstimator;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import java.util.Comparator;

/**
 *
 * @author Jean Palate
 */
@Deprecated
@Development(status = Development.Status.Preliminary)
public class ModelBenchmarking2  {

    private IOutliersDetectionModule outliers_;
    private IModelEstimator estimator_;
    private Comparator<PreprocessingModel> comparator_;

    public ModelBenchmarking2() {
        comparator_ = new ModelComparator();
    }

    public void setOutliersDetectionModule(IOutliersDetectionModule outliers) {
        outliers_ = outliers;
    }

    public void setEstimator(IModelEstimator estimator) {
        estimator_ = estimator;
    }

    public Comparator<PreprocessingModel> getComparator() {
        return comparator_;
    }

    public void setComparator(Comparator<PreprocessingModel> cmp) {
        comparator_ = cmp;
    }

    public PreprocessingModel process(ModellingContext context) {

        PreprocessingModel rslt = context.current(true);

        SarimaSpecification spec = rslt.description.getSpecification();
        if (spec.isAirline(context.hasseas)) {
            return rslt;
        }
        boolean hasseas = spec.hasSeasonalPart();

        // compute the corresponding airline model.
        ModellingContext scontext = new ModellingContext();
        scontext.description = rslt.description.clone();
        scontext.description.setAirline(context.hasseas);
        scontext.description.setMean(false);
        scontext.description.setOutliers(null);
        do {
            scontext.description.setOutliers(null);
            if (outliers_ != null) {
                outliers_.process(scontext);
            }
            if (!estimator_.estimate(scontext)) {
                return rslt;
            }
        } while (simplify(scontext));

        ModellingContext nscontext = null;
        if (!hasseas) {
            nscontext = new ModellingContext();
            nscontext.description = rslt.description.clone();
            SarimaSpecification nsspec = new SarimaSpecification(spec.getFrequency());
            nsspec.setD(1);
            nsspec.setQ(1);
            nscontext.description.setSpecification(nsspec);
            nscontext.description.setMean(false);
            nscontext.description.setOutliers(null);
            do {
                nscontext.description.setOutliers(null);
                if (outliers_ != null) {
                    outliers_.process(nscontext);
                }
                if (!estimator_.estimate(nscontext)) {
                    nscontext = null;
                }
            } while (simplify(nscontext));
        }
        // compares models
        if (hasseas) {
            PreprocessingModel pref = compare(rslt, scontext.current(true));
            return pref;
        } else {
            // should be completed
            PreprocessingModel pref = compare(rslt, nscontext.current(true));
            return pref;
//            if (pref != rslt || outliers_ == null) {
//                return pref;
//            } else {
//                // reestimate the model...
//                context.description.setOutliers(null);
//                context.estimation = null;
//                outliers_.process(context);
//            }
//            if (!estimator_.estimate(context)) {
//                return rslt;
//            } else {
//                return context.current(true);
//            }
        }
    }

    private PreprocessingModel compare(PreprocessingModel f, PreprocessingModel m) {
        return comparator_.compare(f, m) > 0 ? m : f;
    }

    private boolean simplify(ModellingContext context) {
         IParametricMapping<SarimaModel> mapper = context.description.defaultMapping();
        if (mapper.getDim() == 0) {
            return false;
        }
        DataBlock p = new DataBlock(mapper.map(context.estimation.getArima()));
        DataBlock pvar = context.estimation.getParametersCovariance().diagonal();
        boolean changed = false;
        for (int i = 0; i < p.getLength(); ++i) {
            if (Math.abs(p.get(i) / Math.sqrt(pvar.get(i))) < 1) {
                p.set(i, 0);
                changed = true;
            }
        }
        if (changed) {
            SarimaModel n = mapper.map(p);
            n.adjustSpecification();
            context.description.setSpecification(n.getSpecification());
            context.estimation = null;
            return true;
        } else {
            return false;
        }
    }
}
