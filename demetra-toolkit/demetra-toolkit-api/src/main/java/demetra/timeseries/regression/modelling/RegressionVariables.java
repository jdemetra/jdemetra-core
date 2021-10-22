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
import demetra.design.Algorithm;
import nbbrd.design.Development;
import demetra.timeseries.TimeSeriesDomain;
import java.util.List;
import java.util.function.Predicate;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.timeseries.regression.ITsVariable;
import demetra.math.matrices.Matrix;

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

    public <D extends TimeSeriesDomain> Matrix matrix(@NonNull D domain, @NonNull ITsVariable... vars) {
        return PROCESSOR.get().matrix(domain, vars);
    }

    public <D extends TimeSeriesDomain> Matrix matrix(@NonNull D domain, @NonNull List<ITsVariable> vars) {
        return PROCESSOR.get().matrix(domain, vars.toArray(new ITsVariable[vars.size()]));
    }

    public <D extends TimeSeriesDomain> DoubleSeq linearEffect(@NonNull D domain, @NonNull List<ITsVariable> vars, DoubleSeq coefficients, Predicate<ITsVariable> predicate) {
        double[] data = new double[domain.length()];
        DoubleSeq.Mutable Data = DoubleSeq.Mutable.of(data);
        DoubleSeqCursor cursor = coefficients.cursor();
        for (ITsVariable var : vars) {
            int n = var.dim();
            if (predicate.test(var)) {
                Matrix M = matrix(domain, var);
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

        public <D extends TimeSeriesDomain> Matrix matrix(@NonNull D domain, @NonNull ITsVariable... vars);

    }

}
