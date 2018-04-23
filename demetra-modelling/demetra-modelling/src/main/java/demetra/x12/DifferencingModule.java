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

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.linearfilters.BackFilter;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.RegArimaUtility;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.regarima.ami.IDifferencingModule;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DifferencingModule implements IDifferencingModule {

    public static final int MAXD = 2, MAXBD = 1;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(DifferencingModule.class)
    public static class Builder {

        private int maxd = MAXD, maxbd = MAXBD;
        private double eps = 1e-5;
        private double ub1 = 0.97;
        private double ub2 = 0.88;
        private double cancel = 0.1;

        private Builder() {
        }

        public Builder maxD(int maxd) {
            this.maxd = maxd;
            return this;
        }

        public Builder maxBD(int maxbd) {
            this.maxbd = maxbd;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder ub1(double ub1) {
            this.ub1 = ub1;
            return this;
        }

        public Builder ub2(double ub2) {
            this.ub2 = ub2;
            return this;
        }

        public Builder cancel(double cancel) {
            this.cancel = cancel;
            return this;
        }

        public DifferencingModule build() {
            return new DifferencingModule(maxd, maxbd, ub1, ub2, cancel, eps);
        }
    }

    private static double removeMean(DataBlock x) {
        double m = x.average();
        x.sub(m);
        return m;
    }

    private double[] x;
    private SarimaSpecification spec = new SarimaSpecification();
    private SarimaModel lastModel;
    private double rmax, rsmax, c_;
    private int iter;
    private boolean ml_, useml_, mlused, bcalc_;
    private final int maxd, maxbd;
    private final double ub1;
    private final double ub2;
    private final double cancel;
    private final double eps;

    /**
     *
     * @param maxd
     * @param maxbd
     * @param ub1
     * @param ub2
     * @param cancel
     * @param eps
     */
    public DifferencingModule(final int maxd, final int maxbd,
            final double ub1, final double ub2, final double cancel,
            final double eps) {
        this.maxd = maxd;
        this.maxbd = maxbd;
        this.ub1 = ub1;
        this.ub2 = ub2;
        this.cancel = cancel;
        this.eps = eps;
    }

    private void allcond() {
        if (spec.getD() + spec.getBd() != 0) {
            return;
        }
        double ar = lastModel.phi(1);
        if (!hasSeas()) {
            if (Math.abs(ar + 1) <= 0.15) {
                spec.setD(spec.getD() + 1);
            }
        } else {
            double sar = lastModel.bphi(1);
            if (Math.abs(ar + 1) <= 0.15 || Math.abs(sar + 1) <= 0.25) {
                rmax = -ar;
                rsmax = -sar;
                if (ar < sar) {
                    spec.setD(spec.getD() + 1);
                } else {
                    spec.setBd(spec.getBd() + 1);
                }
            }
        }
    }

    private void calc() {
        if (x == null || bcalc_) {
            return;
        }
        c_ = cancel;
        useml_ = false;
        mlused = false;
        bcalc_ = true;
        rsmax = 0;
        rmax = 0;

        step0();
        iter = 0;
        while (nextstep() && iter < 5) {
            ++iter;
        }

    }

    /**
     *
     */
    public void clear() {
        lastModel = null;
        spec = new SarimaSpecification();
        bcalc_ = false;
        x = null;
        useml_ = false;
    }

    private int cond1(int icon) // current condition status
    {
        // only for step1, when no differencing
        if (spec.getD() + spec.getBd() != 0) {
            return icon;
        }

        // spec_.D == 0 and spec_.BD == 0 and iround == 2 (cfr TRAMO)
        double ar = lastModel.phi(1), ma = lastModel.theta(1), sar = 0, sma = 0;
        if (hasSeas()) {
            sar = lastModel.bphi(1);
            sma = lastModel.btheta(1);
        }
        // if cancelation ..., but big initial roots
        if ((Math.abs(ar - ma) < c_ || (hasSeas() && Math.abs(sar - sma) < c_))
                && (rmax >= 0.9 || rsmax >= 0.9)) {
            if (useml_ && icon == 1) {
                useml_ = false;
            } else {
                ++icon;
            }
            if (rmax > rsmax) {
                spec.setD(spec.getD() + 1);
            } else {
                spec.setBd(spec.getBd() + 1);
            }
        } // if big initial roots and coef near -1
        else if (((Math.abs(ar + 1) <= 0.15 || (hasSeas() && Math.abs(sar + 1) <= 0.16)) && (rmax >= 0.9 || rsmax >= 0.88))
                || ((Math.abs(ar + 1) <= 0.16 || (hasSeas() && Math.abs(sar + 1) <= 0.17)) && (rmax >= 0.91 || rsmax >= 0.89))) {
            if (useml_ && icon == 1) {
                useml_ = false;
            } else {
                ++icon;
            }
            if (rmax > rsmax) {
                spec.setD(spec.getD() + 1);
            } else {
                spec.setBd(spec.getBd() + 1);
            }
        }
        return icon;
    }

    // avoid overdifferencing
    private int finalcond(int icon) {
        if (icon == 2) {
            spec.setD(spec.getD() - 1);
            spec.setBd(spec.getBd() - 1);
            if (mlused) // take the higher coeff
            {
                if (lastModel.phi(1) < lastModel.bphi(1)) {
                    spec.setD(spec.getD() + 1);
                } else {
                    spec.setBd(spec.getBd() + 1);
                }
            } else // use the values stored in the first step
             if (rmax > rsmax) {
                    if (rmax > 0) {
                        spec.setD(spec.getD() + 1);
                    }
                } else if (rsmax > 0) {
                    spec.setBd(spec.getBd() + 1);
                }
        }

        if (spec.getD() > maxd) {
            spec.setD(maxd);
            icon = 0;
        }
        if (spec.getBd() > maxbd) {
            spec.setBd(maxbd);
            icon = 0;
        }
        return icon;
    }

    /**
     *
     * @return
     */
    public double getUB1() {
        return ub1;
    }

    /**
     *
     * @return
     */
    public double getUB2() {
        return ub2;
    }

    /**
     *
     * @return
     */
    public double getCancel() {
        return cancel;
    }

    private boolean hasSeas() {
        return maxbd > 0 && spec.getPeriod() > 1;
    }

    private void initstep(boolean bstart) {
        if (spec.getD() == 0 && spec.getBd() == 0 && bstart) {
            if (spec.getPeriod() != 2) {
                spec.setP(2);
            } else {
                spec.setP(1);
            }
            spec.setQ(0);
            spec.setBq(0);
            if (hasSeas()) {
                spec.setBp(1);
            }
        } else {
            spec.setP(1);
            spec.setQ(1);
            if (spec.getPeriod() > 1) {
                spec.setBp(1);
                spec.setBq(1);
            }
        }

        BackFilter ur = RegArimaUtility.differencingFilter(spec.getPeriod(), spec.getD(), spec.getBd());
        DataBlock data;
        if (ur.length() > 1) {
            data = DataBlock.make(x.length - ur.length() + 1);
            ur.apply(DataBlock.ofInternal(x), data);
        } else {
            data = DataBlock.copyOf(x);
        }
        removeMean(data);

        HannanRissanen hr = HannanRissanen.builder().build();
        boolean usedefault = !hr.process(data, spec.doStationary());
        // test the model
        if (!usedefault) {
            lastModel = hr.getModel();
            if (bstart && !lastModel.isStable(true)) {
                if (spec.getP() > 1
                        || (spec.getP() == 1 && Math.abs(lastModel.phi(1)) > 1.02)
                        || (spec.getBp() == 1 && Math.abs(lastModel.bphi(1)) > 1.02)) {
                    usedefault = true;
                } else {
                    lastModel = SarimaMapping.stabilize(lastModel);
                }
            }
        }

        if (usedefault) {
            lastModel = SarimaModel.builder(spec.doStationary()).setDefault().build();
        }

        if (usedefault || ml_ || useml_) {
            SarimaMapping.stabilize(lastModel);

            IRegArimaProcessor processor = RegArimaUtility.processor(true, eps);
            RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(data).arima(lastModel).build();
            RegArimaEstimation<SarimaModel> rslt = processor.optimize(regarima);
            if (rslt == null) {
                throw new X12Exception("Non convergence in IDDIF");
            }

            lastModel = rslt.getModel().arima();
            mlused = true;
        } else {
            mlused = false;
        }
        useml_ = false;
    }

    /**
     *
     * @return
     */
    public boolean isUsingML() {
        return ml_;
    }

    private int maincondition() {
        double ar = lastModel.phi(1), ma = lastModel.theta(1), sar = 0, sma = 0;
        if (spec.getPeriod() > 1) {
            sar = lastModel.bphi(1);
            sma = lastModel.btheta(1);
        }
        double din = 0;
        c_ -= 0.002;
        if (!mlused && ub2 >= 0.869) {
            din = .136;
        } else {
            din = 1.005 - ub2;
        }

        int icon = 0;

        // search for regular unit roots
        if (Math.abs(ar + 1) <= din) // ar near -1
        {
            if (-ar > 1.02) // |ar| too big (bad estimation)
            {
                icon = 1;
                useml_ = true;
            } else if (Math.abs(ar - ma) > c_) // no cancelation
            {
                ++icon;
                spec.setD(spec.getD() + 1);
            }
        } else if (Math.abs(ar) > 1.12) // |ar| too big (bad estimation)
        {
            icon = 1;
            useml_ = true;
        }
        if (hasSeas()) {
            if (Math.abs(sar + 1) <= .19) {
                if (-sar > 1.02) {
                    useml_ = true;
                    icon = 1;
                } else if (spec.getBd() == 0 && Math.abs(sar - sma) > c_) {
                    ++icon;
                    spec.setBd(spec.getBd() + 1);
                    if (useml_) {
                        --icon;
                        useml_ = false;
                    }
                }
            } else if (Math.abs(sar) > 1.12) {
                icon = 1;
                useml_ = true;
            }
        }

        return icon;
    }

    private boolean nextstep() {

        initstep(false);
        int icon = maincondition();
        if (iter == 0) {
            icon = cond1(icon);
        }
        allcond();
        return finalcond(icon) != 0;
    }

    /**
     *
     * @param data
     * @param periods
     * @param start
     * @return
     */
    @Override
    public int[] process(DoubleSequence data, int[] periods, int[] start) {
        clear();
        x = data.toArray();
        if (periods.length > 2 || periods[0] != 1 
                || (start != null && start.length != periods.length)) {
            throw new IllegalArgumentException();
        }
        spec.setPeriod(periods.length == 2 ? periods[1] : 1);
        if (start != null) {
            spec.setD(start[0]);
            if (start.length == 2) {
                spec.setBd(start[1]);
            }
        }
        calc();
        return spec.getPeriod() > 1 ? new int[]{spec.getD(), spec.getBd()} : new int[]{spec.getD()};
    }

    private int searchur(Complex[] r, double val, boolean regular) {
        if (r == null) {
            return 0;
        }
        int n = 0;
        double vmax = 0;
        for (int i = 0; i < r.length; ++i) {
            double cdim = Math.abs(r[i].getIm());
            double vcur = (r[i].abs());
            if (vcur >= val && cdim <= 0.05 && r[i].getRe() > 0) {
                ++n;
            } else if (cdim <= 0.02 && r[i].getRe() > 0 && vcur > vmax) {
                vmax = vcur;
            }
        }
        if (regular) {
            rmax = vmax;
        } else {
            rsmax = vmax;
        }
        return n;
    }

    private void step0() {

        initstep(true);
        if (spec.getD() != 0 || spec.getBd() != 0) {
            rmax = lastModel.phi(1);
            rsmax = lastModel.bphi(1);
        }

        Complex[] rar = lastModel.getRegularAR().mirror().roots();
        spec.setD(spec.getD() + searchur(rar, ub1, true));
        if (hasSeas()) {
            Complex[] rsar = lastModel.getSeasonalAR().mirror().roots();
            spec.setBd(spec.getBd() + searchur(rsar, ub1, false));
        }
    }

    /**
     *
     * @param value
     */
    public void useML(boolean value) {
        ml_ = value;
        clear();
    }

    @Override
    public boolean isMeanCorrection() {
        if (spec.getDifferenceOrder() == 0) {
            return isStMean();
        } else {
            return isNstMean();
        }
    }

    private boolean isStMean() {
        int n = x.length;
        double wm = 0, ssq = 0;
        for (int i = 0; i < n; ++i) {
            wm += x[i];
            ssq += x[i] * x[i];
        }
        double wd = Math.sqrt(ssq);
        double tval = wm / wd;

        double vct;
        if (n > 200) {
            vct = 2.55;
        } else if (n > 80) {
            vct = 2;
        } else {
            vct = 1.96;
        }
        return Math.abs(tval) > vct;
    }

    private boolean isNstMean() {
        // compute regression model with mean
        //GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        //monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        SarimaSpecification spec = this.spec.clone();
        RegArimaModel<SarimaModel> model = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSequence.ofInternal(x))
                .meanCorrection(true)
                .arima(SarimaModel.builder(spec).setDefault().build())
                .build();

        RegArimaEstimation<SarimaModel> est = RegArimaUtility.processor(true, eps).process(model);

        if (est == null) {
            return false;
        }
        double t = est.getConcentratedLikelihood().tstat(0, 0, false);

        int n = x.length;
        double vct;
        if (n <= 80) {
            vct = 1.96;
        } else if (n <= 155) {
            vct = 1.98;
        } else if (n <= 230) {
            vct = 2.1;
        } else if (n <= 350) {
            vct = 2.3;
        } else {
            vct = 2.5;
        }
        return Math.abs(t) > vct;
    }

}
