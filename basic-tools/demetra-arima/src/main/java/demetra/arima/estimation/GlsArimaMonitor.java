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
package demetra.arima.estimation;

import demetra.sarima.estimation.*;
import demetra.arima.ArimaException;
import demetra.arima.ArimaModel;
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
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GlsArimaMonitor {

    public static class Builder implements IBuilder<GlsArimaMonitor> {

        private IParametricMapping<ArimaModel> mapping;
        private double eps = 1e-9;
        private ISsqFunctionMinimizer min;
        private boolean ml = true, mt = false;

        public Builder mapping(IParametricMapping<ArimaModel> mapping) {
            this.mapping = mapping;
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
        public GlsArimaMonitor build() {
            return new GlsArimaMonitor(mapping, min, eps, ml, mt);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    private final IParametricMapping mapping;
    private final ISsqFunctionMinimizer min;
    private final boolean ml, mt;

    /**
     *
     */
    private GlsArimaMonitor(IParametricMapping<ArimaModel> mapping, final ISsqFunctionMinimizer min, 
            final double eps, final boolean ml, final boolean mt) {
            this.mapping=mapping;
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
    public RegArimaEstimation<ArimaModel> compute(RegArimaModel<ArimaModel> regs) {
        RegArmaModel<ArimaModel> dmodel = RegArmaModel.of(regs);
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        RegArmaEstimation<ArimaModel> rslt = processor.compute(dmodel, mapping.getDefault(), mapping, min, ndf);
        ArimaModel arma = rslt.getModel().getArma();
        ArimaModel arima=new ArimaModel(arma.getAR(), regs.arima().getNonStationaryAR(), arma.getMA(), 1);
        RegArimaModel<ArimaModel> nmodel = RegArimaModel.of(regs, arima);
        return RegArimaEstimation.compute(nmodel);
    }
}
