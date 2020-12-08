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

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.internal.RegArmaEstimation;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.internal.RegArmaProcessor;
import jdplus.data.DataBlock;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.regarima.RegArimaMapping;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import java.util.function.Function;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegSarimaProcessor implements IRegArimaProcessor<SarimaModel> {

    public static final RegSarimaProcessor PROCESSOR = new Builder()
            .precision(1e-12)
            .startingPoint(StartingPoint.Multiple)
            .build();

    @BuilderPattern(RegSarimaProcessor.class)
    public static class Builder {

        private double eps = DEF_EPS, feps = DEF_INTERNAL_EPS;
        private boolean ml = true, mt = false, cdf = true, fast = true;
        private StartingPoint start = StartingPoint.HannanRissanen;
        private SsqFunctionMinimizer.Builder min;

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
            return new RegSarimaProcessor(min == null ? LevenbergMarquardtMinimizer.builder() : min,
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


    public static final double DEF_EPS = 1e-7, DEF_INTERNAL_EPS = 1e-4;
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

    public RegSarimaProcessor(SsqFunctionMinimizer.Builder min, final double eps, final double feps,
            final boolean ml, final StartingPoint start, final boolean cdf, final boolean mt, final boolean fast) {
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
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        SarimaModel current = regs.arima();
        if (mapping == null) {
            mapping = SarimaMapping.of(current.orders());
        }
        SarimaOrders curSpec = current.orders();
        if (curSpec.getParametersCount() == 0 || (mapping != null && mapping.getDim() == 0)) {
            return RegArimaEstimation.<SarimaModel>builder()
                    .model(regs)
                    .concentratedLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs))
                    .build();
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
                SarimaOrders spec = starthr.orders();
                mstart = starthr;
                if (this.start == StartingPoint.Multiple) {
                    RegArimaEstimation<SarimaModel> mhr = estimate(regs, mapping, starthr, feps);
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

                    RegArimaEstimation<SarimaModel> mdef = estimate(regs, mapping, startdef, feps);
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
                mstart = SarimaModel.builder(regs.arima().orders().doStationary())
                        .setDefault().build();
                break;
            default:
                mstart = SarimaModel.builder(regs.arima().orders().doStationary())
                        .setDefault(0, 0).build();
                break;
        }
        return estimate(regs, mapping, mstart, eps);
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        SarimaModel arima = regs.arima();
        if (mapping == null) {
            mapping = SarimaMapping.of(arima.orders());
        }
        return estimate(regs, mapping, (SarimaModel) arima.stationaryTransformation().getStationaryModel(), eps);
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
    private RegArimaEstimation<SarimaModel> estimate(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping, SarimaModel start, double precision) {
        SarimaOrders curSpec = regs.arima().orders();
        if (curSpec.getParametersCount() == 0 || mapping.getDim() == 0) {
            return RegArimaEstimation.<SarimaModel>builder()
                    .model(regs)
                    .concentratedLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs))
                    .build();
        }
        return optimize(regs, mapping, start, precision, true);
    }

    private RegArimaEstimation<SarimaModel> finalProcessing(RegArimaEstimation<SarimaModel> estimation, IArimaMapping<SarimaModel> mapping) {
        RegArimaEstimation<SarimaModel> nestimation = tryUrpCancelling(estimation, mapping);
        RegArimaEstimation<SarimaModel> festimation = tryUrmCancelling(nestimation, mapping);
        return festimation;
    }

    private RegArimaEstimation<SarimaModel> tryUrpCancelling(RegArimaEstimation<SarimaModel> estimation, IArimaMapping<SarimaModel> mapping) {
        SarimaModel arima = estimation.getModel().arima();
        SarimaOrders spec = arima.orders();
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
            SarimaModel narima = SarimaModel.builder(SarimaOrders.stationary(spec))
                    .parameters(parameters)
                    .build();
            RegArimaEstimation<SarimaModel> nrslts = optimize(estimation.getModel(), mapping, narima, eps, false);
            if (nrslts.getConcentratedLikelihood().logLikelihood() > estimation.getConcentratedLikelihood().logLikelihood()) {
                return nrslts;
            } else {
                return estimation;
            }
        } catch (Exception err) {
            return estimation;
        }
    }

    private RegArimaEstimation<SarimaModel> tryUrmCancelling(RegArimaEstimation<SarimaModel> estimation, IArimaMapping<SarimaModel> mapping) {
        SarimaModel arima = estimation.getModel().arima();
        SarimaOrders spec = arima.orders();
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
            SarimaModel narima = SarimaModel.builder(SarimaOrders.stationary(spec))
                    .parameters(parameters)
                    .build();
            RegArimaEstimation<SarimaModel> nrslts = optimize(estimation.getModel(), mapping, narima, eps, false);
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
     * @param mapping Mapping to the non-stationary SarimaModel model
     * @param ststart The starting model must be stationary !
     * @param prec
     * @return
     */
    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping, SarimaModel ststart, double precision, boolean finalProcessing) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        IArimaMapping<SarimaModel> stationaryMapping = mapping.stationaryMapping();
        DataBlock p = DataBlock.of(stationaryMapping.parametersOf(ststart));
        stationaryMapping.validate(p);

        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();
        if (cdf) {
            ndf -= stationaryMapping.getDim();
        }

        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, p, stationaryMapping, min.functionPrecision(precision).build(), ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().orders())
                .parameters(DoubleSeq.of(rslt.getParameters()))
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);
        RegArimaEstimation finalRslt = RegArimaEstimation.<SarimaModel>builder()
                .model(nmodel)
                .concentratedLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel))
                .max(new LogLikelihoodFunction.Point(llFunction(regs, mapping), rslt.getParameters(), rslt.getScore(), rslt.getInformation()))
                .build();
        return finalProcessing ? finalProcessing(finalRslt, mapping) : finalRslt;
    }

    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> llFunction(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping = new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }

}
