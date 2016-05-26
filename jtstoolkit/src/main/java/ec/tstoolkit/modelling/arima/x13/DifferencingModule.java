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

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.arima.IDifferencingModule;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DifferencingModule implements IDifferencingModule {

    public static final int MAXD = 2, MAXBD = 1;
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    private static double removeMean(DataBlock data) {
        int n = data.getLength();
        double m = data.sum() / n;
        data.sub(m);
        return m;
    }
    private DataBlock data_ = null;
    private SarimaSpecification spec_ = new SarimaSpecification();
    private SarimaModel lastModel_;
    private double rmax_, rsmax_, c_;
    private double ub1_ = 0.97;
    private double ub2_ = 0.88;
    private double cancel_ = 0.1;
    private int iter_;
    private boolean ml_, useml_, mlused_, bcalc_;
    private int maxd = MAXD, maxbd = MAXBD;

    /**
     *
     */
    public DifferencingModule() {
    }

    private void allcond() {
        if (spec_.getD() + spec_.getBD() != 0) {
            return;
        }
        double ar = lastModel_.phi(1);
        if (! hasSeas()) {
            if (Math.abs(ar + 1) <= 0.15) {
                spec_.setD(spec_.getD() + 1);
            }
        } else {
            double sar = lastModel_.bphi(1);
            if (Math.abs(ar + 1) <= 0.15 || Math.abs(sar + 1) <= 0.25) {
                rmax_ = -ar;
                rsmax_ = -sar;
                if (ar < sar) {
                    spec_.setD(spec_.getD() + 1);
                } else {
                    spec_.setBD(spec_.getBD() + 1);
                }
            }
        }
    }

    private void calc() {
        if (data_ == null || bcalc_) {
            return;
        }
        c_ = cancel_;
        useml_ = false;
        mlused_ = false;
        bcalc_ = true;
        rsmax_ = 0;
        rmax_ = 0;

        step0();
        iter_ = 0;
        while (nextstep() && iter_ < 5) {
            ++iter_;
        }

    }

    /**
     *
     */
    public void clear() {
        lastModel_ = null;
        spec_ = new SarimaSpecification();
        bcalc_ = false;
        data_ = null;
        useml_ = false;
    }

    private int cond1(int icon) // current condition status
    {
        // only for step1, when no differencing
        if (spec_.getD() + spec_.getBD() != 0) {
            return icon;
        }

        // spec_.D == 0 and spec_.BD == 0 and iround == 2 (cfr TRAMO)
        double ar = lastModel_.phi(1), ma = lastModel_.theta(1), sar = 0, sma = 0;
        if (hasSeas()) {
            sar = lastModel_.bphi(1);
            sma = lastModel_.btheta(1);
        }
        // if cancelation ..., but big initial roots
        if ((Math.abs(ar - ma) < c_ || (hasSeas() && Math.abs(sar - sma) < c_))
                && (rmax_ >= 0.9 || rsmax_ >= 0.9)) {
            if (useml_ && icon == 1) {
                useml_ = false;
            } else {
                ++icon;
            }
            if (rmax_ > rsmax_) {
                spec_.setD(spec_.getD() + 1);
            } else {
                spec_.setBD(spec_.getBD() + 1);
            }
        } // if big initial roots and coef near -1
        else if (((Math.abs(ar + 1) <= 0.15 || (hasSeas() && Math.abs(sar + 1) <= 0.16)) && (rmax_ >= 0.9 || rsmax_ >= 0.88))
                || ((Math.abs(ar + 1) <= 0.16 || (hasSeas() && Math.abs(sar + 1) <= 0.17)) && (rmax_ >= 0.91 || rsmax_ >= 0.89))) {
            if (useml_ && icon == 1) {
                useml_ = false;
            } else {
                ++icon;
            }
            if (rmax_ > rsmax_) {
                spec_.setD(spec_.getD() + 1);
            } else {
                spec_.setBD(spec_.getBD() + 1);
            }
        }
        return icon;
    }

    // avoid overdifferencing
    private int finalcond(int icon) {
        if (icon == 2) {
            spec_.setD(spec_.getD() - 1);
            spec_.setBD(spec_.getBD() - 1);
            if (mlused_) // take the higher coeff
            {
                if (lastModel_.phi(1) < lastModel_.bphi(1)) {
                    spec_.setD(spec_.getD() + 1);
                } else {
                    spec_.setBD(spec_.getBD() + 1);
                }
            } else // use the values stored in the first step
             if (rmax_ > rsmax_) {
                    if (rmax_ > 0) {
                        spec_.setD(spec_.getD() + 1);
                    }
                } else if (rsmax_ > 0) {
                    spec_.setBD(spec_.getBD() + 1);
                }
        }

        if (spec_.getD() > maxd) {
            spec_.setD(maxd);
            icon = 0;
        }
        if (spec_.getBD() > maxbd) {
            spec_.setBD(maxbd);
            icon = 0;
        }
        return icon;
    }

    /**
     *
     * @return
     */
    public int getBD() {
        return spec_.getBD();
    }

    /**
     *
     * @return
     */
    public int getD() {
        return spec_.getD();
    }

    public BackFilter getDifferencingFilter() {
        return spec_.getDifferencingFilter();
    }

    /**
     *
     * @return
     */
    public double getUB1() {
        return ub1_;
    }

    /**
     *
     * @return
     */
    public double getUB2() {
        return ub2_;
    }

    /**
     *
     * @return
     */
    public double getCancel() {
        return cancel_;
    }
    
    private boolean hasSeas(){
        return maxbd > 0 && spec_.getFrequency() > 1;
    }

    private void initstep(boolean bstart) {
        if (spec_.getD() == 0 && spec_.getBD() == 0 && bstart) {
            if (spec_.getFrequency() != 2) {
                spec_.setP(2);
            } else {
                spec_.setP(1);
            }
            spec_.setQ(0);
            spec_.setBQ(0);
            if (hasSeas()) {
                spec_.setBP(1);
            }
        } else {
            spec_.setP(1);
            spec_.setQ(1);
            if (spec_.getFrequency() > 1) {
                spec_.setBP(1);
                spec_.setBQ(1);
            }
        }

        BackFilter ur = spec_.getDifferencingFilter();

        DataBlock data;
        if (ur.getDegree() > 0) {
            data = new DataBlock(data_.getLength() - ur.getDegree());
            ur.filter(data_, data);
        } else {
            data = data_.deepClone();
        }
        removeMean(data);

        HannanRissanen hr = new HannanRissanen();
        boolean usedefault = !hr.process(data, spec_.doStationary());
        // test the model
        if (!usedefault) {
            lastModel_ = hr.getModel();
            if (bstart && !lastModel_.isStable(true)) {
                if (spec_.getP() > 1
                        || (spec_.getP() == 1 && Math.abs(lastModel_.phi(1)) > 1.02)
                        || (spec_.getBP() == 1 && Math.abs(lastModel_.bphi(1)) > 1.02)) {
                    usedefault = true;
                } else {
                    SarimaMapping.stabilize(lastModel_);
                }
            }
        }

        if (usedefault) {
            lastModel_ = new SarimaModel(spec_.doStationary());
        }

        if (usedefault || ml_ || useml_) {
            SarimaMapping.stabilize(lastModel_);

            GlsSarimaMonitor monitor = new GlsSarimaMonitor();
            monitor.setPrecision(eps_);
            //monitor.setMinimizer(new ProxyMinimizer(new QRMarquardt()));
            RegArimaModel<SarimaModel> model = new RegArimaModel<>(
                    new SarimaModel(spec_), data_);
            // model.setMeanCorrection(true);
            //monitor.useMaximumLikelihood(!(bstart && spec_.getD() == 0 && spec_.getBD() == 0));
            RegArimaEstimation<SarimaModel> rslt = monitor.optimize(
                    model, lastModel_);
            if (rslt == null) {
                throw new X13Exception("Non convergence in IDDIF");
            }
            lastModel_.setParameters(rslt.model.getArima().getParameters());
            mlused_ = true;
        } else {
            mlused_ = false;
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
        double ar = lastModel_.phi(1), ma = lastModel_.theta(1), sar = 0, sma = 0;
        if (spec_.getFrequency() > 1) {
            sar = lastModel_.bphi(1);
            sma = lastModel_.btheta(1);
        }
        double din = 0;
        c_ -= 0.002;
        if (!mlused_ && ub2_ >= 0.869) {
            din = .136;
        } else {
            din = 1.005 - ub2_;
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
                spec_.setD(spec_.getD() + 1);
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
                } else if (spec_.getBD() == 0 && Math.abs(sar - sma) > c_) {
                    ++icon;
                    spec_.setBD(spec_.getBD() + 1);
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
        if (iter_ == 0) {
            icon = cond1(icon);
        }
        allcond();
        return finalcond(icon) != 0;
    }

    /**
     *
     * @param freq
     * @param data
     * @param d
     * @param bd
     */
    public void process(IReadDataBlock data, int freq, int d, int bd) {
        clear();
        data_ = new DataBlock(data);
        spec_.setFrequency(freq);
        spec_.setD(d);
        spec_.setBD(bd);
        calc();
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
            rmax_ = vmax;
        } else {
            rsmax_ = vmax;
        }
        return n;
    }

    /**
     *
     * @param value
     */
    public void setCancel(double value) {
        cancel_ = value;
        clear();
    }

    /**
     *
     * @param value
     */
    public void setUB1(double value) {
        ub1_ = value;
        clear();
    }

    /**
     *
     * @param value
     */
    public void setUB2(double value) {
        ub2_ = value;
        clear();
    }

    private void step0() {

        initstep(true);
        if (spec_.getD() != 0 || spec_.getBD() != 0) {
            rmax_ = lastModel_.phi(1);
            rsmax_ = lastModel_.bphi(1);
        }

        Complex[] rar = lastModel_.getRegularAR().mirror().roots();
        spec_.setD(spec_.getD() + searchur(rar, ub1_, true));
        if (hasSeas()) {
            Complex[] rsar = lastModel_.getSeasonalAR().mirror().roots();
            spec_.setBD(spec_.getBD() + searchur(rsar, ub1_, false));
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

    public boolean isMeanCorrection() {
        if (spec_.getDifferenceOrder() == 0) {
            return isStMean();
        } else {
            return isNstMean();
        }
    }

    private boolean isStMean() {
        int n = data_.getLength();
        double wm = data_.sum();
        double wd = Math.sqrt(data_.ssq());
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
        SarimaSpecification spec = spec_.clone();
        RegArimaEstimator monitor = new RegArimaEstimator(new SarimaMapping(spec, true));
        monitor.setPrecision(1e-4);
//        spec.setP(0);
//        spec.setBP(0);
        RegArimaModel<SarimaModel> model = new RegArimaModel<>(
                new SarimaModel(spec), data_);
        model.setMeanCorrection(true);

        RegArimaEstimation<SarimaModel> est = monitor.process(model);
        if (est == null) {
            return false;
        }
        double t = est.likelihood.getTStats(false, 0)[0];

        int n = data_.getLength();
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

    @Override
    public void setLimits(int maxd, int maxbd) {
        this.maxd = maxd;
        this.maxbd = maxbd;
    }

}
