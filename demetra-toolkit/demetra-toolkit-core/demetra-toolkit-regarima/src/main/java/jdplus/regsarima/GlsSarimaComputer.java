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
import nbbrd.design.BuilderPattern;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.internal.RegArmaEstimation;
import jdplus.regarima.RegArmaModel;
import jdplus.regarima.internal.RegArmaProcessor;
import nbbrd.design.Development;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.RegArimaMapping;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import java.util.function.Function;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.sarima.SarimaModel;
import jdplus.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsSarimaComputer implements IRegArimaComputer<SarimaModel> {
    
    public static final GlsSarimaComputer PROCESSOR=new Builder()
            .precision(1e-7)
            .build(); 
    
    public static final double DEF_EPS = 1e-7;
    
    @BuilderPattern(GlsSarimaComputer.class)
    public static class Builder {
        
        private IArmaInitializer initializer;
        private double eps = DEF_EPS;
        private SsqFunctionMinimizer.Builder min;
        private boolean ml = true, mt = false, fast = true;
        
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
        
        public GlsSarimaComputer build() {
            SsqFunctionMinimizer.Builder builder
                    = min == null ? LevenbergMarquardtMinimizer.builder() : min;
            
            return new GlsSarimaComputer(
                    initializer == null ? IArmaInitializer.defaultInitializer() : initializer,
                    builder.functionPrecision(eps).build(),
                    ml, mt, fast);
        }
        
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private final IArmaInitializer initializer;
    private final SsqFunctionMinimizer min;
    private final boolean ml, mt, fast;

    /**
     *
     */
    private GlsSarimaComputer(final IArmaInitializer initializer, final SsqFunctionMinimizer min,
            final boolean ml, final boolean mt, final boolean fast) {
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
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        SarimaModel start = initializer.initialize(dmodel);
        // not used for the time being
        return optimize(regs, start, mapping);
    }
    
    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        return optimize(regs, null, mapping);
    }
    
    public LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> llFunction(RegArimaModel<SarimaModel> regs, IArimaMapping<SarimaModel> mapping) {
        IParametricMapping<RegArimaModel<SarimaModel>> rmapping = mapping == null
                ? new RegArimaMapping<>(SarimaMapping.of(regs.arima().orders()), regs)
                : new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }
    
    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel ststart, IArimaMapping<SarimaModel> mapping) {
        RegArmaModel<SarimaModel> dmodel = regs.differencedModel();
        if (ststart == null) {
            ststart = dmodel.getArma();
        }
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        IArimaMapping<SarimaModel> stationaryMapping = mapping == null ? SarimaMapping.of(ststart.orders()) : mapping.stationaryMapping();
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, stationaryMapping.parametersOf(ststart), stationaryMapping, min, ndf);
        
        SarimaModel arima = SarimaModel.builder(regs.arima().orders())
                .parameters(rslt.getParameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);
        
        return RegArimaEstimation.<SarimaModel>builder()
                .model(nmodel)
                .concentratedLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel))
                .max(new LogLikelihoodFunction.Point(llFunction(regs, mapping), rslt.getParameters(), rslt.getScore(), rslt.getInformation()))
                .build();
    }
    
}
