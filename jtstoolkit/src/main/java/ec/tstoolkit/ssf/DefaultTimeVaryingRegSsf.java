/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf;

import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultTimeVaryingRegSsf extends AbstractTimeVaryingRegSsf {

    private final double nvar_;

    public DefaultTimeVaryingRegSsf(final ISsf ssf, final SubMatrix X, final double nvar) {
        super(ssf, X);
        nvar_ = nvar;
    }

    @Override
    protected void fullRegNoise(int pos, SubMatrix vregs) {
        vregs.diagonal().set(nvar_);
    }

    @Override
    protected int getRegNoiseDim() {
        return getX().getColumnsCount();
    }

    @Override
    protected boolean isRegNoiseTimeInvariant() {
        return true;
    }

    @Override
    protected void regNoise(int pos, SubMatrix vregs) {
        vregs.diagonal().set(nvar_);
    }

    @Override
    protected void wRegNoise(int pos, SubMatrix vregs) {
        // Should never be called.
        vregs.diagonal().set(1);
    }

}
