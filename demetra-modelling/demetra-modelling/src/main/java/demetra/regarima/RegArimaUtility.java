/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.regarima;

import demetra.arima.IArimaModel;
import demetra.arima.StationaryTransformation;
import demetra.arima.internal.FastKalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.Likelihood;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.internal.HannanRissanenInitializer;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegArimaUtility {

    
    /**
     * Data corrected for regression effects (except mean effect)
     *
     * @param <M>
     * @param model
     * @param concentratedLikelihood
     * @return
     */
    public <M extends IArimaModel>DoubleSequence linearizedData(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihood concentratedLikelihood) {
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
     * @param <M>
     * @param model
     * @return
     */
    public <M extends IArimaModel> DoubleSequence olsResiduals(@Nonnull RegArimaModel<M> model) {
        LinearModel lm = model.differencedModel().asLinearModel();
        Ols ols = new Ols();
        LeastSquaresResults lsr = ols.compute(lm);
        return lm.calcResiduals(lsr.getCoefficients());
    }
    
    /**
     * 
     * @param <M>
     * @param model
     * @param concentratedLikelihood
     * @return 
     */
    public <M extends IArimaModel> DoubleSequence fullResiduals(@Nonnull RegArimaModel<M> model, @Nonnull ConcentratedLikelihood concentratedLikelihood) {
        // compute the residuals...
        if (model.getVariablesCount() == 0) {
            return concentratedLikelihood.e();
        }
        
        DoubleSequence ld = linearizedData(model, concentratedLikelihood);
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


    public IRegArimaProcessor<SarimaModel> processor(IArimaMapping<SarimaModel> mapping, boolean ml, double precision) {
        HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                .stabilize(true)
                .useDefaultIfFailed(true)
                .build();

        return GlsSarimaProcessor.builder()
                .mapping(mapping)
                .initializer(initializer)
                .useMaximumLikelihood(ml)
                .precision(precision)
                .build();
    }
    
    public RegArimaModel<SarimaModel> airlineModel(DoubleSequence data, boolean mean, int ifreq, boolean seas) {
        // use airline model with mean
        SarimaSpecification spec = new SarimaSpecification(ifreq);
        spec.airline(seas);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        return RegArimaModel.builder(SarimaModel.class)
                .arima(arima)
                .y(data)
                .meanCorrection(mean)
                .build();
    }

    public BackFilter differencingFilter(int freq, int d, int bd) {
        Polynomial X = null;
        if (d > 0) {
            X = UnitRoots.D(1, d);
        }
        if (bd > 0) {
            Polynomial XD = UnitRoots.D(freq, bd);
            if (X == null) {
                X = XD;
            } else {
                X = X.times(XD);
            }
        }
        if (X == null) {
            X = Polynomial.ONE;
        }
        return new BackFilter(X);
    }
}
