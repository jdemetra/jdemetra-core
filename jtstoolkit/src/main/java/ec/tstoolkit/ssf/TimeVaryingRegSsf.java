/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingRegSsf extends AbstractTimeVaryingRegSsf {

    private final Matrix nvar_, wnvar_, fullnvar_;

    public TimeVaryingRegSsf(final ISsf ssf, final SubMatrix X, final Matrix nvar, Matrix wnvar) {
        super(ssf, X);
        nvar_ = nvar;
        wnvar_ = wnvar;
        if (wnvar == null) {
            fullnvar_ = nvar;
        } else {
            fullnvar_ = SymmetricMatrix.quadraticFormT(nvar, wnvar);
        }
    }

    public TimeVaryingRegSsf(final ISsf ssf, final SubMatrix X, final Matrix nvar) {
        super(ssf, X);
        nvar_ = nvar;
        wnvar_ = null;
        fullnvar_ = nvar;
    }
    
    public Matrix getNoiseVar(){
        return nvar_;
    }
    
    public Matrix getFullNoiseVar(){
        return fullnvar_;
    }

    @Override
    protected void fullRegNoise(int pos, SubMatrix vregs) {
        vregs.copy(fullnvar_.subMatrix());
    }

    @Override
    protected int getRegNoiseDim() {
        return fullnvar_.getColumnsCount();
    }

    @Override
    protected boolean isRegNoiseTimeInvariant() {
        return true;
    }

    @Override
    protected void regNoise(int pos, SubMatrix vregs) {
        vregs.copy(nvar_.subMatrix());
    }

    @Override
    protected void wRegNoise(int pos, SubMatrix vregs) {
        // Should never be called.
        if (wnvar_ != null) {
            vregs.copy(wnvar_.subMatrix());
        }
    }

}
