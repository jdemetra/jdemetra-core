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
package demetra.sarima;

import demetra.sarima.internal.HannanRissanenInitializer;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.internal.RegArmaEstimation;
import demetra.regarima.RegArmaModel;
import demetra.regarima.internal.RegArmaProcessor;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.regarima.RegArimaMapping;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegSarimaProcessor implements IRegArimaProcessor<SarimaModel> {

    public static class Builder implements IBuilder<RegSarimaProcessor> {

        private Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
        private double eps = DEF_EPS, feps = DEF_INTERNAL_EPS;
        private boolean ml = true, mt = false, cdf = true;
        private StartingPoint start = StartingPoint.Multiple;
        private ISsqFunctionMinimizer min = new LevenbergMarquardtMinimizer();

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

        public Builder minimizer(ISsqFunctionMinimizer min) {
            this.min = min;
            return this;
        }

        @Override
        public RegSarimaProcessor build() {
            return new RegSarimaProcessor(this.mappingProvider, this.min, this.eps, this.feps, this.ml, this.start, this.cdf, this.mt);
        }
    }

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
    private final ISsqFunctionMinimizer min;

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public StartingPoint getStartingPoint() {
        return start;
    }

    public RegSarimaProcessor(Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider, ISsqFunctionMinimizer min,
            final double eps, final double feps, final boolean ml, final StartingPoint start, final boolean cdf, final boolean mt) {
        if (mappingProvider == null) {
            this.mappingProvider = m -> SarimaMapping.stationary(m.specification());
        } else {
            this.mappingProvider = mappingProvider;
        }
        this.min = min;
        this.eps = eps;
        this.feps = feps;
        this.ml = ml;
        this.mt = mt;
        this.cdf = cdf;
        this.start = start;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        if (mapping.getDim() == 0) {
            return new RegArimaEstimation(regs, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs), 0);
        }
        SarimaModel mstart;
        if (this.start == StartingPoint.HannanRissanen || this.start == StartingPoint.Multiple) {
            HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                    .stabilize(true)
                    .useDefaultIfFailed(true).build();
            RegArmaModel<SarimaModel> dregs = regs.differencedModel();

            SarimaModel starthr = initializer.initialize(dregs);
            SarimaSpecification spec = starthr.specification();
            mstart = starthr;
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
                    startdef = nstart.adjustOrders(false).build();
                }

                RegArimaEstimation<SarimaModel> mdef = estimate(regs, startdef);
                if (mdef != null) {
                    if (mhr.getConcentratedLikelihood().logLikelihood() < mdef.getConcentratedLikelihood().logLikelihood()) {
                        mstart = (SarimaModel) mdef.getModel().arima().stationaryTransformation().getStationaryModel();
                    } else {
                        mstart = (SarimaModel) mhr.getModel().arima().stationaryTransformation().getStationaryModel();
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
        return optimize(regs, mstart);
    }

    public double getPreliminaryPrecision() {
        return feps;
    }

    @Override
    public double getPrecision() {
        return eps;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        SarimaModel arima = regs.arima();
        DataBlock p = DataBlock.of(mapping.map(arima));
        if (mapping.validate(p) == ParamValidation.Changed) {
            arima = mapping.map(p);
        }
        return optimize(regs, (SarimaModel) arima.stationaryTransformation().getStationaryModel());
    }

    private RegArimaEstimation<SarimaModel>  optimize(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        if (mapping.getDim() == 0) {
            return new RegArimaEstimation<>(regs, 
                    ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs)
                    , 0);
        }
        RegArimaEstimation<SarimaModel> rslt = optimize(regs, start.parameters(), eps);
        if (rslt != null) {
            return finalProcessing(rslt);
        } else {
            return null;
        }
    }

    private RegArimaEstimation<SarimaModel> estimate(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        IParametricMapping<SarimaModel> mapping = this.mappingProvider.apply(regs.arima());
        if (mapping.getDim() == 0) {
            return new RegArimaEstimation<>(regs, 
                    ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs)
                    , 0);
        }
        return optimize(regs, start.parameters(), feps);
    }

    private RegArimaEstimation<SarimaModel> finalProcessing(RegArimaEstimation<SarimaModel> estimation) {
        RegArimaEstimation<SarimaModel> nestimation = tryUrpCancelling(estimation);
        RegArimaEstimation<SarimaModel> festimation = tryUrmCancelling(nestimation);
        return festimation;
    }

    private RegArimaEstimation<SarimaModel> tryUrpCancelling(RegArimaEstimation<SarimaModel> estimation) {
        SarimaModel arima = estimation.getModel().arima();
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
        int qstart = pend + spec.getBp(), qend = qstart + spec.getQ();
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
            RegArimaEstimation<SarimaModel> nrslts = optimize(estimation.getModel(), parameters, eps);
            if (nrslts.getConcentratedLikelihood().logLikelihood() > estimation.getConcentratedLikelihood().logLikelihood()) {
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }

    private RegArimaEstimation<SarimaModel> tryUrmCancelling(RegArimaEstimation<SarimaModel> estimation) {
        SarimaModel arima = estimation.getModel().arima();
        SarimaSpecification spec = arima.specification();
        if (spec.getP() == 0 || spec.getQ() == 0 || spec.getBd() == 0) {
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
        int qstart = pend + spec.getBp(), qend = qstart + spec.getQ();
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
            RegArimaEstimation<SarimaModel> nestimation = optimize(estimation.getModel(), parameters, eps);
            if (nestimation.getConcentratedLikelihood().logLikelihood() > estimation.getConcentratedLikelihood().logLikelihood()) {
                return nestimation;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }
    private static final double UR_LIMIT = .1;

    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, DoubleSequence start, double prec) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
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
        return new RegArimaEstimation(nmodel, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel)
                , new LogLikelihoodFunction.Point(llFunction(regs), rslt.getParameters(), rslt.getGradient(), rslt.getHessian()));
    }
    
    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihood> llFunction(RegArimaModel<SarimaModel> regs){
        IParametricMapping<SarimaModel> mapping = mappingProvider.apply(regs.arima());
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping=new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihood> fn=model->ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }
    
}
