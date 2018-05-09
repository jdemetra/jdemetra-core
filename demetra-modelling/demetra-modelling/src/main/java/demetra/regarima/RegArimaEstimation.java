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

import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.arima.internal.FastKalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.Likelihood;
import demetra.likelihood.LikelihoodStatistics;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.functions.IParametricMapping;
import demetra.sarima.SarimaModel;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

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
    ConcentratedLikelihood concentratedLikelihood;

    /**
     *
     */
    LogLikelihoodFunction.Point<RegArimaModel<M>, ConcentratedLikelihood> max;
    
    int nparams;
    
    public RegArimaEstimation(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihood concentratedLikelihood,
            @Nonnull LogLikelihoodFunction.Point<RegArimaModel<M>, ConcentratedLikelihood> max) {
        this.model = model;
        this.concentratedLikelihood = concentratedLikelihood;
        this.max = max;
        this.nparams = max.getParameters().length;
    }
    
    public RegArimaEstimation(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihood concentratedLikelihood,
            int nparams) {
        this.model = model;
        this.concentratedLikelihood = concentratedLikelihood;
        this.max = null;
        this.nparams = nparams;
    }

    /**
     *
     * @param adj Adjustment factor, defined by the possible transformation of the data
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
     * Data corrected for regression effects (except mean effect)
     *
     * @return
     */
    public DoubleSequence linearizedData() {
        double[] res = model.getY().toArray();

        // handle missing values
        int[] missing = model.missing();
        if (missing.length > 0) {
            DoubleSequence missingEstimates = concentratedLikelihood.missingEstimates();
            for (int i = 0; i < missing.length; ++i) {
                res[missing[i]] -= missingEstimates.get(i);
            }
        }
        DoubleSequence b = concentratedLikelihood.coefficients();
        DataBlock e = DataBlock.ofInternal(res);
        if (b.length() > 0) {
            List<DoubleSequence> x = model.getX();
            int cur = model.isMean() ? 1 : 0;
            for (int i = 0; i < x.size(); ++i) {
                double bcur = b.get(cur++);
                e.apply(x.get(i), (u, v) -> u - bcur * v);
            }
        }
        return e;
    }

    /**
     *
     * @return
     */
    public DoubleSequence olsResiduals() {
        LinearModel lm = model.differencedModel().asLinearModel();
        Ols ols = new Ols();
        LeastSquaresResults lsr = ols.compute(lm);
        return lm.calcResiduals(lsr.getCoefficients());
    }
    
    public DoubleSequence fullResiduals() {
        // compute the residuals...
        if (model.getVariablesCount() == 0) {
            return concentratedLikelihood.e();
        }
        
        DoubleSequence ld = linearizedData();
        StationaryTransformation st = model.arima().stationaryTransformation();
        DataBlock dld;
        
        if (st.getUnitRoots().getDegree() == 0) {
            dld = DataBlock.of(ld);
            if (model.isMean()) {
                dld.sub(concentratedLikelihood.coefficients().get(0));
            }
        } else {
            dld = DataBlock.make(ld.length() - st.getUnitRoots().getDegree());
        }
        st.getUnitRoots().apply(ld, dld);
        
        FastKalmanFilter kf = new FastKalmanFilter((IArimaModel) st.getStationaryModel());
        Likelihood ll = kf.process(dld);
        return ll.e();
        
    }

    /**
     * Returns the concentrated log likelihood function associated with a regarima model,
     * taking into account the mapping between the parameter set and the underlying
     * parametric ARIMA model
     *
     * @param <M>
     * @param mappingProvider
     * @param regs
     * @return
     */
    public static <M extends IArimaModel> LogLikelihoodFunction<RegArimaModel<SarimaModel>, ConcentratedLikelihood>
            concentratedLogLikelihoodFunction(IArimaMapping<M> mapping, RegArimaModel<M> regs) {
        RegArimaMapping<M> rmapping = new RegArimaMapping<>(mapping, regs);
        Function<RegArimaModel<M>, ConcentratedLikelihood> fn = model -> ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model);
        return new LogLikelihoodFunction(rmapping, fn);
    }
    
    public static <M extends IArimaModel> RegArimaEstimation<M> of(RegArimaModel<M> model, int nparams) {
        return new RegArimaEstimation<>(model, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(model), nparams);
    }
    
}
