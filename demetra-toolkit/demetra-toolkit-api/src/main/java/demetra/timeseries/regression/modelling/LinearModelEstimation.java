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

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.MissingValueEstimation;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.PreadjustmentVariable;
import demetra.timeseries.regression.Variable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public final class LinearModelEstimation<M> {

    private final TsData originalSeries;

    // Series transformations
    private final TsDomain estimationDomain;
    private final boolean logTransformation;
    private final LengthOfPeriodType lpTransformation;

    /**
     * Pre-adjustment variables. Pre-specified mean should be integrated in
     * the pre-adjustment variables
     */
    private final PreadjustmentVariable[] preadjustmentVariables;

    /**
     * Mean correction (on the differenced variable)
     */
    private boolean meanCorrection;

    /**
     * Regression variables
     */
    private final Variable[] variables;

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

    private InformationSet addtionalResults=new InformationSet();

    public TsData preadjustmentEffect(TsDomain domain, Predicate<ITsVariable> test) {
        if (preadjustmentVariables == null || preadjustmentVariables.length ==0)
            return null;
        double[] data = new double[domain.length()];
        DoubleSeq.Mutable Data = DoubleSeq.Mutable.of(data);
        for (PreadjustmentVariable var : preadjustmentVariables) {
            if (test.test(var.getVariable())) {
                MatrixType M = RegressionVariables.matrix(domain, var.getVariable());
                DoubleSeqCursor cursor = var.getCoefficients().cursor();
                for (int i = 0; i < M.getColumnsCount(); ++i) {
                    Data.addAY(cursor.getAndNext(), M.column(i));
                }
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), data);
    }

    public TsData regressionEffect(@NonNull TsDomain domain, Predicate<Variable> predicate) {
        if (variables == null || variables.length ==0)
            return null;
        double[] data = new double[domain.length()];
        DoubleSeq.Mutable Data = DoubleSeq.Mutable.of(data);
        int cpos = meanCorrection ? 1 : 0;
        for (Variable var : variables) {
            int n = var.getVariable().dim();
            if (predicate.test(var)) {
                MatrixType M = RegressionVariables.matrix(domain, var.getVariable());
                for (int i = 0; i < n; ++i) {
                    Data.addAY(coefficients[cpos++], M.column(i));
                }
            } else {
                cpos += n;
            }
        }
        return TsData.ofInternal(domain.getStartPeriod(), data);
    }
    

}
