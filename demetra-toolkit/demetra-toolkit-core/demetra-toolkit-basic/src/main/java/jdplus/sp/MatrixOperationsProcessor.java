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
package jdplus.sp;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixOperations;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(MatrixOperations.Processor.class)
public class MatrixOperationsProcessor implements MatrixOperations.Processor {

    private static Matrix transform(CanonicalMatrix M) {
        return Matrix.ofInternal(M.getStorage(), M.getRowsCount(), M.getColumnsCount());
    }

    @Override
    public Matrix plus(Matrix left, Matrix right) {
        CanonicalMatrix L = CanonicalMatrix.of(left), R = CanonicalMatrix.of(right);
        return transform(L.plus(R));
    }

    @Override
    public Matrix plus(Matrix M, double d) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        return transform(m.plus(d));
    }

    @Override
    public Matrix minus(Matrix left, Matrix right) {
        CanonicalMatrix L = CanonicalMatrix.of(left), R = CanonicalMatrix.of(right);
        return transform(L.minus(R));
    }

    @Override
    public Matrix minus(Matrix M, double d) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        return transform(m.minus(d));
    }

    @Override
    public Matrix times(Matrix left, Matrix right) {
        CanonicalMatrix L = CanonicalMatrix.of(left), R = CanonicalMatrix.of(right);
        return transform(L.times(R));
    }

    @Override
    public Matrix times(Matrix M, double d) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        return transform(m.times(d));
    }

    @Override
    public Matrix chs(Matrix M) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        m.chs();
        return transform(m);
    }

    @Override
    public Matrix inv(Matrix M) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        return transform(m.inv());
    }

    @Override
    public Matrix transpose(Matrix M) {
        CanonicalMatrix m = CanonicalMatrix.of(M);
        return m.transpose().unmodifiable();
    }

    @Override
    public Matrix XXt(Matrix X) {
        CanonicalMatrix M = CanonicalMatrix.of(X);
        return transform(SymmetricMatrix.XXt(M));
    }

    @Override
    public Matrix XtX(Matrix X) {
        CanonicalMatrix M = CanonicalMatrix.of(X);
        return transform(SymmetricMatrix.XtX(M));
    }
}
