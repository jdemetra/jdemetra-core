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
package demetra.sarima.estimation;

import demetra.arima.regarima.IRegArimaProcessor;
import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.arima.regarima.internal.RegArmaModel;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.functions.IFunctionMinimizer;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.functions.ssq.ProxyMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;

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
    protected double eps = DEF_EPS, feps_;
    protected boolean ml_ = true, logll_ = false, fml_;
    protected StartingPoint start = StartingPoint.Multiple;
    protected Matrix pcov_;
    protected double[] score_;
    protected final IParametricMapping<SarimaModel> mapping;
    protected GlsSarimaMonitor monitor_;
    protected ISsqFunctionMinimizer min = new LevenbergMarquardtMinimizer();

    public boolean isMaximumLikelihood() {
        return ml_;
    }

    public boolean isLogLikelihood() {
        return logll_;
    }

    public StartingPoint getStartingPoint() {
        return start;
    }

    public void setStartingPoint(StartingPoint start) {
        this.start = start;
    }

    public void setMaximumLikelihood(boolean ml) {
        ml_ = ml;
    }

    public void setLogLikelihood(boolean logll) {
        logll_ = logll;
    }

    public RegArimaEstimator(IParametricMapping<SarimaModel> mapper) {
        mapping = mapper;
    }


    public void setMinimizer(ISsqFunctionMinimizer min) {
        this.min = min;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {

        if (mapping.getDim() == 0) {
            return RegArimaEstimation.compute(regs);
        }
        SarimaModel start;
        if (this.start == StartingPoint.HannanRissanen || this.start == StartingPoint.Multiple) {
            HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                    .useDefaultIfFailed(true).build();
            RegArmaModel<SarimaModel> dregs = RegArmaModel.of(regs);

            SarimaModel starthr = initializer.initialize(dregs);
            SarimaSpecification spec = starthr.specification();
            start = starthr;
            if (this.start == StartingPoint.Multiple) {
                RegArimaEstimation<SarimaModel> mhr = estimate(regs, starthr);
                SarimaModel startdef = mhr.getModel().arima();
                if (spec.getP() > 0 && spec.getQ() > 0) {
                    SarimaModel.Builder nstart = startdef.toBuilder();
                    double p = startdef.theta(1) < 0 ? .9 : -.9;
                    nstart.theta(1, p);
                    for (int i = 1; i <= spec.getP(); ++i) {
                        nstart.phi(i, 0);
                    }
                    for (int i = 2; i <= spec.getQ(); ++i) {
                        nstart.theta(i, 0);
                    }
                    startdef=nstart.build();
                }
                

                RegArimaEstimation<SarimaModel> mdef = estimate(regs, startdef);
                if (mdef != null) {
                    if (mhr.getEstimation().getLikelihood().logLikelihood()< mdef.getEstimation().getLikelihood().logLikelihood()) {
                        start = (SarimaModel) mdef.getModel().arima().stationaryTransformation().getStationaryModel();
                    } else {
                        start = (SarimaModel) mhr.getModel().arima().stationaryTransformation().getStationaryModel();
                    }
                }
            }
        } else if (this.start == StartingPoint.Default) {
            start=SarimaModel.builder(regs.arima().specification().doStationary())
                    .setDefault().build();
        } else {
            start = SarimaModel.builder(regs.arima().specification().doStationary())
                    .setDefault(0,0).build();
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
        return eps;
    }

    public Matrix getParametersCovariance() {
        return pcov_;
    }

    public double[] getScore() {
        return score_;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        SarimaModel arima = regs.arima();
        DataBlock p = DataBlock.of(mapping.map(arima));
        if (mapping.validate(p) == ParamValidation.Changed) {
            arima = mapping.map(p);
        }
        return optimize(regs, (SarimaModel) arima.stationaryTransformation().getStationaryModel());
    }

    protected GlsSarimaMonitor createProcessor() {
        return createProcessor(eps);
    }

    protected GlsSarimaMonitor createProcessor(double eps) {
        GlsSarimaMonitor monitor = GlsSarimaMonitor.builder()
                .minimizer(min.exemplar())
                .precision(eps)
                .useMaximumLikelihood(ml_)
                .build();
        return monitor;
    }

    protected GlsSarimaMonitor createFastProcessor(double eps) {
        GlsSarimaMonitor monitor = GlsSarimaMonitor.builder()
                .minimizer(min.exemplar())
                .precision(eps)
                .useMaximumLikelihood(ml_)
                .build();
        return monitor;
    }

    protected RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel start) {

        monitor_ = null;
        score_ = null;
        if (mapping.getDim() == 0) {
            return RegArimaEstimation.compute(regs);
        }
        fml_ = ml_;
        feps_ = eps;
        monitor_ = createProcessor();
        RegArimaEstimation<SarimaModel> rslt = monitor_.optimize(regs, start);
        if (rslt != null) {
            return finalProcessing(rslt);
        } else {
           return null;
        }
    }

    protected RegArimaEstimation<SarimaModel> estimate(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        if (mapping.getDim() == 0) {
            return new RegArimaEstimation<>(regs, regs.computeLikelihood());
        }
        fml_ = ml_;
        feps_ = eps;
        GlsSarimaMonitor monitor = createProcessor(DEF_INTERNAL_EPS);
        return monitor.optimize(regs, start);
    }

    @Override
    public void setPrecision(double value) {
        eps = value;
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
            nmodel.setArima(nmodel.getArima().clone());
            mapping.validate(parameters);
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
            nmodel.setArima(nmodel.getArima().clone());
            mapping.validate(parameters);
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
        int n = rslt.getEstimation().likelihood.getDegreesOfFreedom(true, mapping.getDim());
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

}