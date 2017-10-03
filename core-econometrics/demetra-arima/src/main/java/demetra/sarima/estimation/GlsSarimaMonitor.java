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

import demetra.arima.ArimaException;
import demetra.arima.regarima.RegArimaEstimation;
import demetra.arima.regarima.RegArimaModel;
import demetra.arima.regarima.internal.RegArmaEstimation;
import demetra.arima.regarima.internal.RegArmaModel;
import demetra.arima.regarima.internal.RegArmaProcessor;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.sarima.SarimaModel;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsSarimaMonitor {

    public static class Builder implements IBuilder<GlsSarimaMonitor> {

        private Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
        private IarmaInitializer initializer;
        private double eps = 1e-9;
        private ISsqFunctionMinimizer min;
        private boolean ml = true, mt = false;

        public Builder mapping(Function<SarimaModel, IParametricMapping<SarimaModel>> mapping) {
            this.mappingProvider = mapping;
            return this;
        }

        public Builder initializer(IarmaInitializer initializer) {
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

        @Override
        public GlsSarimaMonitor build() {
            return new GlsSarimaMonitor(mappingProvider, initializer, min, eps, ml, mt);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider;
    private final IarmaInitializer initializer;
    private final ISsqFunctionMinimizer min;
    private final boolean ml, mt;

    /**
     *
     */
    private GlsSarimaMonitor(Function<SarimaModel, IParametricMapping<SarimaModel>> mappingProvider,
            final IarmaInitializer initializer, final ISsqFunctionMinimizer min, 
            final double eps, final boolean ml, final boolean mt) {
        if (mappingProvider == null){
            this.mappingProvider=m->SarimaMapping.of(m.specification());
        }else
            this.mappingProvider=mappingProvider;
        if (initializer == null) {
            this.initializer = IarmaInitializer.defaultInitializer();
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
    }

    /**
     *
     * @param regs
     * @return
     */
    public RegArimaEstimation<SarimaModel> compute(RegArimaModel<SarimaModel> regs) {
        RegArmaModel<SarimaModel> dmodel = RegArmaModel.of(regs);
        SarimaModel start = initializer.initialize(dmodel);
        // not used for the time being
        return optimize(regs, start.parameters());
    }

    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, DoubleSequence start) {
        RegArmaModel<SarimaModel> dmodel = RegArmaModel.of(regs);
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt);
        IParametricMapping<SarimaModel> mapping = mappingProvider.apply(dmodel.getArma());
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount() - mapping.getDim();
        RegArmaEstimation<SarimaModel> rslt = processor.compute(dmodel, start, mapping, min, ndf);

        SarimaModel arima = SarimaModel.builder(regs.arima().specification())
                .parameters(rslt.getModel().getArma().parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, arima);

        return RegArimaEstimation.compute(nmodel);
    }
}
