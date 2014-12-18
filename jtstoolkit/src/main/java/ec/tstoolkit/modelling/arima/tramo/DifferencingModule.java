/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.ArmaKF;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
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
public class DifferencingModule extends AbstractTramoModule implements IPreprocessingModule {

    public static final int MAXD = 2, MAXBD = 1;
    public static final double EPS = 1e-5;

    private static double removeMean(DataBlock data) {
        int n = data.getLength();
        double m = data.sum() / n;
        data.sub(m);
        return m;
    }

    static boolean comespd(final int freq, final int nz, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(2);
        if (seas) {
            spec.setBD(1);
        }
        return TramoProcessor.autlar(nz, spec) >= 0;
    }
    private DataBlock data_ = null;
    private SarimaSpecification spec_ = new SarimaSpecification();
    private boolean seas_;
    private SarimaModel lastModel_;
    private RegArimaModel<SarimaModel> model_;
    private volatile double rmax_, rsmax_, c_, din_;
    private double ub1_ = 0.97;
    private double ub2_ = 0.91;
    private double cancel_ = 0.1;
    private int iter_;
    private boolean ml_, useml_, mlused_, bcalc_;
    private double tmean_;

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
        if (!seas_) {
            if (Math.abs(ar + 1) <= din_) {
                spec_.setD(spec_.getD() + 1);
            }
        } else {
            double sar = lastModel_.bphi(1);
            if (Math.abs(ar + 1) <= 0.15 || Math.abs(sar + 1) <= din_) {
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
        din_ = 0;

        step0();
        iter_ = 0;
        while (nextstep() && iter_ < 5) {
            ++iter_;
        }

        computeTMean();
    }

    /**
     *
     */
    public void clear() {
        lastModel_ = null;
        model_ = null;
        spec_ = new SarimaSpecification();
        bcalc_ = false;
        data_ = null;
        useml_ = false;
        tmean_ = 0;
    }

    private int cond1(int icon) // current condition status
    {
        // only for step1, when no differencing
        if (spec_.getD() + spec_.getBD() != 0) {
            return icon;
        }

        // spec_.D == 0 and spec_.BD == 0 and iround == 2 (cfr TRAMO)
        double ar = lastModel_.phi(1), ma = lastModel_.theta(1), sar = 0, sma = 0;
        if (seas_) {
            sar = lastModel_.bphi(1);
            sma = lastModel_.btheta(1);
        }
        // if cancelation ..., but big initial roots
        if ((Math.abs(ar - ma) < c_ || (seas_ && Math.abs(sar - sma) < c_))
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
//        else if (((Math.abs(ar + 1) <= 0.15 || (spec_.getFrequency() != 1 && Math.abs(sar + 1) <= 0.16)) && (rmax_ >= 0.9 || rsmax_ >= 0.88))
//                || ((Math.abs(ar + 1) <= 0.16 || (spec_.getFrequency() != 1 && Math.abs(sar + 1) <= 0.17)) && (rmax_ >= 0.91 || rsmax_ >= 0.89))) {
//            if (useml_ && icon == 1) {
//                useml_ = false;
//            } else {
//                ++icon;
//            }
//            if (rmax_ > rsmax_) {
//                spec_.setD(spec_.getD() + 1);
//            } else {
//                spec_.setBD(spec_.getBD() + 1);
//            }
//        }
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

        if (spec_.getD() == 3) {
            spec_.setD(2);
            icon = 0;
        }
        if (spec_.getBD() == 2) {
            spec_.setBD(1);
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

    public double getTMean() {
        return tmean_;
    }

    /**
     *
     * @return
     */
    public int getD() {
        return spec_.getD();
    }

    /**
     *
     * @return
     */
    public BackFilter getDifferencingFilter() {
        return spec_.getDifferencingFilter();
    }

    public boolean isMean() {
        return TramoProcessor.meantest(data_.getLength(), tmean_);
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

    private void initstep(boolean bstart) {
        if (spec_.getD() == 0 && spec_.getBD() == 0 && bstart) {
            if (spec_.getFrequency() != 2) {
                spec_.setP(2);
            } else {
                spec_.setP(1);
            }
            spec_.setQ(0);
            spec_.setBQ(0);
            if (seas_) {
                spec_.setBP(1);
            }
        } else {
            spec_.setP(1);
            spec_.setQ(1);
            if (seas_) {
                spec_.setBP(1);
                spec_.setBQ(1);
            }
        }

        model_ = new RegArimaModel<>(
                new SarimaModel(spec_), data_);
        model_.setMeanCorrection(true);
        DataBlock data = model_.getDModel().getY().deepClone();
        double mean = removeMean(data);

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
            //lastModel_.setDefault(-.1,-.1);
            GlsSarimaMonitor monitor = getMonitor();
            monitor.setPrecision(EPS);
            monitor.useMaximumLikelihood(!(bstart && spec_.getD() == 0 && spec_.getBD() == 0));
            RegArimaEstimation<SarimaModel> rslt = monitor.optimize(
                    model_, lastModel_);
            if (rslt == null) {
                throw new TramoException("Non convergence in ESPDIF");
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
        if (seas_) {
            sar = lastModel_.bphi(1);
            sma = lastModel_.btheta(1);
        }
        c_ -= 0.002;
        din_ = 1.005 - ub2_;
//        if (!mlused_ && ub2_ >= 0.869) {
//            din = .136;
//        } else {
//            din = 1.005 - ub2_;
//        }

        int icon = 0;

        // search for regular unit roots
        if (Math.abs(ar + 1) <= din_) // ar near -1
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
        if (seas_) {
            if (Math.abs(sar + 1) <= din_) {
                if (-sar > 1.02) {
                    useml_ = true;
                    icon = 1;
//                } else if (spec_.getBD() == 0 && (mlused_ || Math.abs(sar - sma) > c_)) {
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
        //allcond();
        return finalcond(icon) != 0;
    }

    /**
     *
     * @param context
     * @return
     */
    @Override
    public ProcessingResult process(ModellingContext context) {
        try {
            int freq = context.description.getFrequency();
            seas_ = context.hasseas;
            if (!comespd(freq, context.description.getEstimationDomain().getLength(), seas_)) {
                return ProcessingResult.Unprocessed;
            }
            if (context.estimation == null) {
                return ProcessingResult.Failed;
            }
            // correct data for estimated outliers...
            int xcount = context.estimation.getRegArima().getXCount();
            int xout = context.description.getOutliers().size();

            DataBlock res = context.estimation.getCorrectedData(xcount - xout, xcount);
            SarimaSpecification nspec = context.description.getSpecification();
            // get residuals
            process(freq, res, nspec.getD(), nspec.getBD());
            int nd = spec_.getD(), nbd = spec_.getBD();
            boolean changed = false;
            boolean nmean = isMean();
            if (nspec.getD() != nd || nspec.getBD() != nbd) {
                changed = true;
                SarimaSpecification cspec = new SarimaSpecification(freq);
                cspec.setD(nd);
                cspec.setBD(nbd);
                context.description.setSpecification(cspec);
                context.estimation = null;
            }
            if (nmean != context.description.isMean()) {
                changed = true;
                context.description.setMean(nmean);
                context.estimation = null;
            }
            addDifferencingInfo(context, nd, nbd, nmean);
            return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;

        } catch (RuntimeException err) {
            context.description.setAirline(context.hasseas);
            context.estimation = null;
            return ProcessingResult.Failed;
        }
    }

    /**
     *
     * @param freq
     * @param data
     * @param d
     * @param bd
     */
    private void process(int freq, IReadDataBlock data, int d, int bd) {
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
            double cdim = Math.abs(r[i].im);
            double vcur = (r[i].abs());
            if (vcur >= val && cdim <= 0.05 && r[i].re > 0) {
                ++n;
            } else if (cdim <= 0.02 && r[i].re > 0 && vcur > vmax) {
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
            if (seas_) {
                rsmax_ = lastModel_.bphi(1);
            }
        }

        Complex[] rar = lastModel_.getRegularAR().mirror().roots();
        spec_.setD(spec_.getD() + searchur(rar, ub1_, true));
        if (seas_) {
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

    private void computeTMean() {
        DataBlock res = null;
        if (spec_.getD() == 0 && spec_.getBD() == 0) {
            res = data_;
        } else {
            if (lastModel_ == null) {
                throw new TramoException(TramoException.ESPDIF_E);
            }

            SarimaMapping.stabilize(lastModel_);
            ArmaKF kf = new ArmaKF(lastModel_);
            BackFilter D = getDifferencingFilter();
            res = new DataBlock(data_.getLength() - D.getDegree());
            D.filter(data_, res);
            res = kf.fastFilter(res);
        }
        double s = res.sum(), s2 = res.ssq();
        int n = res.getLength();
        tmean_ = s / Math.sqrt((s2 * n - s * s) / n);
    }

    private void addDifferencingInfo(ModellingContext context, int d, int bd, boolean mean) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("D=").append(d);
//            builder.append(", BD=").append(bd);
//            if (mean) {
//                builder.append(" +mean");
//            }
//            context.processingLog.add(ProcessingInformation.info(DIFFERENCING,
//                    DifferencingModule.class.getName(), builder.toString(), null));
//        }
//
    }
    private static final String DIFFERENCING = "Differencing";

}
