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
package demetra.regarima.ami;

import demetra.data.DoubleSequence;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaModel;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.internal.HannanRissanenInitializer;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegArimaUtility {

    public IRegArimaProcessor processor(boolean ml, double precision) {
        ISsqFunctionMinimizer minimizer = new LevenbergMarquardtMinimizer();
        HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                .stabilize(true)
                .useDefaultIfFailed(true)
                .build();

        return GlsSarimaProcessor.builder()
                .initializer(initializer)
                .useMaximumLikelihood(ml)
                .minimizer(minimizer)
                .precision(precision)
                .build();
    }

    public RegArimaModel<SarimaModel> airlineModel(DoubleSequence data, boolean mean, int ifreq, boolean seas) {
        // use airline model with mean
        SarimaSpecification spec = new SarimaSpecification();
        spec.setPeriod(ifreq);
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
