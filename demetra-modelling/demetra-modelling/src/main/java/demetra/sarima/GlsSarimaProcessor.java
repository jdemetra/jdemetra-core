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

import demetra.design.BuilderPattern;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.internal.RegArmaEstimation;
import demetra.regarima.RegArmaModel;
import demetra.regarima.internal.RegArmaProcessor;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaMapping;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsSarimaProcessor implements IRegArimaProcessor<SarimaModel>{

    @BuilderPattern(GlsSarimaProcessor.class)
    public static class Builder {

        private Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
        private IArmaInitializer initializer;
        private double eps = 1e-9;
        private ISsqFunctionMinimizer min;
        private boolean ml = true, mt = false, fast=true;

        public Builder mapping(Function<SarimaModel, IParametricMapping<SarimaModel>> mapping) {
            this.mappingProvider = mapping;
            return this;
        }

        public Builder initializer(IArmaInitializer initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder minimizer(ISsqFunctionMinimizer min) {
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
            return new GlsSarimaProcessor(mappingProvider, initializer, min, eps, ml, mt, fast);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
    private final IArmaInitializer initializer;
    private final ISsqFunctionMinimizer min;
    private final boolean ml, mt, fast;

    /**
     *
     */
    private GlsSarimaProcessor(Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider,
            final IArmaInitializer initializer, final ISsqFunctionMinimizer min, 
            final double eps, final boolean ml, final boolean mt, final boolean fast) {
        if (mappingProvider == null){
            this.mappingProvider=m->SarimaMapping.of(m.specification());
        }else
            this.mappingProvider=mappingProvider;
        if (initializer == null) {
            this.initializer = IArmaInitializer.defaultInitializer();
        } else {
            this.initializer = initializer;
        }
        if (min == null) {
            this.min = new LevenbergMarquardtMinimizer();
        } else {
            this.min = min;
        }
        this.min.setFunctionPrecision(eps);
        this.ml = ml;
        this.mt = mt;
        this.fast=fast;
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
    
    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihood> llFunction(RegArimaModel<SarimaModel> regs){
        IParametricMapping<SarimaModel> mapping = mappingProvider.apply(regs.arima());
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping=new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihood> fn=model->ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }
    
    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel ststart) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        if (ststart == null)
            ststart=dmodel.getArma();
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        IParametricMapping<SarimaModel> mapping = mappingProvider.apply(ststart);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, mapping.map(ststart), mapping, min, ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().specification())
                .parameters(rslt.getModel().getArma().parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);

        return new RegArimaEstimation(nmodel, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel)
                , new LogLikelihoodFunction.Point(llFunction(regs), rslt.getParameters(), rslt.getGradient(), rslt.getHessian()));
    }
    
    @Override
    public double getPrecision(){
        return min.getFunctionPrecision();
    }
}
