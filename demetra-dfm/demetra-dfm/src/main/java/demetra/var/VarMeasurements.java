/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.var;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.multivariate.ISsfMeasurements;

/**
 *
 * @author Jean Palate
 */
class VarMeasurements implements ISsfMeasurements {

    private final int nv, nl;

    VarMeasurements(int nv, int nl) {
        this.nv = nv;
        this.nl = nl;
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public int getCount(int pos) {
        return nv;
    }

    @Override
    public int getMaxCount() {
        return nv;
    }

    @Override
    public boolean isHomogeneous() {
        return true;
    }

    @Override
    public void Z(int pos, int var, DataBlock z) {
        z.set(var, 1);
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean hasIndependentErrors() {
        return true;
    }

    @Override
    public boolean hasError(int pos) {
        return false;
    }

    @Override
    public void H(int pos, Matrix h) {
    }

    @Override
    public void R(int pos, Matrix r) {
    }

    @Override
    public double ZX(int pos, int var, DataBlock m) {
        return m.get(var);
    }

    @Override
    public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
        if (ivar != jvar) {
            return 0;
        } else {
            return V.get(ivar, ivar);
        }
    }

    @Override
    public void addH(int pos, Matrix V) {
    }

    @Override
    public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
        if (ivar == jvar) {
            V.add(ivar, ivar, d);
        }
    }

    @Override
    public void XpZd(int pos, int var, DataBlock x, double d) {
        x.add(var, d);
    }
}
