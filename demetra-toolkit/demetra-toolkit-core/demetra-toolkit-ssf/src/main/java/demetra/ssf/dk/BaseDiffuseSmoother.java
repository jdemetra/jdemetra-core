/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.FastMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.ISmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.data.DoubleSeqCursor;
import demetra.ssf.ISsfLoading;

/**
 *
 * @author Jean Palate
 */
public abstract class BaseDiffuseSmoother {

    protected final ISsf ssf;
    protected final ISsfDynamics dynamics;
    protected final ISsfLoading loading;
    protected final boolean calcvar, rescalevar;
    protected ISmoothingResults srslts;

    protected double e, f, fi;
    protected DataBlock C, Ci, Rf, Ri, tmp0, tmp1, Z;
    protected FastMatrix N0, N1, N2;
    protected boolean missing, hasinfo;
    
    protected BaseDiffuseSmoother(ISsf ssf, boolean calcvar, boolean rescalevar){
        this.ssf=ssf;
        this.calcvar=calcvar;
        this.rescalevar=rescalevar;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
    }

    public ISmoothingResults getResults() {
        return srslts;
    }

    protected void iterate(int pos) {
        iterateR(pos);
        updateA(pos);
        if (calcvar) {
            // P = P-PNP
            iterateN(pos);
            updateP(pos);
        }
    }
    // 

    protected abstract void updateA(int pos);

    protected abstract void updateP(int pos);

    /**
     * Computes in place x = x-c/f*z
     *
     * @param x
     * @param k
     */
    private void xQ(int pos, DataBlock x) {
        loading.XpZd(pos, x, -x.dot(C));
    }

    private void XQ(int pos, DataBlockIterator X) {
        while (X.hasNext()) {
            xQ(pos, X.next());
        }
    }

    private void xQi(int pos, DataBlock x) {
        loading.XpZd(pos, x, -x.dot(Ci));
    }

    private void XQi(int pos, DataBlockIterator X) {
        while (X.hasNext()) {
            xQi(pos, X.next());
        }
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (missing || (f == 0 && fi == 0)) {
            iterateMissingN(pos);
        } else if (fi == 0) {
            iterateRegularN(pos);
        } else {
            iterateDiffuseN(pos);
        }
    }

    private void iterateMissingN(int pos) {
        tvt(pos, N0);
        tvt(pos, N1);
        tvt(pos, N2);
        // reinforceSymmetry();
    }

    private void iterateRegularN(int pos) {
        // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
        tvt(pos, N0);
        XQ(pos, N0.rowsIterator());
        XQ(pos, N0.columnsIterator());
        loading.VpZdZ(pos, N0, 1 / f);
        tvt(pos, N1);
        XQ(pos, N1.columnsIterator());
        tvt(pos, N2);
    }

    private void iterateDiffuseN(int pos) {
        // Nf = Li'*Nf*Li
        // N1 = Z'Z/Fi + Li'*N1*Li - < Z'Kf'*Nf'*Li >
        // N2 = Z'Z * c + Li'*N2*Li - < Z'Kf'*N1'*Li >, c= Kf'*Nf*Kf-Ff/(Fi*Fi)
        // compute first N2 then N1 and finally Nf
        tvt(pos, N0);
        tvt(pos, N1);
        tvt(pos, N2);

        tmp0.product(C, N0.columnsIterator());
        tmp1.product(C, N1.columnsIterator());

        double kn0k = tmp0.dot(C);

        XQi(pos, N0.rowsIterator());
        XQi(pos, N0.columnsIterator());
        XQi(pos, N1.rowsIterator());
        XQi(pos, N2.columnsIterator());
        XQi(pos, N2.rowsIterator());
        XQi(pos, N1.columnsIterator());
        xQi(pos, tmp0);
        xQi(pos, tmp1);

        DataBlockIterator n1cols=N1.columnsIterator(), n2cols=N2.columnsIterator();
        DoubleSeqCursor z=Z.cursor();
        double h=kn0k - f / (fi * fi);
        while (n1cols.hasNext()){
            double zx=z.getAndNext();
            if (zx != 0){
                loading.XpZd(pos, n1cols.next(), zx/fi);
                loading.XpZd(pos, n2cols.next(), zx*h);
            }else{
                n1cols.next();
                n2cols.next();
            }
        }
//        measurement.VpZdZ(pos, N1, 1 / fi); 
//        measurement.VpZdZ(pos, N2, kn0k - f / (fi * fi));

        subZ(pos, N1.rowsIterator(), tmp0);
        subZ(pos, N1.columnsIterator(), tmp0);
        subZ(pos, N2.rowsIterator(), tmp1);
        subZ(pos, N2.columnsIterator(), tmp1);
    }

    private void tvt(int pos, FastMatrix N) {
        dynamics.MT(pos, N);
        dynamics.MT(pos, N.transpose());
    }

    private void subZ(int pos, DataBlockIterator rows, DataBlock b) {
        DoubleSeqCursor cell = b.cursor();
        while (rows.hasNext()) {
            double cur = cell.getAndNext();
            DataBlock row = rows.next();
            if (cur != 0) {
                loading.XpZd(pos, row, -cur);
            }
        } 
    }

    /**
     *
     */
    private void iterateR(int pos) {
        if (fi == 0) {
            iterateRegularR(pos);
        } else {
            iterateDiffuseR(pos);
        }
    }

    private void iterateRegularR(int pos) {
        // R(t-1)=v(t)/f(t)Z(t)+R(t)L(t)
        //   = v/f*Z + R*(T-TC/f*Z)
        //  = (v - RT*C)/f*Z + RT
        dynamics.XT(pos, Rf);
        dynamics.XT(pos, Ri);
        if (!missing && f != 0) {
            // RT
            double c = e / f - Rf.dot(C);
            loading.XpZd(pos, Rf, c);
        }
    }

    private void iterateDiffuseR(int pos) {
        dynamics.XT(pos, Rf);
        dynamics.XT(pos, Ri);
        if (!missing /*&& f != 0*/) {
            // Ri(t-1)=c*Z(t) +Ri(t)*T(t)
            // c = e/fi-(Ri(t)*T(t)*Ci(t))/fi-(Rf(t)*T(t)*Cf(t))/f
            double ci = e / fi - Ri.dot(Ci) - Rf.dot(C);
            loading.XpZd(pos, Ri, ci);
            // Rf(t-1)=c*Z(t)+Rf(t)*T(t)
            // c =  - Rf(t)T(t)*Ci/fi
            double cf = -Rf.dot(Ci);
            loading.XpZd(pos, Rf, cf);
        }
    }

}
