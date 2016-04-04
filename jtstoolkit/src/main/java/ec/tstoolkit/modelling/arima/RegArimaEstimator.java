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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.estimation.IRegArimaProcessor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaFixedMapping;
import ec.tstoolkit.sarima.estimation.SarimaInitializer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaEstimator implements IRegArimaProcessor<SarimaModel> {

    public static final String SCORE = "score", OPTIMIZATION = "optimization";
    public static enum StartingPoint {

        Zero,
        Default,
        HannanRissanen,
        Multiple
    }
    public static final double DEF_EPS = 1e-7, DEF_INTERNAL_EPS = 1e-4;
    protected double eps_ = DEF_EPS, feps_;
    protected boolean ml_ = true, logll_ = false, fml_;
    protected StartingPoint start_ = StartingPoint.Multiple;
    protected Matrix pcov_;
    protected double[] score_;
    protected final IParametricMapping<SarimaModel> mapping_;
    protected GlsSarimaMonitor monitor_;
    protected IFunctionMinimizer min_ = new ProxyMinimizer(new LevenbergMarquardtMethod());

    public boolean isMaximumLikelihood() {
        return ml_;
    }

    public boolean isLogLikelihood() {
        return logll_;
    }

    public StartingPoint getStartingPoint() {
        return start_;
    }

    public void setStartingPoint(StartingPoint start) {
        start_ = start;
    }

    public void setMaximumLikelihood(boolean ml) {
        ml_ = ml;
    }

    public void setLogLikelihood(boolean logll) {
        logll_ = logll;
    }

    public RegArimaEstimator(IParametricMapping<SarimaModel> mapper) {
        mapping_ = mapper;
    }

    public void setMinimizer(IFunctionMinimizer min) {
        min_ = min;
    }

    public void setMinimizer(ISsqFunctionMinimizer min) {
        min_ = new ProxyMinimizer(min);
    }

    public IFunctionMinimizer getMinimizer() {
        return min_;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {

        if (mapping_.getDim() == 0 ) {
            return new RegArimaEstimation<>(regs, regs.computeLikelihood());
        }
        SarimaModel start;
        if (start_ == StartingPoint.HannanRissanen || start_ == StartingPoint.Multiple) {
            SarimaInitializer initializer = new SarimaInitializer();
            initializer.useDefaultIfFailed(true);
            SarimaModel starthr = initializer.initialize(regs);

            SarimaSpecification spec = starthr.getSpecification();
            start = starthr;
            if (start_ == StartingPoint.Multiple) {
                RegArimaEstimation<SarimaModel> mhr = estimate(regs, starthr);
                SarimaModel startdef = mhr.model.getArma().clone();
                if (spec.getP() > 0 && spec.getQ() > 0) {
//                    double phi = rmax(startdef.getRegularAR());
//                    double th = rmax(startdef.getRegularMA());
//                    startdef.setDefault(0, 0);
//                    if (phi != 0) {
//                        startdef.setTheta(1, phi);
//                    }
//                    if (th != 0) {
//                        startdef.setPhi(1, th);
//                    }
//                } else {
//                    startdef.setDefault(0, 0);
//                    double p = rmax(startdef.getRegularAR());
                    double p = startdef.theta(1) < 0 ? .9 : -.9;
                    startdef.setTheta(1, p);
                    for (int i = 1; i <= spec.getP(); ++i) {
                        startdef.setPhi(i, 0);
                    }
                    for (int i = 2; i <= spec.getQ(); ++i) {
                        startdef.setTheta(i, 0);
                    }
                }

                RegArimaEstimation<SarimaModel> mdef = estimate(regs, startdef);
                if (mdef != null) {
                    if (mhr.likelihood.getLogLikelihood() < mdef.likelihood.getLogLikelihood()) {
                        start = mdef.model.getArma();
                    } else {
                        start = mhr.model.getArma();
                    }
                }
            }
        } else if (start_ == StartingPoint.Default) {
            start = regs.getArma();
            start.setDefault();
        } else {
            start = regs.getArma();
            start.setDefault(0, 0);
        }
        return optimize(regs, start);
    }

    public double getFinalPrecision(double value) {
        return feps_;
    }

    public boolean hasUsedMaximumLikelihood() {
        return fml_;
    }

    @Override
    public double getPrecision() {
        return eps_;
    }

    public Matrix getParametersCovariance() {
        return pcov_;
    }

    public double[] getScore() {
        return score_;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        SarimaModel arima = regs.getArima();
        DataBlock p = new DataBlock(mapping_.map(arima));
        if (mapping_.validate(p) == ParamValidation.Changed) {
            arima = mapping_.map(p);
        }
        return optimize(regs, (SarimaModel) arima.stationaryTransformation().stationaryModel);
    }

    protected GlsSarimaMonitor createProcessor() {
        return createProcessor(eps_);
    }

    protected GlsSarimaMonitor createProcessor(double eps) {
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(min_.exemplar());
        monitor.setPrecision(eps);
        monitor.useMaximumLikelihood(ml_);
        monitor.useLogLikelihood(logll_);
        monitor.setMapping(mapping_);
        return monitor;
    }

    protected RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel start) {

        monitor_ = null;
        score_ = null;
        if (mapping_.getDim() == 0) {
            return new RegArimaEstimation<>(regs, regs.computeLikelihood());
        }
        fml_ = ml_;
        feps_ = eps_;
        monitor_ = createProcessor();
        RegArimaEstimation<SarimaModel> rslt = monitor_.optimize(regs, start);
        if (rslt != null) {
            return finalProcessing(rslt);
        } else {
            fml_ = false;
            monitor_.useMaximumLikelihood(false);
            int iter = 0;
            do {
                feps_ *= 10;
                monitor_.setPrecision(feps_);
                rslt = monitor_.optimize(regs, rslt.model.getArma());
            } while (rslt == null && ++iter < 3);
            if (rslt == null) {
                return null;

            } else {
                return finalProcessing(rslt);
//                computepvar(monitor, rslt);

            }
        }
    }

    protected RegArimaEstimation<SarimaModel> estimate(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        if (mapping_.getDim() == 0) {
            return new RegArimaEstimation<>(regs, regs.computeLikelihood());
        }
        fml_ = ml_;
        feps_ = eps_;
        GlsSarimaMonitor monitor = createProcessor(DEF_INTERNAL_EPS);
        monitor.getMinimizer().setMaxIter(10);
        return monitor.optimize(regs, start);
    }

    @Override
    public void setPrecision(double value) {
        eps_ = value;
    }

    private RegArimaEstimation<SarimaModel> finalProcessing(RegArimaEstimation<SarimaModel> model) {
        RegArimaEstimation<SarimaModel> nmodel = tryUrpCancelling(model);
        RegArimaEstimation<SarimaModel> fmodel = tryUrmCancelling(nmodel);
        computepvar(monitor_, fmodel);
        score_ = monitor_.getScore();
        return fmodel;
//        computepvar(monitor_, model);
//        score_ = monitor_.getScore();
//        return model;
    }

    private RegArimaEstimation<SarimaModel> tryUrpCancelling(RegArimaEstimation<SarimaModel> estimation) {
        SarimaModel arima = estimation.model.getArima();
        SarimaSpecification spec = arima.getSpecification();
        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getDifferenceOrder() == 0) {
            return estimation;
        }

        DataBlock parameters = new DataBlock(arima.getParameters());
        // creates AR and MA polynomials
        int pstart = 0, pend = spec.getP();
        double p = 0;
        for (int i = pstart; i < pend; ++i) {
            p += parameters.get(i);
        }
        if (Math.abs(p + 1) > UR_LIMIT) {
            return estimation;
        }
        int qstart = pend + spec.getBP(), qend = qstart + spec.getQ();
        double q = 0;
        for (int i = qstart; i < qend; ++i) {
            q += parameters.get(i);
        }
        if (Math.abs(q + 1) > UR_LIMIT) {
            return estimation;
        }
        // simplify by (1-B). Horner with -1
        double tmp = 0;
        for (int i = pend - 1; i >= pstart; --i) {
            double c = parameters.get(i);
            parameters.set(i, -tmp);
            tmp += c;
        }
        tmp = 0;
        for (int i = qend - 1; i >= qstart; --i) {
            double c = parameters.get(i);
            parameters.set(i, -tmp);
            tmp += c;
        }

        // try the new model
        try {
            GlsSarimaMonitor nmonitor = createProcessor();
            RegArimaModel<SarimaModel> nmodel = estimation.model.clone();
            mapping_.validate(parameters);
            nmodel.getArima().setParameters(parameters);
            RegArimaEstimation<SarimaModel> nrslts = nmonitor.optimize(nmodel);
            if (nrslts.likelihood.getLogLikelihood() > estimation.likelihood.getLogLikelihood()) {
                monitor_ = nmonitor;
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }

    private RegArimaEstimation<SarimaModel> tryUrmCancelling(RegArimaEstimation<SarimaModel> estimation) {
        SarimaModel arima = estimation.model.getArima();
        SarimaSpecification spec = arima.getSpecification();
        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getBD() == 0) {
            return estimation;
        }

        DataBlock parameters = new DataBlock(arima.getParameters());
        // creates AR and MA polynomials
        int pstart = 0, pend = spec.getP();
        double p = 0;
        boolean pos = false;
        for (int i = pstart; i < pend; ++i) {
            p += pos ? parameters.get(i) : -parameters.get(i);
            pos = !pos;
        }
        if (Math.abs(p + 1) > UR_LIMIT) {
            return estimation;
        }
        int qstart = pend + spec.getBP(), qend = qstart + spec.getQ();
        double q = 0;
        pos = false;
        for (int i = qstart; i < qend; ++i) {
            q += pos ? parameters.get(i) : -parameters.get(i);
            pos = !pos;
        }
        if (Math.abs(q + 1) > UR_LIMIT) {
            return estimation;
        }
        // simplify by (1+B). Horner with -1
        double tmp = 0;
        for (int i = pend - 1; i >= pstart; --i) {
            double c = parameters.get(i);
            parameters.set(i, tmp);
            tmp = c - tmp;
        }
        tmp = 0;
        for (int i = qend - 1; i >= qstart; --i) {
            double c = parameters.get(i);
            parameters.set(i, tmp);
            tmp = c - tmp;
        }

        // try the new model
        try {
            GlsSarimaMonitor nmonitor = createProcessor();
            RegArimaModel<SarimaModel> nmodel = estimation.model.clone();
            mapping_.validate(parameters);
            nmodel.getArima().setParameters(parameters);
            RegArimaEstimation<SarimaModel> nrslts = nmonitor.optimize(nmodel);
            if (nrslts.likelihood.getLogLikelihood() > estimation.likelihood.getLogLikelihood()) {
                monitor_ = nmonitor;
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }
    private static final double UR_LIMIT = .1;

    protected void computepvar(GlsSarimaMonitor monitor, RegArimaEstimation<SarimaModel> rslt) {
        int n = rslt.likelihood.getDegreesOfFreedom(true, mapping_.getDim());
        Matrix information = monitor.getObservedInformation(n);
        if (information == null) {
            return;
        }
        pcov_ = SymmetricMatrix.inverse(information);
        if (pcov_ == null) {
            return;
        }
        // inflate pcov_, if need be
        if (monitor.getMapping() instanceof SarimaFixedMapping) {
            SarimaFixedMapping mapping = (SarimaFixedMapping) monitor.getMapping();
            pcov_ = mapping.expandCovariance(pcov_);
        }
    }

    private double rmax(Polynomial p) {
        p = p.adjustDegree();
        if (p.getDegree() == 1) {
            return -p.get(1);
        }
        Complex[] roots = p.roots();
        double rmax = 0;
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].getIm() == 0) {
                if (rmax == 0 || Math.abs(roots[i].getRe()) < Math.abs(rmax)) {
                    rmax = roots[i].getRe();
                }
            }
        }
        if (rmax == 0) {
            return 0;
        }
        rmax = 1 / rmax;
        if (rmax < -.9) {
            rmax = -.9;
        } else if (rmax > .9) {
            rmax = .9;
        }
        return rmax;
    }
}
