/*
 * Copyright 2013-2014 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.var;

import jd.data.DataBlock;
import jd.maths.matrices.LowerTriangularMatrix;
import jd.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class VarDynamics implements ISsfDynamics {

    private final FastMatrix V, T;
    private final int nvars, nl, nlx;
    private final DataBlock ttmp, xtmp;
    private FastMatrix L;

    public static VarDynamics of(final VarDescriptor desc) {
        int nlx = desc.getVarMatrix().getColumnsCount();
        if (nlx % desc.getLagsCount() != 0) {
            return null;
        }
        nlx /= desc.getVariablesCount();
        if (nlx < desc.getLagsCount()) {
            return null;
        }
        return new VarDynamics(desc, nlx);
    }

    public static VarDynamics of(final VarDescriptor desc, final int nlags) {
        int nl = Math.max(nlags, desc.getLagsCount());
        return new VarDynamics(desc, nl);
    }

    private VarDynamics(final VarDescriptor desc, final int nlx) {
        nvars = desc.getVariablesCount();
        nl = desc.getLagsCount();
        this.nlx = nlx;
        V=desc.getInnovationsVariance();
        T = desc.getVarMatrix();
        ttmp = DataBlock.make(nvars);
        xtmp = DataBlock.make(nvars * nl);
    }

    public int getLagsCount() {
        return nlx;
    }

    private FastMatrix L() {
        if (L == null) {
            L = V.deepClone();
            SymmetricMatrix.lcholesky(L, 1e-9);
        }
        return L;
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }

    @Override
    public int getInnovationsDim() {
        return nvars;
    }

    @Override
    public void V(int pos, FastMatrix qm) {
        qm.topLeft(nvars, nvars).copy(V);
    }

    @Override
    public boolean hasInnovations(int pos) {
        return true;
    }

    @Override
    public void S(int pos, FastMatrix sm) {
        sm.topLeft(nvars, nvars).copy(L());
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        DataBlock v = u.deepClone();
        LowerTriangularMatrix.rmul(L(), v);
        x.range(0, nvars).add(v);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        xs.copy(x.range(0, nvars));
        LowerTriangularMatrix.lmul(L(), xs);
    }

    @Override
    public void T(int pos, FastMatrix tr) {
        tr.topLeft(nvars, nvars * nl).copy(T);
        tr.subDiagonal(-nvars).set(1);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        TX(x);
    }

    private void TX(DataBlock x) {
        for (int i = 0; i < nvars; ++i) {
            ttmp.set(i, T.row(i).dot(x));
        }
        x.fshift(nvars);
        x.extract(0, nvars).copy(ttmp);
    }

    @Override
    public void XT(int pos, DataBlock x) {
        xtmp.set(0);
        xtmp.product(x.range(0, nvars), T.columnsIterator());
        x.bshift(nvars);
        x.range((nlx - 1) * nvars, x.length()).set(0);
        x.range(0, nl * nvars).add(xtmp);
    }

    @Override
    public void addV(final int pos, final FastMatrix v) {
        v.topLeft(nvars, nvars).add(V);
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return true;
    }

}
