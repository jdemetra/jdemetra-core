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
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelEstimator implements IModelEstimator {

    private IOutliersDetectionModule outliers_;
    private double eps_ = 1e-7;

    @Override
    public boolean estimate(ModellingContext context) {
//        do {
//            context.description.setOutliers(null);
//            if (outliers_ != null) {
//                outliers_.process(context);
//            }
//            if (!calc(context)) {
//                return false;
//            }
//        }
//        while (simplify(context));
//        return true;
        context.description.setOutliers(null);
        if (outliers_ != null) {
            outliers_.process(context);
        }
        if (!calc(context)) {
            return false;
        } else {
            return true;
        }
    }

//    private boolean simplify(ModellingContext context) {
//        IParametricMapping<SarimaModel> mapper = TramoProcessor.createDefaultMapping(context.description);
//        if (mapper.getDim() == 0) {
//            return false;
//        }
//        DataBlock p = new DataBlock(mapper.map(context.estimation.getArima()));
//        DataBlock pvar = context.estimation.getParametersCovariance().diagonal();
//        boolean changed = false;
//        for (int i = 0; i < p.getLength(); ++i) {
//            if (Math.abs(p.get(i) / Math.sqrt(pvar.get(i))) < 1) {
//                p.set(i, 0);
//                changed = true;
//            }
//        }
//        if (changed) {
//            SarimaModel n = mapper.map(p);
//            n.adjustSpecification();
//            context.description.setSpecification(n.getSpecification());
//            context.estimation = null;
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
    /**
     * This method correspond to the routine TESTMOD2
     *
     * @param context
     * @return
     */
    private boolean simplify(ModellingContext context) {
        double cval = 1;
//        int nz = context.description.getEstimationDomain().getLength();
//        double cmin = nz <= 150 ? .15 : .1;
//        double cmod = .95;

        SarimaModel m = context.estimation.getRegArima().getArima();
        SarimaSpecification spec = m.getSpecification();
        if (spec.getParametersCount() <= 1) {
            return false;
        }

        IReadDataBlock pm = m.getParameters();

        int icpr = 0, icps = 0, icqr = 0, icqs = 0;
        double bmin = 99999;
        int k = -1;
        int nnsig = 0;
        double tmin = cval;
        DataBlock diag = context.estimation.getParametersCovariance().diagonal();
        k += spec.getP();
        if (spec.getP() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin) {// && v < cmin) {
                    icpr = 1;
                    bmin = t;
                }
            }
        }
        k += spec.getBP();
        if (spec.getBP() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin) {// && v < cmin) {
                    if (bmin > t) {
                        icps = 1;
                        bmin = t;
                        icpr = 0;
                    }
                }
            }
        }
        k += spec.getQ();
        if (spec.getQ() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin) {// && v < cmin) {
                    if (bmin > t) {
                        bmin = t;
                        icqr = 1;
                        icpr = 0;
                        icps = 0;
                    }
                }
            }
        }
        k += spec.getBQ();
        if (spec.getBQ() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin) {// && v < cmin) {
                    if (bmin > t) {
                        icqs = 1;
                        icpr = 0;
                        icps = 0;
                        icqr = 0;
                    }
                }
            }
        }

        nnsig = icpr + icps + icqr + icqs;
        if (nnsig == 0) {
            return false;
        }
        SarimaSpecification nspec = spec.clone();
        if (icpr > 0) {
            nspec.setP(nspec.getP() - 1);
        }
        if (icps > 0) {
            nspec.setBP(nspec.getBP() - 1);
        }
        if (icqr > 0) {
            nspec.setQ(nspec.getQ() - 1);
        }
        if (icqs > 0) {
            nspec.setBQ(nspec.getBQ() - 1);
        }

        context.description.setSpecification(nspec);
        context.estimation = null;
        return true;
    }

    private boolean calc(ModellingContext context) {
        IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
        ModelDescription model = context.description;
        context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
        int ndim = mapping.getDim();
        TramoModelEstimator monitor = new TramoModelEstimator(mapping);
        monitor.setPrecision(eps_);
        if (context.estimation.compute(monitor, ndim)) {
            context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
            if (ndim > 0) {
                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, monitor.getScore());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the outliers_
     */
    public IOutliersDetectionModule getOutliersDetectionModule() {
        return outliers_;
    }

    /**
     * @param outliers_ the outliers_ to set
     */
    public void setOutliersDetectionModule(IOutliersDetectionModule outliers_) {
        this.outliers_ = outliers_;
    }

    /**
     * @return the eps
     */
    public double getPrecision() {
        return eps_;
    }

    /**
     * @param eps the eps to set
     */
    public void setPrecision(double eps) {
        this.eps_ = eps;
    }
}
