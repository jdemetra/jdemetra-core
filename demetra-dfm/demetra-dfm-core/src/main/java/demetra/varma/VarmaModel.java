/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.varma;

import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class VarmaModel {

    private static final Matrix[] EMPTY = new Matrix[0];

    private int n_;
    private final Matrix[] phi_, th_;
    private final Matrix sig_;
    
     public VarmaModel(Matrix[] phi, Matrix[] th, Matrix noise) {
        if (phi == null || phi.length == 0) {
            phi_ = EMPTY;
        } else {
            phi_ = phi;
            n_ = phi[0].getRowsCount();
        }
        if (th == null || th.length == 0) {
            th_ = EMPTY;
        } else {
            th_ = th;
            n_ = th[0].getRowsCount();
        }
        if (noise == null) {
            sig_ = Matrix.identity(n_);
        } else {
            sig_ = noise;
        }
    }

    public final int getP() {
        return phi_.length;
    }

    public final int getQ() {
        return th_.length;
    }
    
    public final Matrix sig(){
        return sig_;
    }

    public final Matrix phi(int lag) {
        return phi_[lag - 1];
    }

    public final Matrix th(int lag) {
        return th_[lag - 1];
    }

    public final int getDim() {
        return n_;
    }
    
}
