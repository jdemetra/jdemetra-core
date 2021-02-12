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
package jdplus.regarima;

import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import nbbrd.design.Development;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.LikelihoodStatistics;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.sarima.SarimaModel;
import java.util.List;
import java.util.function.Function;
import demetra.data.DoubleSeq;

/**
 * RegArimaEstimation. Main results
 * @param <M>
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class RegArimaEstimation<M extends IArimaModel> {

    /**
     * Estimated model
     */
    @lombok.NonNull
    private RegArimaModel<M> model;

    /**
     * Concentrated likelihood
     */
    @lombok.NonNull
    private ConcentratedLikelihoodWithMissing concentratedLikelihood;

    /**
     * Maximum Log-likelihood
     */
    private LogLikelihoodFunction.Point<RegArimaModel<M>, ConcentratedLikelihoodWithMissing> max;

    /**
     * Adjustment of the likelihood when the initial observations have been 
     * transformed before estimation (log...). 0 if unused.
     */
    private double llAdjustment;
    
    public int parametersCount(){
        return max== null ? 0 : max.getParameters().length();
    }


    /**
     * Likelihood statistics
     * @return
     */
    public LikelihoodStatistics statistics() {
        return LikelihoodStatistics.statistics(concentratedLikelihood.logLikelihood(), model.getObservationsCount() - model.getMissingValuesCount())
                .llAdjustment(llAdjustment)
                .differencingOrder(model.arima().getNonStationaryArOrder())
                .parametersCount(parametersCount() + model.getVariablesCount() + 1)
                .ssq(concentratedLikelihood.ssq())
                .build();

    }

    /**
     * Returns the concentrated log likelihood function associated with a
     * regarima model, taking into account the mapping between the parameter set
     * and the underlying parametric ARIMA model
     *
     * @param <M>
     * @param mapping
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
        ConcentratedLikelihoodWithMissing ll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return RegArimaEstimation.<M>builder()
                .model(model)
                .concentratedLikelihood(ll)
                .max(null)
                .build();
    }

    public DoubleSeq interpolatedSeries() {
        DoubleSeq y = model.getY();
        int[] missing = model.missing();
        if (missing.length == 0) {
            return y;
        } else {
            double[] ay = y.toArray();
            DoubleSeq missingEstimates = concentratedLikelihood.missingCorrections();
            DoubleSeqCursor reader = missingEstimates.cursor();
            for (int i = 0; i < missing.length; ++i) {
                ay[missing[i]] = reader.getAndNext();
            }
            return DoubleSeq.of(ay);
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
