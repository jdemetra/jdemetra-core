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
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.ComplexMath;
import ec.tstoolkit.maths.linearfilters.Utilities;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.IModelEstimator;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FinalEstimator implements IModelEstimator {

    private double cancel_ = .1;
    private double tsig_ = 1;
    private double ur_ = .94;
    private static final int MAXD = 2, MAXBD = 1;
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    @Override
    public boolean estimate(ModellingContext context) {

        int niter = 0;
        do {
            try {
                IParametricMapping<SarimaModel> mapping = X13Preprocessor.createDefaultMapping(context.description);
                RegArimaEstimator monitor = new RegArimaEstimator(mapping);
                monitor.setPrecision(eps_);
                ModelDescription model = context.description;
                context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
                int ndim = mapping.getDim();
                if (context.description.isFullySpecified()) {
                    context.estimation.improve(monitor, ndim);
                } else {
                    context.estimation.compute(monitor, ndim);
                }
                context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
                //if (checkUnitRoots(context) && checkCommonRoots(context)) {
                if (ndim == 0) {
                    return true;
                }
                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, monitor.getScore());
                if (context.automodelling) {
                    int itest = test(context);
                    if (itest == 0) {
                        return true;
                    } else if (itest > 1) {
                        return false;
                    }
                } else {
                    return true;
                }
                //}
            } catch (RuntimeException err) {
                return false;
            }
        } while (niter++ < 5);
        return false;
    }

    private int test(ModellingContext context) {
        double cval = tsig_;
        int nz = context.description.getEstimationDomain().getLength();
        double cmin = nz <= 150 ? .15 : .1;
        double cmod = .95;
        double bmin = 999;

        SarimaModel m = context.estimation.getRegArima().getArima();
        SarimaSpecification spec = m.getSpecification();
        if (spec.getParametersCount() == 1) {
            return 0;
        }

        IReadDataBlock pm = m.getParameters();
        //if (context.automodelling) {
        int start = 0, len = spec.getP();
        boolean dpr = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// (m.RegularAR.Roots,
        start += len;
        len = spec.getBP();
        boolean dps = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// SeasonalAR.Roots,
        start += len;
        len = spec.getQ();
        boolean dqr = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// RegularMA.Roots,
        start += len;
        len = spec.getBQ();
        boolean dqs = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// SeasonalMA.Roots,
        // 1/cmod);

        if (!dpr || !dps || !dqr || !dqs) {
            return 0;
        }
        //}

        // new implementation (cfr Tramo)
        // search the smallest TVal, remove only 1 parameter

        int cpr = 0, cps = 0, cqr = 0, cqs = 0;
        double tmin = cval;
        DataBlock diag = context.estimation.getParametersCovariance().diagonal();


        int k = -1;
        if (spec.getP() > 0) {
            k += spec.getP();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    ++cpr;
                    bmin = t;
                }
            }
        }
        if (spec.getBP() > 0) {
            k += spec.getBP();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cps;
                        bmin = t;
                        cpr = 0;
                    }
                }
            }
        }
        if (spec.getQ() > 0) {
            k += spec.getQ();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqr;
                        bmin = t;
                        cpr = 0;
                        cps = 0;
                    }
                }
            }
        }
        if (spec.getBQ() > 0) {
            k += spec.getBQ();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqs;
                        cpr = 0;
                        cps = 0;
                        cqr = 0;
                    }
                }
            }
        }

        int nnsig = cpr + cps + cqr + cqs;
        if (nnsig == 0) {
            return 0;
        }

        SarimaSpecification nspec = spec.clone();
        context.estimation = null;
        // reduce the orders
        if (cpr > 0) {
            nspec.setP(nspec.getP() - cpr);
        } else if (cps > 0) {
            nspec.setBP(nspec.getBP() - cps);
        } else if (cqr > 0) {
            nspec.setQ(nspec.getQ() - cqr);
        } else if (cqs > 0) {
            nspec.setBQ(nspec.getBQ() - cqs);
        }

        context.description.setSpecification(nspec);
        return nnsig;
    }

//    private boolean checkCommonRoots(ModellingContext context) {
//        // simplify possible common roots on ar, ma
//        SarimaModel arima = context.estimation.getArima();
//        SarimaSpecification spec = arima.getSpecification();
//        boolean changed = false;
//        if (spec.getP() != 0 && spec.getQ() != 0) {
//            Polynomial p = arima.getRegularAR(), q = arima.getRegularMA();
//            Complex[] pr = p.roots(), qr = q.roots();
//            Complex.SimplifyingTool tool = new Complex.SimplifyingTool(cancel_ * .1);
//            if (tool.simplify(pr, qr)) {
//                spec.setP(spec.getP() - 1);
//                spec.setQ(spec.getQ() - 1);
//                changed = true;
//            }
//        }
//        if (spec.getBP() == 1 && spec.getBQ() == 1 && (Math.abs(arima.bphi(1) - arima.btheta(1)) < cancel_)) {
//            spec.setBP(0);
//            spec.setBQ(0);
//            changed = true;
//        }
//        if (changed) {
//            context.description.setSpecification(spec);
//            context.estimation = null;
//            return false;
//        }
//        else {
//            return true;
//        }
//    }
//    private boolean checkUnitRoots(ModellingContext context) {
//
//        //quasi-unit roots of ar are changed in true unit roots
//        SarimaModel m = context.estimation.getArima();
//        SarimaSpecification nspec = m.getSpecification();
//
//        boolean ok = true;
//        if (nspec.getP() > 0 && nspec.getD() < MAXD) {
//            if (0 != searchur(m.getRegularAR().mirror().roots())) {
//                nspec.setP(nspec.getP() - 1);
//                nspec.setD(nspec.getD() + 1);
//                ok = false;
//            }
//        }
//        if (nspec.getBP() > 0 && nspec.getBD() < MAXBD) {
//            if (0 != searchur(m.getSeasonalAR().mirror().roots())) {
//                nspec.setBP(nspec.getBP() - 1);
//                nspec.setBD(nspec.getBD() + 1);
//                ok = false;
//            }
//        }
//        if (ok) {
//            return true;
//        }
//        else {
//            context.description.setSpecification(nspec);
//            context.estimation = null;
//            return false;
//        }
//    }
    private int searchur(final Complex[] r) {
        if (r == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < r.length; ++i) {
            double cdim = Math.abs(r[i].getIm());
            double vcur = ComplexMath.abs(r[i]);
            if (vcur > ur_ && cdim <= 0.05 && r[i].getRe() > 0) {
                ++n;
            }
        }
        return n;
    }
}
