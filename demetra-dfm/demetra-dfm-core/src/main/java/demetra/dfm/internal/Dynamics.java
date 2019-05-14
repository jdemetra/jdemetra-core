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
package demetra.dfm.internal;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.ssf.ISsfDynamics;

/**
 *
 * @author Jean Palate
 */
@lombok.Data(staticConstructor = "of")
public class Dynamics implements ISsfDynamics {

    public static Dynamics of(CanonicalMatrix T, CanonicalMatrix V, int nlx) {
        Dynamics dyn = new Dynamics(nlx, T, V);
        int nf=V.getRowsCount();
        dyn.ttmp = new double[nf];
        dyn.xtmp = new double[nf*nlx];
        return dyn;
    }

    final int nlx;
    final CanonicalMatrix T, V;
    double[] ttmp, xtmp;

    int nl() {
        return T.getColumnsCount() / T.getRowsCount();
    }

    int nf() {
        return T.getRowsCount();
    }

    @Override
    public int getInnovationsDim() {
        return V.getRowsCount();
    }

    @Override
    public void V(int pos, FastMatrix qm) {
        qm.copy(V);
    }

    @Override
    public void S(int pos, FastMatrix cm) {
        int nf = nf();
        for (int i = 0, r = 0; i < nf; ++i, r += nlx) {
            cm.set(r, i, 1);
        }
    }

    @Override
    public boolean hasInnovations(int pos) {
        return true;
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return true;
    }

    @Override
    public void T(int pos, FastMatrix tr) {
        int nl = nl(), nf = nf();
        for (int i = 0, r = 0; i < nf; ++i, r += nlx) {
            for (int j = 0, c = 0; j < nf; ++j, c += nlx) {
                FastMatrix B = tr.extract(r, r + nlx, c, c + nlx);
                if (i == j) {
                    B.subDiagonal(-1).set(1);
                }
                B.row(0).range(0, nl).copy(T.row(i).range(j * nl, (j + 1) * nl));
            }
        }
    }

    @Override
    public void TX(int pos, DataBlock x) {
        int nl = nl(), nf = nf();
        // compute first the next item
        for (int i = 0; i < nf; ++i) {
            double r = 0;
            DataWindow p = T.row(i).left();
            DataWindow xb = x.left();
            r += p.next(nl).dot(xb.next(nl));
            for (int j = 1; j < nf; ++j) {
                r += p.next(nl).dot(xb.slide(nlx));
            }
            ttmp[i] = r;
        }
        x.fshiftAndZero();
        x.extract(0, -1, nlx).copyFrom(ttmp, 0);
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        x.extract(0, nf(), nlx).add(u);
    }

    @Override
    public void addV(int pos, FastMatrix p) {
        int nf = nf();
        for (int i = 0; i < nf; ++i) {
            DataBlock cv = p.column(i * nlx).extract(0, nf, nlx);
            cv.add(V.column(i));
        }
    }

    @Override
    public void XT(int pos, DataBlock x) {
        int nl = nl(), nf = nf();
        for (int i = 0, k = 0, l = 0; i < nf; ++i) {
            for (int j = 0; j < nl; ++j, ++k) {
                double r = ((k + 1) % nlx != 0) ? x.get(k + 1) : 0;
                r += T.column(l++).dot(x.extract(0, nf, nlx));
                xtmp[k] = r;
            }
            for (int j = nl; j < nlx - 1; ++j, ++k) {
                xtmp[k] = x.get(k + 1);
            }
            if (nlx > nl) {
                xtmp[k++] = 0;
            }
        }
        x.copyFrom(xtmp, 0);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        xs.copy(x.extract(0, nf(), nlx));
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
    }
}
