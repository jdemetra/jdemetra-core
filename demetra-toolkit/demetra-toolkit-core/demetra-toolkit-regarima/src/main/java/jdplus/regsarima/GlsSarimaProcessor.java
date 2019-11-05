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

import jdplus.sarima.estimation.SarimaMapping;
import demetra.design.BuilderPattern;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.internal.RegArmaEstimation;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.internal.RegArmaProcessor;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IParametricMapping;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaMapping;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import java.util.function.Function;
import jdplus.maths.functions.ssq.SsqFunctionMinimizer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsSarimaProcessor implements IRegArimaProcessor<SarimaModel> {

    public static final double DEF_EPS = 1e-7;

    @BuilderPattern(GlsSarimaProcessor.class)
    public static class Builder {

        private IArimaMapping<SarimaModel> mapping;
        private IArmaInitializer initializer;
        private double eps = DEF_EPS;
        private SsqFunctionMinimizer.Builder min;
        private boolean ml = true, mt = false, fast = true;

        public Builder mapping(IArimaMapping<SarimaModel> mapping) {
            this.mapping = mapping;
            return this;
        }

        public Builder initializer(IArmaInitializer initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder minimizer(SsqFunctionMinimizer.Builder min) {
            this.min = min;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder useMaximumLikelihood(boolean ml) {
            this.ml = ml;
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

        public GlsSarimaProcessor build() {
            SsqFunctionMinimizer.Builder builder= 
                    min == null ? LevenbergMarquardtMinimizer.builder() : min;
            
            return new GlsSarimaProcessor(mapping,
                    initializer == null ? IArmaInitializer.defaultInitializer() : initializer,
                    builder.functionPrecision(eps).build(),
                    ml, mt, fast);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final IArimaMapping<SarimaModel> mapping;
    private final IArmaInitializer initializer;
    private final SsqFunctionMinimizer min;
    private final boolean ml, mt, fast;

    /**
     *
     */
    private GlsSarimaProcessor(IArimaMapping<SarimaModel> mapping,
            final IArmaInitializer initializer, final SsqFunctionMinimizer min,
            final boolean ml, final boolean mt, final boolean fast) {
        this.mapping = mapping;
        this.initializer = initializer;
        this.min = min;
        this.ml = ml;
        this.mt = mt;
        this.fast = fast;
    }

    /**
     *
     * @param regs
     * @return
     */
    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        SarimaModel start = initializer.initialize(dmodel);
        // not used for the time being
        return optimize(regs, start);
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        return optimize(regs, null);
    }

    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> llFunction(RegArimaModel<SarimaModel> regs) {
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping = mapping == null
                ? new RegArimaMapping<>(SarimaMapping.of(regs.arima().specification()), regs)
                : new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }

    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel ststart) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        if (ststart == null) {
            ststart = dmodel.getArma();
        }
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        IArimaMapping<SarimaModel> stationaryMapping = mapping == null ? SarimaMapping.of(ststart.specification()) :mapping.stationaryMapping();
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, stationaryMapping.parametersOf(ststart), stationaryMapping, min, ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().specification())
                .parameters(rslt.getModel().getArma().parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);

        return new RegArimaEstimation(nmodel, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel),
                new LogLikelihoodFunction.Point(llFunction(regs), rslt.getParameters(), rslt.getGradient(), rslt.getHessian()));
    }

}
