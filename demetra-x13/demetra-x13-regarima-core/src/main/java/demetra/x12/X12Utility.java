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
package demetra.x12;

import demetra.design.Development;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.sarima.GlsSarimaProcessor;
import jdplus.sarima.SarimaModel;
import demetra.sarima.internal.HannanRissanenInitializer;
import jdplus.maths.functions.levmar.LevenbergMarquardtMinimizer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class X12Utility {

    public static final double MINCV = 2.8;

    public double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.advancedComputer().applyAsDouble(nobs), MINCV);
    }

    public IRegArimaProcessor<SarimaModel> processor(boolean ml, double precision) {
        HannanRissanenInitializer initializer = HannanRissanenInitializer.builder()
                .stabilize(true)
                .useDefaultIfFailed(true)
                .build();

        return GlsSarimaProcessor.builder()
                .initializer(initializer)
                .useMaximumLikelihood(ml)
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(precision)
                .build();
    }
}
