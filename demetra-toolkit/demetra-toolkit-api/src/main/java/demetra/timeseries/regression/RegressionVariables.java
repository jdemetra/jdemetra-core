/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.timeseries.regression.RegressionVariablesLoader;
import demetra.timeseries.TimeSeriesDomain;
import java.util.List;
import java.util.function.Predicate;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class RegressionVariables {

    private final RegressionVariablesLoader.Processor PROCESSOR = new RegressionVariablesLoader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public <D extends TimeSeriesDomain> MatrixType matrix(@NonNull D domain, @NonNull ITsVariable... vars) {
        return PROCESSOR.get().matrix(domain, vars);
    }

    public <D extends TimeSeriesDomain> MatrixType matrix(@NonNull D domain, @NonNull List<ITsVariable> vars) {
        return PROCESSOR.get().matrix(domain, vars.toArray(new ITsVariable[vars.size()]));
    }

    public <D extends TimeSeriesDomain> DoubleSeq linearEffect(@NonNull D domain, @NonNull List<ITsVariable> vars, DoubleSeq coefficients, Predicate<ITsVariable> predicate) {
        double[] data = new double[domain.length()];
        DoubleSeq.Mutable Data = DoubleSeq.Mutable.of(data);
        DoubleSeqCursor cursor = coefficients.cursor();
        for (ITsVariable var : vars) {
            int n = var.dim();
            if (predicate.test(var)) {
                MatrixType M = matrix(domain, var);
                for (int i = 0; i < n; ++i) {
                    Data.addAY(cursor.getAndNext(), M.column(i));
                }
            } else {
                cursor.skip(n);
            }
        }
        return DoubleSeq.of(data);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @FunctionalInterface
    public static interface Processor {

        public <D extends TimeSeriesDomain> MatrixType matrix(@NonNull D domain, @NonNull ITsVariable... vars);

    }

}
