/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.tramo.internal;

import demetra.arima.internal.FastKalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.design.VisibleForTesting;
import demetra.maths.Complex;
import demetra.maths.linearfilters.BackFilter;
import demetra.modelling.Variable;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.IDifferencingModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.tramo.TramoException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DifferencingModule implements IDifferencingModule {

    public static final int MAXD = 2, MAXBD = 1;

    static boolean comespd(final int freq, final int nz, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(2);
        if (seas) {
            spec.setBd(1);
        }
        return TramoUtility.autlar(nz, spec) >= 0;
    }

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
        private boolean seasonal=true;

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

        public Builder seasonal(boolean seasonal) {
            this.seasonal=seasonal;
            return this;
        }

        public DifferencingModule build() {
            return new DifferencingModule(maxd, maxbd, ub1, ub2, cancel, eps, seasonal);
        }
    }

    private static double removeMean(DataBlock x) {
        double m = x.average();
        x.sub(m);
        return m;
    }

    private double[] y;
    private SarimaSpecification spec;
    private SarimaModel lastModel;
    private double rmax, rsmax, c, din, tmean;
    private int iter;
    private boolean ml, useml, mlused;
    private final int maxd, maxbd;
    private final double ub1;
    private final double ub2;
    private final double cancel;
    private final double eps;
    private final boolean seasonal;

    /**
     *
     * @param maxd
     * @param maxbd
     * @param ub1
     * @param ub2
     * @param cancel
     * @param eps
     */
    private DifferencingModule(final int maxd, final int maxbd,
            final double ub1, final double ub2, final double cancel,
            final double eps, final boolean seasonal) {
        this.maxd = maxd;
        this.maxbd = maxbd;
        this.ub1 = ub1;
        this.ub2 = ub2;
        this.cancel = cancel;
        this.eps = eps;
        this.seasonal=seasonal;
    }

    private boolean calc() {
        if (y == null) {
            return false;
        }
        c = cancel;
        useml = false;
        mlused = false;
        rsmax = 0;
        rmax = 0;
        din = 0;

        step0();
        iter = 0;
        while (nextstep() && iter < 5) {
            ++iter;
        }

        computeTMean();
        return true;
    }

    /**
     *
     */
    public void clear() {
        lastModel = null;
        spec = null;
        y = null;
        useml = false;
        tmean = 0;
    }

    private int cond1(int icon) // current condition status
    {
        // only for step1, when no differencing
        if (spec.getD() + spec.getBd() != 0) {
            return icon;
        }

        // spec.D == 0 and spec.BD == 0 and iround == 2 (cfr TRAMO)
        double ar = lastModel.phi(1), ma = lastModel.theta(1), sar = 0, sma = 0;
        if (maxbd > 0) {
            sar = lastModel.bphi(1);
            sma = lastModel.btheta(1);
        }
        // if cancelation ..., but big initial roots
        if ((Math.abs(ar - ma) < c || (maxbd > 0 && Math.abs(sar - sma) < c))
                && (rmax >= 0.9 || rsmax >= 0.9)) {
            if (useml && icon == 1) {
                useml = false;
            } else {
                ++icon;
            }
            if (rmax > rsmax) {
                spec.setD(spec.getD() + 1);
            } else {
                spec.setBd(spec.getBd() + 1);
            }
        } // if big initial roots and coef near -1
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

    public double getTMean() {
        return tmean;
    }

    public boolean isMeanCorrection() {
        double vct = 2.5;
        int n = y.length;
        if (n <= 80) {
            vct = 1.96;
        } else if (n <= 155) {
            vct = 1.98;
        } else if (n <= 230) {
            vct = 2.1;
        } else if (n <= 320) {
            vct = 2.3;
        }
        return Math.abs(tmean) > vct;
    }
    
    public int getD(){
        return spec.getD();
    }
    
    public int getBd(){
        return spec.getBd();
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
            if (maxbd > 0) {
                spec.setBp(1);
            }
        } else {
            spec.setP(1);
            spec.setQ(1);
            if (maxbd > 0) {
                spec.setBp(1);
                spec.setBq(1);
            }
        }

        BackFilter ur = RegArimaUtility.differencingFilter(spec.getPeriod(), spec.getD(), spec.getBd());
        DataBlock data;
        if (ur.getDegree() > 0) {
            data = DataBlock.make(y.length - ur.getDegree());
            ur.apply(DataBlock.ofInternal(y), data);
        } else {
            data = DataBlock.copyOf(y);
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
                    SarimaMapping.stabilize(lastModel);
                }
            }
        }

        if (usedefault) {
            lastModel = SarimaModel.builder(spec.doStationary()).setDefault().build();
        }

        if (usedefault || ml || useml) {
            SarimaMapping.stabilize(lastModel);
            IRegArimaProcessor processor = TramoUtility.processor(true, eps);
            RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class).y(data).arima(lastModel).build();
            RegArimaEstimation<SarimaModel> rslt = processor.optimize(regarima);
            if (rslt == null) {
                throw new TramoException("Non convergence in ESPDIF");
            }
            lastModel = rslt.getModel().arima();
            mlused = true;
        } else {
            mlused = false;
        }
        useml = false;
    }

    private int maincondition() {
        double ar = lastModel.phi(1), ma = lastModel.theta(1), sar = 0, sma = 0;
        if (maxbd > 0) {
            sar = lastModel.bphi(1);
            sma = lastModel.btheta(1);
        }
        c -= 0.002;
        din = 1.005 - ub2;
//        if (!mlused_ && ub2_ >= 0.869) {
//            din = .136;
//        } else {
//            din = 1.005 - ub2_;
//        }

        int icon = 0;

        // searchVariable for regular unit roots
        if (Math.abs(ar + 1) <= din) // ar near -1
        {
            if (-ar > 1.02) // |ar| too big (bad estimation)
            {
                icon = 1;
                useml = true;
            } else if (Math.abs(ar - ma) > c) // no cancelation
            {
                ++icon;
                spec.setD(spec.getD() + 1);
            }
        } else if (Math.abs(ar) > 1.12) // |ar| too big (bad estimation)
        {
            icon = 1;
            useml = true;
        }
        if (maxbd > 0) {
            if (Math.abs(sar + 1) <= din) {
                if (-sar > 1.02) {
                    useml = true;
                    icon = 1;
//                } else if (spec.getBd() == 0 && (mlused_ || Math.abs(sar - sma) > c_)) {
                } else if (spec.getBd() == 0 && Math.abs(sar - sma) > c) {
                    ++icon;
                    spec.setBd(spec.getBd() + 1);
                    if (useml) {
                        --icon;
                        useml = false;
                    }
                }
            } else if (Math.abs(sar) > 1.12) {
                icon = 1;
                useml = true;
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
        //allcond();
        return finalcond(icon) != 0;
    }

    /**
     *
     * @param data
     * @param period
     * @param d
     * @param bd
     * @param seasonal
     * @return 
     */
    @VisibleForTesting
    public boolean process(DoubleSequence data, int period, int d, int bd, boolean seasonal) {
        clear();
        y = data.toArray();
        spec = new SarimaSpecification(period);
        spec.setD(d);
        if (seasonal) {
            spec.setBd(bd);
        }

        return calc();
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
            if (maxbd > 0) {
                rsmax = lastModel.bphi(1);
            }
        }

        Complex[] rar = lastModel.getRegularAR().mirror().roots();
        spec.setD(spec.getD() + searchur(rar, ub1, true));
        if (maxbd > 0) {
            Complex[] rsar = lastModel.getSeasonalAR().mirror().roots();
            spec.setBd(spec.getBd() + searchur(rsar, ub1, false));
        }
    }

    private void computeTMean() {
        DataBlock res = null;
        if (spec.getD() == 0 && spec.getBd() == 0) {
            res = DataBlock.ofInternal(y);
        } else {
            if (lastModel == null) {
                throw new TramoException(TramoException.IDDIF_E);
            }

            SarimaMapping.stabilize(lastModel);
            FastKalmanFilter kf = new FastKalmanFilter(lastModel);
            BackFilter D = RegArimaUtility.differencingFilter(spec.getPeriod(), spec.getD(), spec.getBd());
            res = DataBlock.make(y.length - D.getDegree());
            D.apply(DataBlock.ofInternal(y), res);
            res = kf.fastFilter(res);
        }
        double s = res.sum(), s2 = res.ssq();
        int n = res.length();
        tmean = s / Math.sqrt((s2 * n - s * s) / n);
    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        if (context.needEstimation())
            context.estimate(eps);
        ModelEstimation estimation = context.getEstimation();

        int freq = desc.getAnnualFrequency();
        try {
            if (!DifferencingModule.comespd(freq, desc.regarima().getObservationsCount(), seasonal)) {
                return airline(context);
            }

            Stream<Variable> filter = desc.variables().filter(var -> var.isOutlier(false));
            int nvars = (int) filter.count();
            DoubleSequence res = RegArimaUtility.interpolatedData(desc.regarima(), estimation.getConcentratedLikelihood());
            if (nvars > 0) {
                Optional<Variable> first = filter.findFirst();
                // remove the outliers effects
                DoubleSequence outs = RegArimaUtility.regressionEffect(desc.regarima(), estimation.getConcentratedLikelihood(), desc.findPosition(first.get().getVariable()), nvars);
                res = Doubles.op(res, outs, (a, b) -> a - b);
            }
            SarimaSpecification curspec = desc.getSpecification();
            // get residuals
            if (!process(res, freq, curspec.getD(), curspec.getBd(), seasonal)) {
                return airline(context);
            }
            boolean nmean = isMeanCorrection();
            boolean changed = false;
            if (spec.getD() != curspec.getD() || spec.getBd() != curspec.getBd()) {
                changed = true;
                desc.setSpecification(spec);
                context.setEstimation(null);
            }
            if (nmean != desc.isMean()) {
                changed = true;
                desc.setMean(nmean);
                context.setEstimation(null);
            }
            return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
        } catch (RuntimeException err) {
            return airline(context);
        }

    }

    private ProcessingResult airline(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        if (!desc.getSpecification().isAirline(seasonal)) {
            desc.setAirline(seasonal);
            desc.setMean(false);
            context.setEstimation(null);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unprocessed;
        }

    }
}
