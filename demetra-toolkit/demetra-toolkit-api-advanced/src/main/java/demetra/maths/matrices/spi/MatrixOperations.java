/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.maths.matrices.spi;

import demetra.math.matrices.MatrixType;
import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class MatrixOperations {

    private final MatrixOperationsLoader.Processor PROCESSOR = new MatrixOperationsLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public MatrixType plus(MatrixType left, MatrixType right) {
        return PROCESSOR.get().plus(left, right);
    }

    public MatrixType plus(MatrixType M, double d) {
        return PROCESSOR.get().plus(M, d);
    }

    public MatrixType minus(MatrixType left, MatrixType right) {
        return PROCESSOR.get().minus(left, right);
    }

    public MatrixType minus(MatrixType M, double d) {
        return PROCESSOR.get().minus(M, d);
    }

    public MatrixType times(MatrixType left, MatrixType right) {
        return PROCESSOR.get().times(left, right);
    }

    public MatrixType times(MatrixType M, double d) {
        return PROCESSOR.get().times(M, d);
    }

    public MatrixType chs(MatrixType M) {
        return PROCESSOR.get().chs(M);
    }

    public MatrixType inv(MatrixType M) {
        return PROCESSOR.get().inv(M);
    }

    public MatrixType transpose(MatrixType M) {
        return PROCESSOR.get().transpose(M);
    }

    public MatrixType XXt(MatrixType X) {
        return PROCESSOR.get().XXt(X);
    }

    public MatrixType XtX(MatrixType X) {
        return PROCESSOR.get().XtX(X);
    }

    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @Algorithm
    public static interface Processor {

        MatrixType plus(MatrixType left, MatrixType right);

        MatrixType plus(MatrixType M, double d);

        MatrixType minus(MatrixType left, MatrixType right);

        MatrixType minus(MatrixType M, double d);

        MatrixType times(MatrixType left, MatrixType right);

        MatrixType times(MatrixType M, double d);

        MatrixType chs(MatrixType M);

        MatrixType inv(MatrixType M);

        MatrixType transpose(MatrixType M);

        MatrixType XXt(MatrixType X);

        MatrixType XtX(MatrixType X);
    }

}
