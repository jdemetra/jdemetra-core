/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.spi;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixOperations;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = MatrixOperations.Processor.class)
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
