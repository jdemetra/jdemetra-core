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
package jdplus.regsarima;

import demetra.arima.SarimaSpecification;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.internal.RegArmaEstimation;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.internal.RegArmaProcessor;
import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IParametricMapping;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.regarima.RegArimaMapping;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import java.util.function.Function;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.maths.functions.ssq.SsqFunctionMinimizer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegSarimaProcessor implements IRegArimaProcessor<SarimaModel> {

    @BuilderPattern(RegSarimaProcessor.class)
    public static class Builder {

        private IArimaMapping<SarimaModel> mapping;
        private double eps = DEF_EPS, feps = DEF_INTERNAL_EPS;
        private boolean ml = true, mt = false, cdf = true, fast = true;
        private StartingPoint start = StartingPoint.HannanRissanen;
        private SsqFunctionMinimizer.Builder min;

        public Builder mapping(IArimaMapping<SarimaModel> mapping) {
            this.mapping = mapping;
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

        public Builder computeExactFinalDerivatives(boolean exact) {
            this.fast = !exact;
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

        public Builder minimizer(SsqFunctionMinimizer.Builder min) {
            this.min = min;
            return this;
        }

        public RegSarimaProcessor build() {
            return new RegSarimaProcessor(mapping, 
                    min == null ?  LevenbergMarquardtMinimizer.builder() : min, 
                    eps, feps, ml, start, cdf, mt, fast);
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

    public static final RegSarimaProcessor DEFAULT = builder().build();

    public static final double DEF_EPS = 1e-7, DEF_INTERNAL_EPS = 1e-4;
    private final IArimaMapping<SarimaModel> mapping;
    private final double eps, feps;
    private final boolean ml, mt, cdf, fast;
    private final StartingPoint start;
    private final SsqFunctionMinimizer.Builder min;

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public StartingPoint getStartingPoint() {
        return start;
    }

    public RegSarimaProcessor(IArimaMapping<SarimaModel> mapping, SsqFunctionMinimizer.Builder min,
            final double eps, final double feps, final boolean ml, final StartingPoint start, final boolean cdf, final boolean mt, final boolean fast) {
        this.mapping = mapping;
        this.min = min;
        this.eps = eps;
        this.feps = feps;
        this.ml = ml;
        this.mt = mt;
        this.cdf = cdf;
        this.start = start;
        this.fast = fast;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {
        SarimaModel current = regs.arima();
        SarimaSpecification curSpec = current.specification();
        if (curSpec.getParametersCount() == 0 || (mapping != null && mapping.getDim() == 0)) {
            return new RegArimaEstimation(regs, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs), 0);
        }
        SarimaModel mstart;
        switch (start) {
            case HannanRissanen:
            case Multiple:
                HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                        .stabilize(true)
                        .useDefaultIfFailed(true).build();
                RegArmaModel<SarimaModel> dregs = regs.differencedModel();
                SarimaModel starthr = initializer.initialize(dregs);
                SarimaSpecification spec = starthr.specification();
                mstart = starthr;
                if (this.start == StartingPoint.Multiple) {
                    RegArimaEstimation<SarimaModel> mhr = estimate(regs, starthr, feps);
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

                    RegArimaEstimation<SarimaModel> mdef = estimate(regs, startdef, feps);
                    if (mdef != null) {
                        if (mhr.getConcentratedLikelihood().logLikelihood() < mdef.getConcentratedLikelihood().logLikelihood()) {
                            mstart = (SarimaModel) mdef.getModel().arima().stationaryTransformation().getStationaryModel();
                        } else {
                            mstart = (SarimaModel) mhr.getModel().arima().stationaryTransformation().getStationaryModel();
                        }
                    }
                }
                break;
            case Default:
                mstart = SarimaModel.builder(regs.arima().specification().doStationary())
                        .setDefault().build();
                break;
            default:
                mstart = SarimaModel.builder(regs.arima().specification().doStationary())
                        .setDefault(0, 0).build();
                break;
        }
        return estimate(regs, mstart, eps);
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        SarimaModel arima = regs.arima();
        return estimate(regs, (SarimaModel) arima.stationaryTransformation().getStationaryModel(), eps);
    }

//    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel start, IArimaMapping<SarimaModel> curMapping) {
//        if (curMapping.getDim() == 0) {
//            return new RegArimaEstimation<>(regs,
//                    ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs), 0);
//        }
//        RegArimaEstimation<SarimaModel> rslt = optimize(regs, curMapping.map(start), curMapping, eps);
//        if (rslt != null) {
//            return finalProcessing(rslt, curMapping);
//        } else {
//            return null;
//        }
//    }
    /**
     *
     * @param regs
     * @param start Must be a stationary model !!
     * @return
     */
    private RegArimaEstimation<SarimaModel> estimate(RegArimaModel<SarimaModel> regs, SarimaModel start, double precision) {
        SarimaSpecification curSpec = regs.arima().specification();
        if (curSpec.getParametersCount() == 0 || (mapping != null && mapping.getDim() == 0)) {
            return new RegArimaEstimation<>(regs,
                    ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs), 0);
        }
        return optimize(regs, start, precision, true);
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
            SarimaModel narima = SarimaModel.builder(SarimaSpecification.stationary(spec))
                    .parameters(parameters)
                    .build();
            RegArimaEstimation<SarimaModel> nrslts = optimize(estimation.getModel(), narima, eps, false);
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
            SarimaModel narima = SarimaModel.builder(SarimaSpecification.stationary(spec))
                    .parameters(parameters)
                    .build();
            RegArimaEstimation<SarimaModel> nrslts = optimize(estimation.getModel(), narima, eps, false);
            if (nrslts.getConcentratedLikelihood().logLikelihood() > estimation.getConcentratedLikelihood().logLikelihood()) {
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }
    private static final double UR_LIMIT = .1;

    /**
     *
     * @param regs
     * @param start
     * @param curMapping
     * @param prec
     * @return
     */
    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel start, double precision, boolean finalProcessing) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        IArimaMapping<SarimaModel> stationaryMapping = stationaryMapping(regs);
        DataBlock p = DataBlock.of(stationaryMapping.parametersOf(start));
        stationaryMapping.validate(p);

        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();
        if (cdf) {
            ndf -= stationaryMapping.getDim();
        }
        
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, p, stationaryMapping, min.functionPrecision(precision).build(), ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().specification())
                .parameters(rslt.getModel().getArma().parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);
        RegArimaEstimation finalRslt = new RegArimaEstimation(nmodel, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel), new LogLikelihoodFunction.Point(llFunction(regs), rslt.getParameters(), rslt.getGradient(), rslt.getHessian()));
        return finalProcessing ? finalProcessing(finalRslt) : finalRslt;
    }

    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> llFunction(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping = new RegArimaMapping<>(stationaryMapping(regs), regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }

    private IArimaMapping<SarimaModel> stationaryMapping(RegArimaModel<SarimaModel> regs) {
        return mapping == null ? SarimaMapping.ofStationary(regs.arima().specification()) : mapping.stationaryMapping();
    }
}
