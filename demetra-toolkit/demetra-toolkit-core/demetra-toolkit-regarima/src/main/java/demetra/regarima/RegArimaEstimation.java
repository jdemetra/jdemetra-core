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
package demetra.regarima;

import demetra.arima.estimation.IArimaMapping;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.arima.IArimaModel;
import demetra.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.sarima.SarimaModel;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import demetra.data.DoubleSeq;

/**
 *
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class RegArimaEstimation<M extends IArimaModel> {

    /**
     * Estimated model
     */
    @lombok.NonNull
    RegArimaModel<M> model;

    /**
     * Concentrated likelihood
     */
    @lombok.NonNull
    ConcentratedLikelihoodWithMissing concentratedLikelihood;

    /**
     *
     */
    LogLikelihoodFunction.Point<RegArimaModel<M>, ConcentratedLikelihoodWithMissing> max;

    int nparams;

    public RegArimaEstimation(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihoodWithMissing concentratedLikelihood,
            @Nonnull LogLikelihoodFunction.Point<RegArimaModel<M>, ConcentratedLikelihoodWithMissing> max) {
        this.model = model;
        this.concentratedLikelihood = concentratedLikelihood;
        this.max = max;
        this.nparams = max.getParameters().length;
    }

    public RegArimaEstimation(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihoodWithMissing concentratedLikelihood,
            int nparams) {
        this.model = model;
        this.concentratedLikelihood = concentratedLikelihood;
        this.max = null;
        this.nparams = nparams;
    }

    /**
     *
     * @param adj Adjustment factor, defined by the possible transformation of
     * the data
     * @return
     */
    public LikelihoodStatistics statistics(double adj) {
        return LikelihoodStatistics.statistics(concentratedLikelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(adj)
                .differencingOrder(model.arima().getNonStationaryAROrder())
                .parametersCount(nparams + model.getVariablesCount() + 1)
                .ssq(concentratedLikelihood.ssq())
                .build();

    }

    /**
     * Returns the concentrated log likelihood function associated with a
     * regarima model, taking into account the mapping between the parameter set
     * and the underlying parametric ARIMA model
     *
     * @param <M>
     * @param mappingProvider
     * @param regs
     * @return
     */
    public static <M extends IArimaModel> LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing>
            concentratedLogLikelihoodFunction(IArimaMapping<M> mapping, RegArimaModel<M> regs) {
        RegArimaMapping<M> rmapping = new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<M>, ConcentratedLikelihoodWithMissing> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }

    public static <M extends IArimaModel> RegArimaEstimation<M> of(RegArimaModel<M> model, int nparams) {
        return new RegArimaEstimation<>(model, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model), nparams);
    }

    public DoubleSeq interpolatedSeries() {
        DoubleSeq y = model.getY();
        int[] missing = model.missing();
        if (missing.length == 0) {
            return y;
        } else {
            double[] dy = y.toArray();
            DoubleSeq missingEstimates = concentratedLikelihood.missingEstimates();
            DoubleSeqCursor reader = missingEstimates.cursor();
            for (int i = 0; i < missing.length; ++i) {
                dy[missing[i]] = reader.getAndNext();
            }
            return DoubleSeq.of(dy);
        }
    }

    public DoubleSeq linearizedSeries() {
        DoubleSeq y = interpolatedSeries();
        List<DoubleSeq> x = model.getX();
        if (x.isEmpty()) {
            return y;
        }
        DoubleSeq coefficients = concentratedLikelihood.coefficients();
        DoubleSeqCursor reader = coefficients.cursor();
        DataBlock ylin = DataBlock.of(y);
        if (model.isMean()) {
            reader.getAndNext();
        }
        for (DoubleSeq cur : x) {
            ylin.addAY(-reader.getAndNext(), DataBlock.of(cur));
        }
        return ylin.unmodifiable();
    }
}
