/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.math.matrices.lapack.GEMM;

/**
 *
 * @author palatej
 */

@lombok.experimental.UtilityClass
public class MatrixOperations {
    
    /**
     * Computes C:=alpha A(') * B(') + beta * C
     * 
     * @param alpha
     * @param A
     * @param B
     * @param beta
     * @param C
     * @param at
     * @param bt 
     */
    public void aAB_p_bC(double alpha, FastMatrix A, FastMatrix B, double beta, FastMatrix C, MatrixTransformation at, MatrixTransformation bt){
        GEMM.apply(alpha, A, B, beta, C, at, bt);
    }
    
    public void aAB_p_bC(double alpha, FastMatrix A, FastMatrix B, double beta, Matrix C){
        GEMM.apply(alpha, A, B, beta, C, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    // Simplified versions
    
    public void setAB(FastMatrix A, FastMatrix B, FastMatrix AB){
        aAB_p_bC(1, A, B, 0, AB, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    public Matrix AB(FastMatrix A, FastMatrix B){
        Matrix R=Matrix.make(A.getRowsCount(), B.getColumnsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.None, MatrixTransformation.None);
        return R;
    }
    
    public void setABt(FastMatrix A, FastMatrix B, FastMatrix ABt){
        aAB_p_bC(1, A, B, 0, ABt, MatrixTransformation.None, MatrixTransformation.Transpose);
    }
    
    public Matrix ABt(FastMatrix A, FastMatrix B){
        Matrix R=Matrix.make(A.getRowsCount(), B.getRowsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.None, MatrixTransformation.Transpose);
        return R;
    }
    
    public void setAtB(FastMatrix A, FastMatrix B, FastMatrix AtB){
        aAB_p_bC(1, A, B, 0, AtB, MatrixTransformation.Transpose, MatrixTransformation.None);
    }
    
    public Matrix AtB(FastMatrix A, FastMatrix B){
        Matrix R=Matrix.make(A.getColumnsCount(), B.getColumnsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.Transpose, MatrixTransformation.None);
        return R;
    }
    
    public void setAtBt(FastMatrix A, FastMatrix B, FastMatrix AtBt){
        aAB_p_bC(1, A, B, 0, AtBt, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
    }

    public Matrix AtBt(FastMatrix A, FastMatrix B){
        Matrix R=Matrix.make(A.getColumnsCount(), B.getRowsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
        return R;
    }

    public void addAB(FastMatrix A, FastMatrix B, FastMatrix CpAB){
        aAB_p_bC(1, A, B, 1, CpAB, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    public void addABt(FastMatrix A, FastMatrix B, FastMatrix CpABt){
        aAB_p_bC(1, A, B, 1, CpABt, MatrixTransformation.None, MatrixTransformation.Transpose);
    }
    
    public void addAtB(FastMatrix A, FastMatrix B, FastMatrix CpAtB){
        aAB_p_bC(1, A, B, 1, CpAtB, MatrixTransformation.Transpose, MatrixTransformation.None);
    }
    
    public void addAtBt(FastMatrix A, FastMatrix B, FastMatrix CpAtBt){
        aAB_p_bC(1, A, B, 1, CpAtBt, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
    }
}
