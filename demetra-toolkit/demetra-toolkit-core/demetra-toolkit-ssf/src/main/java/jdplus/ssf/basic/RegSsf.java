/*
 * Copyright 2017 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
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
package jdplus.ssf.basic;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.DataWindow;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.ISsfDynamics;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.StateComponent;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.Ssf;

/**
 * SSF extended by regression variables with fixed or time varying coefficients.
 * Time varying coefficients follow a multi-variate random walk.
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegSsf {
    
    public Ssf ssf(ISsf model, FastMatrix X){
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = model.getStateDim(), nx=X.getColumnsCount();
        return Ssf.of(new Xinitializer(model.initialization(), nx),
                new Xdynamics(mdim, model.dynamics(), nx),
                new Xloading(mdim, model.loading(), X),
                model.measurementError());
        
    }

    public Ssf timeVaryingSsf(ISsf model, FastMatrix X, FastMatrix cvar){
        int nx=X.getColumnsCount();
        if (X.isEmpty() || ! cvar.isSquare() || cvar.getColumnsCount() != nx) {
            throw new IllegalArgumentException();
        }
        int mdim = model.getStateDim();
        FastMatrix s = cvar.deepClone();
        SymmetricMatrix.lcholesky(s, 1e-12);
        return Ssf.of(new Xinitializer(model.initialization(), nx),
                new Xvardynamics(mdim, model.dynamics(), cvar, s),
                new Xloading(mdim, model.loading(), X),
                model.measurementError());
    }
    
    /**
     * Extends the loading of a given state block with regression variables
     * @param dim The size of the state block that will be extended
     * @param loading The loading to be applied on the state block
     * @param X The regression variables
     * @return 
     */
    public ISsfLoading defaultLoading(int dim, ISsfLoading loading, FastMatrix X) {
        return new Xloading(dim, loading, X);
    }

    public StateComponent of(StateComponent cmp, FastMatrix X) {
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = cmp.dim(), nx=X.getColumnsCount();
        return new StateComponent(new Xinitializer(cmp.initialization(), nx),
                new Xdynamics(mdim, cmp.dynamics(), nx));
    }

    /**
     * Creates a state component with time varying coefficients     *
     * @param cmp
     * @param X
     * @param cvar The covariance of the coefficients
     * @return
     */
    public StateComponent ofTimeVarying(StateComponent cmp, FastMatrix X, FastMatrix cvar) {
        int nx=X.getColumnsCount();
        if (X.isEmpty() || ! cvar.isSquare() || cvar.getColumnsCount() != nx) {
            throw new IllegalArgumentException();
        }
        int mdim = cmp.dim();
        FastMatrix s = cvar.deepClone();
        SymmetricMatrix.lcholesky(s, 1e-12);
        return new StateComponent(new Xinitializer(cmp.initialization(), nx),
                new Xvardynamics(mdim, cmp.dynamics(), cvar, s));
    }

    /**
     * Creates a state component with time varying coefficients, such that the innovations
     * covariance are defined by SS'
     *
     * @param cmp
     * @param X
     * @param l The Cholesky factor of the covariance of the coefficients
     * @return
     */
    public StateComponent ofTimeVaryingFactor(StateComponent cmp, FastMatrix X, FastMatrix l) {
        int nx=X.getColumnsCount();
        if (X.isEmpty() || ! l.isSquare() || l.getColumnsCount() != nx) {
            throw new IllegalArgumentException();
        }
        int mdim = cmp.dim();
        return new StateComponent(new Xinitializer(cmp.initialization(), nx),
                new Xvardynamics(mdim, cmp.dynamics(), SymmetricMatrix.XXt(l), l));
    }

    static class Xdynamics implements ISsfDynamics {

        private final int n, nx;
        private final ISsfDynamics dyn;

        Xdynamics(int n, ISsfDynamics dyn, int nx) {
            this.dyn = dyn;
            this.n = n;
            this.nx = nx;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            dyn.V(pos, qm.extract(0, n, 0, n));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            dyn.S(pos, cm.extract(0, n, 0, cm.getColumnsCount()));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dyn.hasInnovations(pos);
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dyn.areInnovationsTimeInvariant();
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            dyn.T(pos, tr.extract(0, n, 0, n));
            tr.diagonal().drop(n, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dyn.TX(pos, x.range(0, n));
        }

        @Override
        public void TM(int pos, FastMatrix m) {
            dyn.TM(pos, m.extract(0, n, 0, m.getColumnsCount()));
        }

        @Override
        public void TVT(int pos, FastMatrix m) {
            FastMatrix dz = m.extract(0, n, 0, n);
            dyn.TVT(pos, dz);
            FastMatrix hz = m.extract(0, n, n, nx);
            dyn.TM(pos, hz);
            FastMatrix cz = m.extract(n, nx, 0, n);
            cz.copyTranspose(hz);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XT(int pos, DataBlock x) {
            dyn.XT(pos, x.range(0, n));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            dyn.addV(pos, p.extract(0, n, 0, n));
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

    }

    static class Xvardynamics implements ISsfDynamics {

        private final int n, nx;
        private final ISsfDynamics dyn;
        private final FastMatrix var, s;

        private FastMatrix v00(FastMatrix v) {
            return v.extract(0, n, 0, n);
        }

        private FastMatrix r0(FastMatrix m) {
            return m.extract(0, n, 0, m.getColumnsCount());
        }

        private FastMatrix r1(FastMatrix m) {
            return m.extract(n, nx, 0, m.getColumnsCount());
        }

        private FastMatrix v11(FastMatrix v) {
            return v.extract(n, nx, n, nx);
        }

        private FastMatrix v01(FastMatrix v) {
            return v.extract(0, n, n, nx);
        }

        private FastMatrix v10(FastMatrix v) {
            return v.extract(n, nx, 0, n);
        }

        Xvardynamics(int n, ISsfDynamics dyn, FastMatrix xvar, FastMatrix xs) {
            this.dyn = dyn;
            this.n = n;
            this.nx = xvar.getColumnsCount();
            this.var = xvar;
            this.s = xs;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim() + s.getColumnsCount();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            dyn.V(pos, v00(qm));
            v11(qm).copy(var);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            int m=dyn.getInnovationsDim();
            dyn.S(pos, cm.extract(0, n, 0, m));
            cm.extract(n, nx, m, s.getColumnsCount()).copy(s);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return dyn.areInnovationsTimeInvariant();
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            dyn.T(pos, tr.extract(0, n, 0, n));
            tr.diagonal().drop(n, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dyn.TX(pos, x.range(0, n));
        }

        @Override
        public void TM(int pos, FastMatrix m) {
            dyn.TM(pos, r0(m));
        }

        @Override
        public void TVT(int pos, FastMatrix m) {
            dyn.TVT(pos, v00(m));
            FastMatrix v01 = v01(m);
            dyn.TM(pos, v01);
            v10(m).copyTranspose(v01);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            DataWindow xleft = x.left(), xsleft = xs.left();
            dyn.XS(pos, xleft.next(n), xsleft.next(dyn.getInnovationsDim()));
            xsleft.next(s.getColumnsCount()).product(xleft.next(nx), s.columnsIterator());
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XT(int pos, DataBlock x) {
            dyn.XT(pos, x.range(0, n));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            dyn.addV(pos, v00(p));
            v11(p).add(var);
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }
    }

    static class Xinitializer implements ISsfInitialization {

        private final int n, nx;
        private final ISsfInitialization dyn;

        Xinitializer(ISsfInitialization init, int nx) {
            this.dyn = init;
            this.n = init.getStateDim();
            this.nx = nx;
        }

        @Override
        public int getStateDim() {
            return n + nx;
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return nx + dyn.getDiffuseDim();
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            int nd = dyn.getDiffuseDim();
            if (nd > 0) {
                dyn.diffuseConstraints(b.extract(0,n,0,nd));
            }
            b.subDiagonal(nd-n).drop(nd, 0).set(1);
         }

        @Override
        public void a0(DataBlock a0) {
            dyn.a0(a0.range(0, n));
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            dyn.Pf0(pf0.extract(0,n, 0, n));
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            dyn.Pi0(pi0.extract(0, n, 0, n));
            pi0.diagonal().drop(n, 0).set(1);
        }
    }

    static class Xloading implements ISsfLoading {

        private final ISsfLoading loading;
        private final FastMatrix data;
        private final int n, nx;
        private final DataBlock tmp;

        private Xloading(final int n, final ISsfLoading loading, final FastMatrix data) {
            this.data = data;
            this.loading = loading;
            this.n = n;
            nx = data.getColumnsCount();
            tmp = DataBlock.make(nx);
        }

         private FastMatrix v00(FastMatrix v) {
            return v.extract(0, n, 0, n);
        }

        private FastMatrix r0(FastMatrix m) {
            return m.extract(0, n, 0, m.getColumnsCount());
        }

        private FastMatrix r1(FastMatrix m) {
            return m.extract(n, nx, 0, m.getColumnsCount());
        }

        private FastMatrix v11(FastMatrix v) {
            return v.extract(n, nx, n, nx);
        }

        private FastMatrix v01(FastMatrix v) {
            return v.extract(0, n, n, nx);
        }

        private FastMatrix v10(FastMatrix v) {
            return v.extract(n, nx, 0, n);
        }

       @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            DataWindow range = z.window(0, n);
            loading.Z(pos, range.get());
            range.next(nx).copy(data.row(pos));
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            DataWindow range = x.window(0, n);
            double r = loading.ZX(pos, range.get());
            return r + range.next(nx).dot(data.row(pos));
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            double v00 = loading.ZVZ(pos, v00(V));
            tmp.set(0);
            loading.ZM(pos, v01(V), tmp);
            double v01 = tmp.dot(data.row(pos));
            double v11 = QuadraticForm.apply(v11(V), data.row(pos));
            return v00 + 2 * v01 + v11;
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            if (d == 0)
                return;
            loading.VpZdZ(pos, v00(V), d);
            FastMatrix v01 = v01(V);
            FastMatrix v10=v10(V), v11=v11(V);
            DataBlockIterator cols = v01.columnsIterator();
            DataBlock xrow = data.row(pos);
            DoubleSeqCursor x = xrow.cursor();
            while (cols.hasNext()) {
                loading.XpZd(pos, cols.next(), d * x.getAndNext());
            }
            v10.copyTranspose(v01);
            v11.addXaXt(d, xrow);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            DataWindow range = x.left();
            loading.XpZd(pos, range.next(n), d);
            range.next(nx).addAY(d, data.row(pos));
        }

    }

}
