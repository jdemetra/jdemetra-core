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
package demetra.regarima.internal;

import demetra.regarima.internal.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.design.Immutable;
import demetra.likelihood.ConcentratedLikelihood;

/**
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class ConcentratedLikelihoodEstimation<M extends IArimaModel> {

    static class Builder<M extends IArimaModel> implements IBuilder<ConcentratedLikelihoodEstimation<M>> {

        private RegArmaModel<M> dmodel;
        private ConcentratedLikelihood cll;
        private DoubleSequence el, bmissing, vmissing;

        public Builder<M> differencedModel(final RegArmaModel<M> dmodel) {
            this.dmodel = dmodel;
            return this;
        }

        public Builder<M> concentratedLogLikelihood(final ConcentratedLikelihood cll) {
            this.cll = cll;
            return this;
        }

        public Builder<M> residuals(final DoubleSequence res) {
            this.el = res;
            return this;
        }

        public Builder<M> missingValues(final DoubleSequence ao, final DoubleSequence aoUnscaledVariance) {
            if (ao.length() != aoUnscaledVariance.length()) {
                throw new IllegalArgumentException();
            }
            this.bmissing = ao;
            this.vmissing = aoUnscaledVariance;
            return this;
        }

        @Override
        public ConcentratedLikelihoodEstimation<M> build() {
            if (bmissing != null) {
                return new ConcentratedLikelihoodEstimation<>(dmodel, cll, el, bmissing, vmissing);
            } else {
                return new ConcentratedLikelihoodEstimation<>(dmodel, cll, el, DoubleSequence.EMPTY, DoubleSequence.EMPTY);
            }
        }
    }

    static <M extends IArimaModel> Builder<M> builder() {
        return new Builder<>();
    }

    private final RegArmaModel<M> dmodel;
    private final ConcentratedLikelihood cll;
    private final DoubleSequence el, bmissing, vmissing;

    public ConcentratedLikelihoodEstimation(final RegArmaModel<M> dmodel, final ConcentratedLikelihood cll,
            final DoubleSequence el, final DoubleSequence bmissing, final DoubleSequence vmissing) {
        this.dmodel = dmodel;
        this.cll = cll;
        this.el = el;
        this.bmissing = bmissing;
        this.vmissing = vmissing;
    }

    /**
     *
     * @return
     */
    public ConcentratedLikelihood getLikelihood() {
        return cll;
    }

    /**
     *
     * @return
     */
    public DoubleSequence getResiduals() {
        return el;
    }

    public DoubleSequence getMissingEstimates() {
        return bmissing;
    }

    public DoubleSequence getMissingEstimatesUnscaledVariance() {
        return vmissing;
    }

    public RegArmaModel getDifferencedModel() {
        return dmodel;
    }

    
}
