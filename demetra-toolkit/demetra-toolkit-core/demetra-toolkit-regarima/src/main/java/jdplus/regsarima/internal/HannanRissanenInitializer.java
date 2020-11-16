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
package jdplus.regsarima.internal;

import jdplus.regarima.RegArmaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.arima.SarmaOrders;
import jdplus.sarima.estimation.HannanRissanen;
import jdplus.regsarima.IArmaInitializer;
import jdplus.sarima.estimation.SarimaMapping;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class HannanRissanenInitializer implements IArmaInitializer {

    @BuilderPattern(HannanRissanenInitializer.class)
    public static class Builder {

        private boolean usedefault, stabilize, failifunstable;

        /**
         * Returns a default model if HR failed
         * @param usedefault
         * @return 
         */
        public Builder useDefaultIfFailed(boolean usedefault) {
            this.usedefault = usedefault;
            return this;
        }

        /**
         * Stabilizes the estimated model (roots >1)
         * @param stabilize
         * @return 
         */
        public Builder stabilize(boolean stabilize) {
            this.stabilize = stabilize;
            return this;
        }

        /**
         * Fails if the estimated model is unstable (the stabilize option is automatically activated)
         * @param failifunstable
         * @return 
         */
        public Builder failIfUnstable(boolean failifunstable) {
            this.failifunstable = failifunstable;
            if (failifunstable)
                stabilize=true;
            return this;
        }

        public HannanRissanenInitializer build() {
            return new HannanRissanenInitializer(stabilize, usedefault, failifunstable);
        }
    }

    private static final double EPS = 1e-9;

    private final boolean usedefault, stabilize, failifunstable;
    private DoubleSeq dy_;

    public boolean isStabilizing() {
        return stabilize;
    }

    public boolean isUsingDefaultIfFailed() {
        return usedefault;
    }
    
    public static Builder builder(){
        return new Builder();
    }

    private HannanRissanenInitializer(boolean stabilize, boolean usedefault, boolean failifunstable) {
        this.stabilize = stabilize;
        this.usedefault = usedefault;
        this.failifunstable = failifunstable;
    }

    /**
     * Initialize the parameters of a given RegArima model. The initialization
     * procedure is the following. If the regression model contains variables,
     * an initial set of residuals is computed by ols. If the ols routine fails,
     * null is returned.
     *
     * @param regs The initial model
     * @return The seasonal stationary arma model that contains the initial
     * parameters
     */
    @Override
    public SarimaModel initialize(RegArmaModel<SarimaModel> regs) {
        SarimaModel sarima = regs.getArma();
        SarimaOrders spec = sarima.orders();
        SarmaOrders dspec = spec.doStationary();
        try {
            if (spec.getParametersCount() == 0) {
                return SarimaModel.builder(dspec).build();
            }
            dy_ = null;
            LinearModel lm = regs.asLinearModel();
            HannanRissanen hr = HannanRissanen.builder().build();
            if (lm.getVariablesCount() > 0) {
                LeastSquaresResults lsr = Ols.compute(lm);
                dy_ = lm.calcResiduals(lsr.getCoefficients());
            } else {
                dy_ = lm.getY();
            }
            if (Math.sqrt(dy_.ssq() / dy_.length()) < EPS) {
                return SarimaModel.builder(spec).setDefault(0, 0).build();
            }

            if (!hr.process(dy_, dspec)) {
                if (usedefault) {
                    return SarimaModel.builder(spec).setDefault().build();
                } else {
                    return null;
                }
            }
            SarimaModel m = hr.getModel();
            if (!stabilize) {
                return m;
            }
            SarimaModel nm = SarimaMapping.stabilize(m);
            if (nm != m && failifunstable) {
                return null;
            }
            return nm;

        } catch (Exception ex) {
            if (usedefault) {
                return SarimaModel.builder(spec).setDefault().build();
            } else {
                return null;
            }
        }
    }
}
