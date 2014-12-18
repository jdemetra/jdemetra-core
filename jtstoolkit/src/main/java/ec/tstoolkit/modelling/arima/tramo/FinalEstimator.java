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
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.ComplexMath;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.IModelEstimator;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FinalEstimator implements IModelEstimator {

    private double cancel_ = .044;
    private double tsig_ = 1;
    private double ur_ = .96;
    private double eps_ = .0001;
    private static final int MAXD = 2, MAXBD = 1;
    private int pass_ = 0;
    private int nnsig_;

    public void setPass(int pass) {
        pass_ = pass;
    }

    public int getChangedParametersCount() {
        return nnsig_;
    }

    @Override
    public boolean estimate(ModellingContext context) {

        int niter = 0;
        do {
            try {
                IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
                ModelDescription model = context.description;
                context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
                // should be changed for fixed parameters
                int ndim = mapping.getDim();
                TramoModelEstimator monitor = new TramoModelEstimator(mapping);
                monitor.setPrecision(getEpsilon());
                if (context.description.isPartiallySpecified()) {
                    context.estimation.improve(monitor, ndim);
                } else {
                    context.estimation.compute(monitor, ndim);
                }
                context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
                if (ndim == 0) {
                    return true;
                }
                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, monitor.getScore());
                if (checkUnitRoots(context)) {
                    nnsig_ = 0;
                    if (context.automodelling) {
                        if (!checkCommonRoots(context)) {
                            nnsig_ = 2;
                        } else {
                            nnsig_ = test(context);
                        }
                    }
                    if (nnsig_ == 0) {
                        return true;
                    }
                    if (nnsig_ == 1) {
                        continue;
                    }
                    if (context.outliers && pass_ <= 1) {
                        return false;
                    }
                }
            } catch (RuntimeException err) {
                return false;
            }
        } while (niter++ < 5);
        return false;
    }

    /**
     * This method correspond to the routine TESTMOD2
     *
     * @param context
     * @return
     */
    private int test(ModellingContext context) {
        double cval = getTsig();
        int nz = context.description.getEstimationDomain().getLength();
        double cmin = nz <= 150 ? .15 : .1;
        double cmod = .95;

        SarimaModel m = context.estimation.getRegArima().getArima();
        SarimaSpecification spec = m.getSpecification();
//        if (spec.getParametersCount() == 1) {
//            return 0;
//        }

        IReadDataBlock pm = m.getParameters();
//        int start = 0, len = spec.getP();
//        boolean dpr = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// (m.RegularAR.Roots,
//        start += len;
//        len = spec.getBP();
//        boolean dps = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// SeasonalAR.Roots,
//        start += len;
//        len = spec.getQ();
//        boolean dqr = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// RegularMA.Roots,
//        start += len;
//        len = spec.getBQ();
//        boolean dqs = Utilities.checkRoots(pm.rextract(start, len), 1 / cmod);// SeasonalMA.Roots,

        int icpr = 0, icps = 0, icqr = 0, icqs = 0;
        double bmin = 99999;
        int k = -1;
//        int nnsig = 0;
        double tmin = cval;
        DataBlock diag = context.estimation.getParametersCovariance().diagonal();
        k += spec.getP();
//        if (spec.getP() > 0 && dpr) {
        if (spec.getP() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    icpr = 1;
                    bmin = t;
                }
            }
        }
        k += spec.getBP();
//        if (spec.getBP() > 0 && dps) {
        if (spec.getBP() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        bmin = t;
                        icps = 1;
                        icpr = 0;
                    }
                }
            }
        }
        k += spec.getQ();
//        if (spec.getQ() > 0 && dqr) {
        if (spec.getQ() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {

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
//        if (spec.getBQ() > 0 && dqs) {
        if (spec.getBQ() > 0) {
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        icqs = 1;
                        icpr = 0;
                        icps = 0;
                        icqr = 0;
                    }
                }
            }
        }

        int nnsig = icpr + icps + icqr + icqs;
        if (nnsig == 0) {
            return 0;
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
        return nnsig;
    }

    private boolean checkCommonRoots(ModellingContext context) {
        // simplify possible common roots on ar, ma
        SarimaModel arima = context.estimation.getArima();
        SarimaSpecification spec = arima.getSpecification();
        boolean changed = false;
        if (spec.getP() != 0 && spec.getQ() != 0) {
            Polynomial p = arima.getRegularAR(), q = arima.getRegularMA();
            Complex[] pr = p.roots(), qr = q.roots();
            // use inverse roots
            for (int i = 0; i < pr.length; ++i) {
                pr[i] = pr[i].inv();
            }
            for (int i = 0; i < qr.length; ++i) {
                qr[i] = qr[i].inv();
            }
            Complex.SimplifyingTool tool = new Complex.SimplifyingTool(getCancel());
            if (tool.simplify(pr, qr)) {
                spec.setP(spec.getP() - 1);
                spec.setQ(spec.getQ() - 1);
                changed = true;
            }
        }
        if (spec.getBP() == 1 && spec.getBQ() == 1 && (Math.abs(arima.bphi(1) - arima.btheta(1)) < getCancel())) {
            spec.setBP(0);
            spec.setBQ(0);
            changed = true;
        }
        if (changed) {
            context.description.setSpecification(spec);
            context.estimation = null;
            return false;
        } else {
            return true;
        }
    }

    private boolean checkUnitRoots(ModellingContext context) {

        //quasi-unit roots of ar are changed in true unit roots
        SarimaModel m = context.estimation.getArima();
        SarimaSpecification nspec = m.getSpecification();

        boolean ok = true;
        if (nspec.getP() > 0 && nspec.getD() < MAXD) {
            if (0 != searchur(m.getRegularAR().mirror().roots())) {
                nspec.setP(nspec.getP() - 1);
                nspec.setD(nspec.getD() + 1);
                ok = false;
            }
        }
        if (nspec.getBP() > 0 && nspec.getBD() < MAXBD) {
            if (0 != searchur(m.getSeasonalAR().mirror().roots())) {
                nspec.setBP(nspec.getBP() - 1);
                nspec.setBD(nspec.getBD() + 1);
                ok = false;
            }
        }
        if (ok) {
            return true;
        } else {
            context.description.setSpecification(nspec);
            context.estimation = null;
            return false;
        }
    }

    private int searchur(final Complex[] r) {
        if (r == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < r.length; ++i) {
            double cdim = Math.abs(r[i].getIm());
            double vcur = ComplexMath.abs(r[i]);
            if (vcur > getUr() && cdim <= 0.05 && r[i].getRe() > 0) {
                ++n;
            }
        }
        return n;
    }

    /**
     * @return the cancel_
     */
    public double getCancel() {
        return cancel_;
    }

    /**
     * @param cancel the cancel to set
     */
    public void setCancel(double cancel) {
        this.cancel_ = cancel;
    }

    /**
     * @return the tsig_
     */
    public double getTsig() {
        return tsig_;
    }

    /**
     * @param tsig the tsig to set
     */
    public void setTsig(double tsig) {
        this.tsig_ = tsig;
    }

    /**
     * @return the ur_
     */
    public double getUr() {
        return ur_;
    }

    /**
     * @param ur the ur to set
     */
    public void setUr(double ur) {
        this.ur_ = ur;
    }

    /**
     * @return the eps_
     */
    public double getEpsilon() {
        return eps_;
    }

    /**
     * @param eps the eps to set
     */
    public void setEpsilon(double eps) {
        this.eps_ = eps;
    }
//    private void tryUrCancelling(ModellingContext context) {
//        tryUrpCancelling(context);
//        tryUrmCancelling(context);
//    }
//
//    private void tryUrpCancelling(ModellingContext context) {
//
//        SarimaModel arima = context.estimation.getArima();
//        SarimaSpecification spec = arima.getSpecification();
//        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getDifferenceOrder() == 0) {
//            return;
//        }
//
//        DataBlock parameters = new DataBlock(arima.getParameters());
//        // creates AR and MA polynomials
//        int pstart = 0, pend = spec.getP();
//        double p = 0;
//        for (int i = pstart; i < pend; ++i) {
//            p += parameters.get(i);
//        }
//        if (Math.abs(p + 1) > UR_LIMIT) {
//            return;
//        }
//        int qstart = pend + spec.getBP(), qend = qstart + spec.getQ();
//        double q = 0;
//        for (int i = qstart; i < qend; ++i) {
//            q += parameters.get(i);
//        }
//        if (Math.abs(q + 1) > UR_LIMIT) {
//            return;
//        }
//        // simplify by (1-B). Horner with -1
//        double tmp = 0;
//        for (int i = pend - 1; i >= pstart; --i) {
//            double c = parameters.get(i);
//            parameters.set(i, -tmp);
//            tmp += c;
//        }
//        tmp = 0;
//        for (int i = qend - 1; i >= qstart; --i) {
//            double c = parameters.get(i);
//            parameters.set(i, -tmp);
//            tmp += c;
//        }
//
//        // try the new model
//        try {
//            IParametricMapping<SarimaModel> mapping = TramoProcessor.createDefaultMapping(context.description);
//            ModelDescription model = context.description;
//            ModelEstimation nestimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
//            mapping.validate(parameters);
//            nestimation.getArima().setParameters(parameters);
//            int ndim = mapping.getDim();
//            TramoModelEstimator monitor = new TramoModelEstimator(mapping);
//            monitor.setPrecision(getEpsilon());
//            if (!nestimation.improve(monitor, ndim)) {
//                return;
//            }
//            if (nestimation.getLikelihood().getLogLikelihood() <= context.estimation.getLikelihood().getLogLikelihood()) {
//                return;
//            }
//            context.estimation = nestimation;
//            context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
//        } catch (Exception err) {
//        }
//    }
//
//    private void tryUrmCancelling(ModellingContext context) {
//
//        SarimaModel arima = context.estimation.getArima();
//        SarimaSpecification spec = arima.getSpecification();
//        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getBD() == 0) {
//            return;
//        }
//
//        DataBlock parameters = new DataBlock(arima.getParameters());
//        // creates AR and MA polynomials
//        int pstart = 0, pend = spec.getP();
//        double p = 0;
//        boolean pos = false;
//        for (int i = pstart; i < pend; ++i) {
//            p += pos ? parameters.get(i) : -parameters.get(i);
//            pos = !pos;
//        }
//        if (Math.abs(p + 1) > UR_LIMIT) {
//            return;
//        }
//        int qstart = pend + spec.getBP(), qend = qstart + spec.getQ();
//        double q = 0;
//        pos = false;
//        for (int i = qstart; i < qend; ++i) {
//            q += pos ? parameters.get(i) : -parameters.get(i);
//            pos = !pos;
//        }
//        if (Math.abs(q + 1) > UR_LIMIT) {
//            return;
//        }
//        // simplify by (1+B). Horner with -1
//        double tmp = 0;
//        for (int i = pend - 1; i >= pstart; --i) {
//            double c = parameters.get(i);
//            parameters.set(i, tmp);
//            tmp = c - tmp;
//        }
//        tmp = 0;
//        for (int i = qend - 1; i >= qstart; --i) {
//            double c = parameters.get(i);
//            parameters.set(i, tmp);
//            tmp = c - tmp;
//        }
//
//        // try the new model
//        try {
//            IParametricMapping<SarimaModel> mapping = TramoProcessor.createDefaultMapping(context.description);
//            ModelDescription model = context.description;
//            ModelEstimation nestimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
//            mapping.validate(parameters);
//            nestimation.getArima().setParameters(parameters);
//            int ndim = mapping.getDim();
//            TramoModelEstimator monitor = new TramoModelEstimator(mapping);
//            monitor.setPrecision(getEpsilon());
//            if (!nestimation.improve(monitor, ndim)) {
//                return;
//            }
//            if (nestimation.getLikelihood().getLogLikelihood() <= context.estimation.getLikelihood().getLogLikelihood()) {
//                return;
//            }
//            context.estimation = nestimation;
//            context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
//        } catch (Exception err) {
//        }
//    }
//    private static final double UR_LIMIT = .1;
}
