/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.timeseries.regression.modelling;

import demetra.data.MissingValueEstimation;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.Variable;
import java.util.Map;

/**
 *
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class LinearModelEstimation<M> {

    private TsData originalSeries;

    // Series transformations
    private TsDomain estimationDomain;
    private boolean logTransformation;
    private LengthOfPeriodType lpTransformation;

    /**
     * Mean correction (after differencing)
     */
    private boolean meanCorrection;

    /**
     * Regression variables
     */
    private Variable[] variables;

    /**
     * Stochastic model
     */
    private M stochasticComponent;

    /**
     * Regression estimation. The order correspond to the order of the variables
     * (starting with the mean)
     */
    private double[] coefficients;
    private MatrixType coefficientsCovariance;
    private double[] parameters, score;
    private MatrixType parametersCovariance;
    private LikelihoodStatistics statistics;

    private MissingValueEstimation[] missing;
    private int freeParametersCount;

    @lombok.Singular
    private Map<String, Object> addtionalResults;


}
