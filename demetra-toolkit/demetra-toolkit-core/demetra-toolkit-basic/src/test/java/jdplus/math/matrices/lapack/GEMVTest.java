/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.DataPointer;
import ec.tstoolkit.random.JdkRNG;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixTransformation;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class GEMVTest {

    public GEMVTest() {
    }

    @Test
    public void test() {
        FastMatrix A = FastMatrix.make(10, 20);
        FastMatrix B = A.extract(2, 6, 3, 15);
        JdkRNG rng = JdkRNG.newRandom(0);
        A.set((i, j) -> rng.nextDouble());
        DataBlock xa = DataBlock.make(A.getColumnsCount()),
                ya = DataBlock.make(A.getRowsCount());
        xa.set(rng::nextDouble);
        ya.set(rng::nextDouble);
        DataBlock xb = DataBlock.make(B.getColumnsCount()),
                yb = DataBlock.make(B.getRowsCount());
        xb.set(rng::nextDouble);
        yb.set(rng::nextDouble);
        GEMV.apply(0.1, A, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.None);
        GEMV.apply(0, A, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.None);
        GEMV.apply(0.1, A, DataPointer.of(xa), 0, DataPointer.of(ya), MatrixTransformation.None);
        GEMV.apply(0.1, B, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.None);
        GEMV.apply(0, B, DataPointer.of(xa), -1, DataPointer.of(yb), MatrixTransformation.None);
        GEMV.apply(0.1, B, DataPointer.of(xb.reverse()), 0, DataPointer.of(yb.reverse()), MatrixTransformation.None);
    }

    @Test
    public void testTranspose() {
        FastMatrix A = FastMatrix.make(10, 20);
        FastMatrix B = A.extract(2, 6, 3, 15);
        JdkRNG rng = JdkRNG.newRandom(0);
        A.set((i, j) -> rng.nextDouble());
        DataBlock xa = DataBlock.make(A.getRowsCount()),
                ya = DataBlock.make(A.getColumnsCount());
        xa.set(rng::nextDouble);
        ya.set(rng::nextDouble);
        DataBlock xb = DataBlock.make(B.getRowsCount()),
                yb = DataBlock.make(B.getColumnsCount());
        xb.set(rng::nextDouble);
        yb.set(rng::nextDouble);
        GEMV.apply(0.1, A, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.Transpose);
        GEMV.apply(0, A, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.Transpose);
        GEMV.apply(0.1, A, DataPointer.of(xa), 0, DataPointer.of(ya), MatrixTransformation.Transpose);
        GEMV.apply(0.1, B, DataPointer.of(xa), -1, DataPointer.of(ya), MatrixTransformation.Transpose);
        GEMV.apply(0, B, DataPointer.of(xa), -1, DataPointer.of(yb), MatrixTransformation.Transpose);
        GEMV.apply(0.1, B, DataPointer.of(xb.reverse()), 0, DataPointer.of(yb.reverse()), MatrixTransformation.Transpose);
    }
}