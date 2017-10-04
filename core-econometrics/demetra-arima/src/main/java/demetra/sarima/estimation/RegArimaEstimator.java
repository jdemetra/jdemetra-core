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
import demetra.arima.regarima.internal.RegArmaEstimation;
import demetra.arima.regarima.internal.RegArmaModel;
import demetra.arima.regarima.internal.RegArmaProcessor;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaEstimator implements IRegArimaProcessor<SarimaModel> {

    public static class Builder implements IBuilder<RegArimaEstimator> {

        private Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
        private double eps = DEF_EPS, feps = DEF_INTERNAL_EPS;
        private boolean ml = true, mt = false, cdf = true;
        private StartingPoint start = StartingPoint.Multiple;

        public Builder mapping(Function<SarimaModel, IParametricMapping<SarimaModel>> mapping) {
            this.mappingProvider = mapping;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder preliminaryEpsilon(double eps) {
            this.feps = eps;
            return this;
        }

        public Builder useParallelProcessing(boolean mt) {
            this.mt = mt;
            return this;
        }

        public Builder useMaximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        public Builder useCorrectedDegreesOfFreedom(boolean cdf) {
            this.cdf = cdf;
            return this;
        }

        public Builder startingPoint(StartingPoint start) {
            this.start = start;
            return this;
        }

        @Override
        public RegArimaEstimator build() {
            return new RegArimaEstimator(this.mappingProvider, this.eps, this.feps, this.ml, this.start, this.cdf, this.mt);
        }
    }

    @lombok.Value
    private static class Estimation {

        RegArimaEstimation<SarimaModel> estimation;
        Matrix pcov;
        double[] score;

    }

    public static final String SCORE = "score", OPTIMIZATION = "optimization";

    public static enum StartingPoint {

        Zero,
        Default,
        HannanRissanen,
        Multiple
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final double DEF_EPS = 1e-7, DEF_INTERNAL_EPS = 1e-4;
    private final Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
    private final double eps, feps;
    private final boolean ml, mt, cdf;
    private final StartingPoint start;
    private Matrix pcov;
    private double[] score;
    protected ISsqFunctionMinimizer min = new LevenbergMarquardtMinimizer();

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public StartingPoint getStartingPoint() {
        return start;
    }

    public RegArimaEstimator(Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider,
            final double eps, final double feps, final boolean ml, final StartingPoint start, final boolean cdf, final boolean mt) {
        if (mappingProvider == null) {
            this.mappingProvider = m -> SarimaMapping.stationary(m.specification());
        } else {
            this.mappingProvider = mappingProvider;
        }
        this.eps = eps;
        this.feps = feps;
        this.ml = ml;
        this.mt = mt;
        this.cdf = cdf;
        this.start = start;
    }

    public void setMinimizer(ISsqFunctionMinimizer min) {
        this.min = min;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        if (mapping.getDim() == 0) {
            return RegArimaEstimation.compute(regs);
        }
        SarimaModel mstart;
        if (this.start == StartingPoint.HannanRissanen || this.start == StartingPoint.Multiple) {
            HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                    .stabilize(true)
                    .useDefaultIfFailed(true).build();
            RegArmaModel<SarimaModel> dregs = RegArmaModel.of(regs);

            SarimaModel starthr = initializer.initialize(dregs);
            SarimaSpecification spec = starthr.specification();
            mstart = starthr;
            if (this.start == StartingPoint.Multiple) {
                Estimation mhr = estimate(regs, starthr);
                SarimaModel startdef = mhr.getEstimation().getModel().arima();
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
                    startdef = nstart.adjustOrders(false).build();
                }

                Estimation mdef = estimate(regs, startdef);
                if (mdef != null) {
                    if (mhr.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood() < mdef.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood()) {
                        mstart = (SarimaModel) mdef.getEstimation().getModel().arima().stationaryTransformation().getStationaryModel();
                    } else {
                        mstart = (SarimaModel) mhr.getEstimation().getModel().arima().stationaryTransformation().getStationaryModel();
                    }
                }
            }
        } else if (this.start == StartingPoint.Default) {
            mstart = SarimaModel.builder(regs.arima().specification().doStationary())
                    .setDefault().build();
        } else {
            mstart = SarimaModel.builder(regs.arima().specification().doStationary())
                    .setDefault(0, 0).build();
        }
        Estimation rslt = optimize(regs, mstart);
        pcov = rslt.getPcov();
        score = rslt.getScore();
        return rslt.getEstimation();
    }

    public double getPreliminaryPrecision() {
        return feps;
    }

    @Override
    public double getPrecision() {
        return eps;
    }

    public Matrix getParametersCovariance() {
        return pcov;
    }

    public double[] getScore() {
        return score;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        SarimaModel arima = regs.arima();
        DataBlock p = DataBlock.of(mapping.map(arima));
        if (mapping.validate(p) == ParamValidation.Changed) {
            arima = mapping.map(p);
        }
        Estimation rslt = optimize(regs, (SarimaModel) arima.stationaryTransformation().getStationaryModel());
        pcov = rslt.getPcov();
        score = rslt.getScore();
        return rslt.getEstimation();
    }

    private Estimation optimize(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        score = null;
        pcov = null;
        if (mapping.getDim() == 0) {
            return new Estimation(RegArimaEstimation.compute(regs), null, null);
        }
        Estimation rslt = optimize(regs, start.parameters(), eps);
        if (rslt != null) {
            return finalProcessing(rslt);
        } else {
            return null;
        }
    }

    private Estimation estimate(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        if (mapping.getDim() == 0) {
            return new Estimation(RegArimaEstimation.compute(regs), null, null);
        }
        return optimize(regs, start.parameters(), feps);
    }

    private Estimation finalProcessing(Estimation estimation) {
        Estimation nestimation = tryUrpCancelling(estimation);
        Estimation festimation = tryUrmCancelling(nestimation);
//        computepvar(monitor_, fmodel);
//        score_ = monitor_.getScore();
        return festimation;
//        computepvar(monitor_, model);
//        score_ = monitor_.getScore();
//        return model;
    }

    private Estimation tryUrpCancelling(Estimation estimation) {
        SarimaModel arima = estimation.getEstimation().getModel().arima();
        SarimaSpecification spec = arima.specification();
        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getDifferenceOrder() == 0) {
            return estimation;
        }

        DataBlock parameters = DataBlock.of(arima.parameters());
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
            IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(arima);
            mapping.validate(parameters);
            Estimation nrslts = optimize(estimation.getEstimation().getModel(), parameters, eps);
            if (nrslts.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood() > estimation.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood()) {
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }

    private Estimation tryUrmCancelling(Estimation estimation) {
        SarimaModel arima = estimation.getEstimation().getModel().arima();
        SarimaSpecification spec = arima.specification();
        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getBD() == 0) {
            return estimation;
        }

        DataBlock parameters = DataBlock.of(arima.parameters());
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
            IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(arima);
            mapping.validate(parameters);
            Estimation nestimation = optimize(estimation.getEstimation().getModel(), parameters, eps);
            if (nestimation.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood() > estimation.getEstimation().getConcentratedLikelihood().getLikelihood().logLikelihood()) {
                return nestimation;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }
    private static final double UR_LIMIT = .1;

    private Estimation optimize(RegArimaModel<SarimaModel> regs, DoubleSequence start, double prec) {
        RegArmaModel<SarimaModel> dmodel = RegArmaModel.of(regs);
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt);

        IParametricMapping<SarimaModel> mapping = mappingProvider.apply(dmodel.getArma());
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();
        if (cdf) {
            ndf -= mapping.getDim();
        }
        min.setFunctionPrecision(prec);
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, start, mapping, min, ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().specification())
                .parameters(rslt.getModel().getArma().parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);
        Matrix cov = rslt.information();
        if (cov != null) {
            cov = SymmetricMatrix.inverse(cov);
        }

        return new Estimation(RegArimaEstimation.compute(nmodel), cov, rslt.score());
    }
}
