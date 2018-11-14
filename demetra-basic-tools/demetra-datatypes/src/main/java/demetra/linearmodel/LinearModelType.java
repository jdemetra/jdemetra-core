/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder
public class LinearModelType {

    @lombok.NonNull
    private DoubleSequence y;

    private boolean meanCorrection;

    private MatrixType X;

    @lombok.Builder.Default
    private int[] missing=NOMISSING;
    
    public static final int[] NOMISSING = new int[0];

//    public int variablesCount() {
//        int n = 0;
//        if (meanCorrection) {
//            ++n;
//        }
//        if (X != null) {
//            n += X.getColumnsCount();
//        }
//        return n;
//    }
//
//    public MatrixType variables() {
//        int n = variablesCount();
//        if (n == 0) {
//            return null;
//        }
//        int m = y.length();
//        if (X != null && m != X.getRowsCount()) {
//            throw new EcoException();
//        }
//        if (!meanCorrection) {
//            return X;
//        }
//        double[] z = new double[n * m];
//        for (int i = 0; i < m; ++i) {
//            z[i] = 1;
//        }
//        if (X != null) {
//            X.copyTo(z, m);
//        }
//        return MatrixType.ofInternal(z, m, n);
//
//    }
}
