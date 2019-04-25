/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import demetra.maths.matrices.MatrixType;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Initialization {
    
    public ISsfInitialization of(double[] a, MatrixType P) {
        if (P == null) {
            throw new IllegalArgumentException();
        }
        
        return demetra.ssf.Initialization.builder()
                .dim(P.getRowsCount())
                .a0(a)
                .Pf(FastMatrix.of(P))
                .build();
    }
    
    public ISsfInitialization ofDiffuse(double[] a, MatrixType P, MatrixType B, MatrixType Pi) {
        if (B == null && Pi == null) {
            return of(a, P);
        }
        if (B == null) {
            throw new IllegalArgumentException();
        }
        return demetra.ssf.Initialization.builder()
                .dim(P.getRowsCount())
                .diffuseDim(B.getColumnsCount())
                .a0(a)
                .Pf(FastMatrix.of(P))
                .B(B==null ? null : FastMatrix.of(B))
                .Pi(Pi==null ? null : FastMatrix.of(Pi))
                .build();
    }
}
