/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.varma;

import demetra.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public class VarmaModel {

    private static final FastMatrix[] EMPTY = new FastMatrix[0];

    private int n_;
    private final FastMatrix[] phi_, th_;
    private final FastMatrix sig_;
    
     public VarmaModel(FastMatrix[] phi, FastMatrix[] th, FastMatrix noise) {
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
            sig_ = FastMatrix.identity(n_);
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
    
    public final FastMatrix sig(){
        return sig_;
    }

    public final FastMatrix phi(int lag) {
        return phi_[lag - 1];
    }

    public final FastMatrix th(int lag) {
        return th_[lag - 1];
    }

    public final int getDim() {
        return n_;
    }
    
}
