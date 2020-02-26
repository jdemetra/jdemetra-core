/*
* Copyright 2019 National Bank of Belgium
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
package jdplus.tramo;

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.math.Complex;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.polynomials.Polynomial;
import jdplus.regsarima.regular.IModelEstimator;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.RegSarimaProcessor;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class FinalEstimator implements IModelEstimator {

    private static final int MAXD = 2, MAXBD = 1;
    
    public static Builder builder(){
        return new Builder();
    }

    @BuilderPattern(FinalEstimator.class)
    public static class Builder {

        private double cancel = .044;
        private double significanceThreshold = 1;
        private double unitRootThreshold = .96;
        private double precision = .0001;
        private int pass=0;
        private boolean ami=false, outliers=false;

        public FinalEstimator build() {
            return new FinalEstimator(this);
        }
        
        public Builder precision(double precision){
            this.precision=precision;
            return this;
        }
        
        public Builder cancel(double cancel){
            this.cancel=cancel;
            return this;
        }
        
        public Builder unitRootThreshold(double ur){
            this.unitRootThreshold=ur;
            return this;
        }
        
        public Builder pass(int pass){
            this.pass=pass;
            return this;
        }

        public Builder ami(boolean ami){
            this.ami=ami;
            return this;
        }

        public Builder outliers(boolean outliers){
            this.outliers=outliers;
            return this;
        }
    }

    private final double cancel;
    private final double tsig;
    private final double ur;
    private final double eps;
    private final int pass;
    private final boolean ami, outliers;
    private int nnsig;

    private FinalEstimator(Builder builder) {
        this.cancel = builder.cancel;
        this.eps = builder.precision;
        this.tsig = builder.significanceThreshold;
        this.ur = builder.unitRootThreshold;
        this.pass=builder.pass;
        this.ami=builder.ami;
        this.outliers=builder.outliers;
    }

    @Override
    public boolean estimate(RegSarimaModelling context) {
        int niter = 0;
        do {
            try {
                IParametricMapping<SarimaModel> mapping = context.getDescription().getArimaComponent().defaultMapping();
                RegSarimaProcessor processor = RegSarimaProcessor.builder()
                        .minimizer(LevenbergMarquardtMinimizer.builder())
                        .precision(eps)
//                        .startingPoint(RegSarimaProcessor.StartingPoint.Multiple)
                        .build();
                context.estimate(processor);
                int ndim = mapping.getDim();
                if (ndim == 0) {
                    return true;
                }
//                context.information.subSet(RegArimaEstimator.OPTIMIZATION).set(RegArimaEstimator.SCORE, monitor.getScore());
                if (checkUnitRoots(context)) {
                    nnsig = 0;
                    if (ami) {
                        if (!checkCommonRoots(context)) {
                            nnsig = 2;
                        } else {
                            nnsig = test(context);
                        }
                    }
                    if (nnsig == 0) {
                        return true;
                    }
                    if (nnsig == 1) {
                        continue;
                    }
                    if (outliers && pass <= 1) {
                        return false;
                    }
                }
            } catch (RuntimeException err) {
                return false;
            }
        } while (niter++ < 5);
        return false;
    }

    public int getChangedParametersCount() {
        return nnsig;
    }

//    /**
//     * This method correspond to the routine TESTMOD2
//     *
//     * @param context
//     * @return
//     */
    private int test(RegSarimaModelling context) {
        double cval = tsig;
        int nz = context.getDescription().getEstimationDomain().getLength();
        double cmin = nz <= 150 ? .15 : .1;
        double cmod = .95;

        SarimaModel m = context.getDescription().getArimaComponent().getModel();
        SarimaOrders spec = m.orders();

        DoubleSeq pm = m.parameters();

        int icpr = 0, icps = 0, icqr = 0, icqs = 0;
        double bmin = 99999;
        int k = -1;
        double tmin = cval;
        DataBlock diag = context.getEstimation().getMax().asymptoticCovariance().diagonal();
        k += spec.getP();
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
        k += spec.getBp();
        if (spec.getBp() > 0) {
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
        k += spec.getBq();
        if (spec.getBq() > 0) {
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
        SarimaOrders nspec = spec.clone();
        if (icpr > 0) {
            nspec.setP(nspec.getP() - 1);
        }
        if (icps > 0) {
            nspec.setBp(nspec.getBp() - 1);
        }
        if (icqr > 0) {
            nspec.setQ(nspec.getQ() - 1);
        }
        if (icqs > 0) {
            nspec.setBq(nspec.getBq() - 1);
        }

        context.setSpecification(nspec);
        return nnsig;
    }

    private boolean checkCommonRoots(RegSarimaModelling context) {
        // simplify possible common roots on ar, ma
        SarimaModel arima = context.getDescription().getArimaComponent().getModel();
        SarimaOrders spec = arima.orders();
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
//            Complex.SimplifyingTool tool = new Complex.SimplifyingTool(cancel);
//            if (tool.simplify(pr, qr)) {
//                spec.setP(spec.getP() - 1);
//                spec.setQ(spec.getQ() - 1);
//                changed = true;
//            }
        }
        if (spec.getBp() == 1 && spec.getBq() == 1 && (Math.abs(arima.bphi(1) - arima.btheta(1)) < cancel)) {
            spec.setBp(0);
            spec.setBq(0);
            changed = true;
        }
        if (changed) {
            context.setSpecification(spec);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkUnitRoots(RegSarimaModelling context) {

        //quasi-unit roots of ar are changed in true unit roots
        SarimaModel m = context.getDescription().getArimaComponent().getModel();
        SarimaOrders nspec = m.orders();

        boolean ok = true;
        if (nspec.getP() > 0 && nspec.getD() < MAXD) {
            if (0 != searchur(m.getRegularAR().mirror().roots())) {
                nspec.setP(nspec.getP() - 1);
                nspec.setD(nspec.getD() + 1);
                ok = false;
            }
        }
        if (nspec.getBp() > 0 && nspec.getBd() < MAXBD) {
            if (0 != searchur(m.getSeasonalAR().mirror().roots())) {
                nspec.setBp(nspec.getBp() - 1);
                nspec.setBd(nspec.getBd() + 1);
                ok = false;
            }
        }
        if (ok) {
            return true;
        } else {
            context.setSpecification(nspec);
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
            double vcur = r[i].abs();
            if (vcur > ur && cdim <= 0.05 && r[i].getRe() > 0) {
                ++n;
            }
        }
        return n;
    }

}
