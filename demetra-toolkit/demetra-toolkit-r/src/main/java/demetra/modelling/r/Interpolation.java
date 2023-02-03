/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.modelling.r;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.data.DataBlockStorage;
import jdplus.data.interpolation.AverageInterpolator;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.sarima.estimation.SarimaMapping;
import jdplus.ssf.arima.SsfArima;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.SsfFunction;
import jdplus.ssf.dk.SsfFunctionPoint;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Interpolation {

    public TsData averageInterpolation(TsData input) {
        double[] interpolated = AverageInterpolator.interpolator().interpolate(input.getValues(), null);
        return TsData.ofInternal(input.getStart(), interpolated);
    }

    public TsData airlineInterpolation(TsData input) {
        TsPeriod start = input.getStart();
        SarimaOrders spec = SarimaOrders.airline(start.annualFrequency());
        DoubleSeq values = input.getValues();
        SarimaMapping mapping = SarimaMapping.of(spec);
        SsfData data = new SsfData(values);
        SsfFunction fn = SsfFunction.builder(data,
                mapping,
                s -> SsfArima.ssf(s))
                .useFastAlgorithm(false)
                .useScalingFactor(true)
                .useLog(false)
                .useMaximumLikelihood(true)
                .build();
        // estimate 
        SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                .builder()
                .build();
        fmin.minimize(fn.ssqEvaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt= (SsfFunctionPoint) fmin.getResult();
        ISsf ssf = rslt.getSsf();
        DataBlockStorage fs = DkToolkit.fastSmooth(ssf, data);
        double[] g=input.getValues().toArray();
        for (int i=0; i<g.length; ++i){
            if (Double.isNaN(g[i])){
                g[i]=ssf.loading().ZX(i, fs.block(i));
            }
        }
        return TsData.ofInternal(input.getStart(), g);
    }

}
