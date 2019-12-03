/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import jdplus.data.DataBlock;
import jdplus.math.matrices.lapack.DataPointer;
import jdplus.math.matrices.lapack.GEMM;
import jdplus.math.matrices.lapack.GER;

/**
 *
 * @author palatej
 */

@lombok.experimental.UtilityClass
public class MatrixOperations {
    
    /**
     * Computes A:=alpha * x * y' + A
     * @param alpha
     * @param x
     * @param y
     * @param A 
     */
    public void aXY_pA(double alpha, DataBlock x, DataBlock y, Matrix A){
        GER.apply(alpha, DataPointer.of(x), DataPointer.of(y), A);
    }
    
    public void addXYt(DataBlock x, DataBlock y, Matrix A){
        GER.apply(1, DataPointer.of(x), DataPointer.of(y), A);
    }
    
    public Matrix XYt(DataBlock x, DataBlock y){
        Matrix A=Matrix.make(x.length(), y.length());
        GER.apply(1, DataPointer.of(x), DataPointer.of(y), A);
        return A;
    }
    
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
    public void aAB_p_bC(double alpha, Matrix A, Matrix B, double beta, Matrix C, MatrixTransformation at, MatrixTransformation bt){
        GEMM.apply(alpha, A, B, beta, C, at, bt);
    }
    
    public void aAB_p_bC(double alpha, Matrix A, Matrix B, double beta, Matrix C){
        GEMM.apply(alpha, A, B, beta, C, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    // Simplified versions
    
    public void setAB(Matrix A, Matrix B, Matrix AB){
        aAB_p_bC(1, A, B, 0, AB, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    public Matrix AB(Matrix A, Matrix B){
        Matrix R=Matrix.make(A.getRowsCount(), B.getColumnsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.None, MatrixTransformation.None);
        return R;
    }
    
    public void setABt(Matrix A, Matrix B, Matrix ABt){
        aAB_p_bC(1, A, B, 0, ABt, MatrixTransformation.None, MatrixTransformation.Transpose);
    }
    
    public Matrix ABt(Matrix A, Matrix B){
        Matrix R=Matrix.make(A.getRowsCount(), B.getRowsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.None, MatrixTransformation.Transpose);
        return R;
    }
    
    public void setAtB(Matrix A, Matrix B, Matrix AtB){
        aAB_p_bC(1, A, B, 0, AtB, MatrixTransformation.Transpose, MatrixTransformation.None);
    }
    
    public Matrix AtB(Matrix A, Matrix B){
        Matrix R=Matrix.make(A.getColumnsCount(), B.getColumnsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.Transpose, MatrixTransformation.None);
        return R;
    }
    
    public void setAtBt(Matrix A, Matrix B, Matrix AtBt){
        aAB_p_bC(1, A, B, 0, AtBt, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
    }

    public Matrix AtBt(Matrix A, Matrix B){
        Matrix R=Matrix.make(A.getColumnsCount(), B.getRowsCount());
        aAB_p_bC(1, A, B, 0, R, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
        return R;
    }

    public void addAB(Matrix A, Matrix B, Matrix CpAB){
        aAB_p_bC(1, A, B, 1, CpAB, MatrixTransformation.None, MatrixTransformation.None);
    }
    
    public void addABt(Matrix A, Matrix B, Matrix CpABt){
        aAB_p_bC(1, A, B, 1, CpABt, MatrixTransformation.None, MatrixTransformation.Transpose);
    }
    
    public void addAtB(Matrix A, Matrix B, Matrix CpAtB){
        aAB_p_bC(1, A, B, 1, CpAtB, MatrixTransformation.Transpose, MatrixTransformation.None);
    }
    
    public void addAtBt(Matrix A, Matrix B, Matrix CpAtBt){
        aAB_p_bC(1, A, B, 1, CpAtBt, MatrixTransformation.Transpose, MatrixTransformation.Transpose);
    }
}
