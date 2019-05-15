/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.varma;

import jd.maths.matrices.CanonicalMatrix;

/**
 *
 * @author palatej
 */
public class VarmaModel {

    private static final CanonicalMatrix[] EMPTY = new CanonicalMatrix[0];

    private int n_;
    private final CanonicalMatrix[] phi_, th_;
    private final CanonicalMatrix sig_;
    
     public VarmaModel(CanonicalMatrix[] phi, CanonicalMatrix[] th, CanonicalMatrix noise) {
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
            sig_ = CanonicalMatrix.identity(n_);
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
    
    public final CanonicalMatrix sig(){
        return sig_;
    }

    public final CanonicalMatrix phi(int lag) {
        return phi_[lag - 1];
    }

    public final CanonicalMatrix th(int lag) {
        return th_[lag - 1];
    }

    public final int getDim() {
        return n_;
    }
    
}
