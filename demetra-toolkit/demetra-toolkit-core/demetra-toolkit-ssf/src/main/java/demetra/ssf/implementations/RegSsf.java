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
package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.maths.matrices.QuadraticForm;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.SsfException;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Measurement;

/**
 * SSF extended by regression variables with fixed or time varying coefficients.
 * Time varying coefficients follow a multi-variate random walk.
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegSsf {

    public SsfComponent of(Matrix X) {
        int nx = X.getColumnsCount();
        return new SsfComponent(new ConstantInitialization(nx), new ConstantDynamics(), Loading.regression(X));
    }

    public SsfComponent ofTimeVarying(Matrix X, double var) {
        int nx = X.getColumnsCount();
        return new SsfComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(X.getColumnsCount(), var), Loading.regression(X));
    }

    public SsfComponent ofTimeVarying(Matrix X, DoubleSequence vars) {
        int nx = X.getColumnsCount();
        if (vars.length() == 1) {
            return new SsfComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(nx, vars.get(0)), Loading.regression(X));
        } else if (nx == vars.length()) {
            return new SsfComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(vars), Loading.regression(X));
        } else {
            throw new SsfException(SsfException.MODEL);
        }
    }

    public SsfComponent ofTimeVarying(Matrix X, Matrix vars) {
        int nx = X.getColumnsCount();
        return new SsfComponent(new ConstantInitialization(nx), TimeVaryingDynamics.of(vars), Loading.regression(X));
    }

    public ISsf of(ISsf model, Matrix X) {
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = model.getStateDim();
        return Ssf.of(new Xinitializer(model.initialization(), X.getColumnsCount()),
                new Xdynamics(mdim, model.dynamics(), X.getColumnsCount()),
                new Xloading(mdim, model.loading(), X), model.measurementError());
    }

    public SsfComponent of(SsfComponent model, Matrix X) {
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = model.initialization().getStateDim();
        return new SsfComponent(new Xinitializer(model.initialization(), X.getColumnsCount()),
                new Xdynamics(mdim, model.dynamics(), X.getColumnsCount()),
                new Xloading(mdim, model.loading(), X));
    }

    /**
     * Creates a ssf with time varying coefficients, such that the innovations
     * covariance are defined by cvar
     *
     * @param model
     * @param X
     * @param cvar The covariance of the coefficients
     * @return
     */
    public ISsf ofTimeVarying(ISsf model, Matrix X, Matrix cvar) {
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = model.getStateDim();
        Matrix s = cvar.deepClone();
        SymmetricMatrix.lcholesky(s, 1e-12);
        return Ssf.of(new Xinitializer(model.initialization(), X.getColumnsCount()),
                new Xvardynamics(mdim, model.dynamics(), cvar, s),
                new Xloading(mdim, model.loading(), X), model.measurementError());
    }

    /**
     * Creates a ssf with time varying coefficients, such that the innovations
     * covariance are defined by SS'
     *
     * @param model
     * @param X
     * @param s The Cholesky factor of the covariance of the coefficients
     * @return
     */
    public ISsf ofTimeVaryingFactor(ISsf model, Matrix X, Matrix s) {
        if (X.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int mdim = model.getStateDim();
        Matrix var = SymmetricMatrix.XXt(s);
        return Ssf.of(new Xinitializer(model.initialization(), X.getColumnsCount()),
                new Xvardynamics(mdim, model.dynamics(), var, s),
                new Xloading(mdim, model.loading(), X), model.measurementError());
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
        public void V(int pos, Matrix qm) {
            dyn.V(pos, qm.topLeft(n, n));
        }

        @Override
        public void S(int pos, Matrix cm) {
            dyn.S(pos, cm.top(n));
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
        public void T(int pos, Matrix tr) {
            dyn.T(pos, tr.topLeft(n, n));
            tr.diagonal().drop(n, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dyn.TX(pos, x.range(0, n));
        }

        @Override
        public void TM(int pos, Matrix m) {
            dyn.TM(pos, m.top(n));
        }

        @Override
        public void TVT(int pos, Matrix m) {
            MatrixWindow z = m.topLeft(n, n);
            dyn.TVT(pos, z);
            MatrixWindow zc = z.clone();
            z.hnext(nx);
            dyn.TM(pos, z);
            zc.vnext(nx);
            zc.copy(z.transpose());
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
        public void addV(int pos, Matrix p) {
            dyn.addV(pos, p.topLeft(n, n));
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

    }

    static class Xvardynamics implements ISsfDynamics {

        private final int n, nx;
        private final ISsfDynamics dyn;
        private final Matrix var, s;

        Xvardynamics(int n, ISsfDynamics dyn, Matrix xvar, Matrix xs) {
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
        public void V(int pos, Matrix qm) {
            MatrixWindow cur = qm.topLeft(n, n);
            dyn.V(pos, cur);
            cur.next(nx, nx);
            cur.copy(var);
        }

        @Override
        public void S(int pos, Matrix cm) {
            MatrixWindow cur = cm.topLeft(n, dyn.getInnovationsDim());
            dyn.S(pos, cur);
            cur.next(nx, s.getColumnsCount());
            cur.copy(s);
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
        public void T(int pos, Matrix tr) {
            dyn.T(pos, tr.topLeft(n, n));
            tr.diagonal().drop(n, 0).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            dyn.TX(pos, x.range(0, n));
        }

        @Override
        public void TM(int pos, Matrix m) {
            dyn.TM(pos, m.top(n));
        }

        @Override
        public void TVT(int pos, Matrix m) {
            MatrixWindow z = m.topLeft(n, n);
            dyn.TVT(pos, z);
            MatrixWindow zc = z.clone();
            z.hnext(nx);
            dyn.TM(pos, z);
            zc.vnext(nx);
            zc.copy(z.transpose());
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
        public void addV(int pos, Matrix p) {
            MatrixWindow cur = p.topLeft(n, n);
            dyn.addV(pos, cur);
            cur.next(nx, nx);
            cur.add(var);
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
        public void diffuseConstraints(Matrix b) {
            int nd = dyn.getDiffuseDim();
            MatrixWindow tmp = b.topLeft(n, nd);
            if (nd > 0) {
                dyn.diffuseConstraints(tmp);
            }
            tmp.next(nx, nx);
            tmp.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
            dyn.a0(a0.range(0, n));
        }

        @Override
        public void Pf0(Matrix pf0) {
            dyn.Pf0(pf0.topLeft(n, n));
        }

        @Override
        public void Pi0(Matrix pi0) {
            MatrixWindow tmp = pi0.topLeft(n, n);
            dyn.Pi0(tmp);
            tmp.next(nx, nx);
            tmp.diagonal().set(1);
        }
    }

    static class Xloading implements ISsfLoading {

        private final ISsfLoading loading;
        private final Matrix data;
        private final int n, nx;
        private final DataBlock tmp;

        private Xloading(final int n, final ISsfLoading loading, final Matrix data) {
            this.data = data;
            this.loading = loading;
            this.n = n;
            nx = data.getColumnsCount();
            tmp = DataBlock.make(nx);
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
        public double ZVZ(int pos, Matrix V) {
            MatrixWindow v = V.topLeft(n, n);
            double v00 = loading.ZVZ(pos, v);
            v.hnext(nx);
            tmp.set(0);
            loading.ZM(pos, v, tmp);
            double v01 = tmp.dot(data.row(pos));
            v.vnext(nx);
            double v11 = QuadraticForm.apply(v, data.row(pos));
            return v00 + 2 * v01 + v11;
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            MatrixWindow v = V.topLeft(n, n);
            loading.VpZdZ(pos, v, d);
            MatrixWindow vtmp = v.clone();
            vtmp.hnext(nx);
            v.vnext(nx);
            DataBlockIterator rows = v.rowsIterator();
            DataBlock xrow = data.row(pos);
            DoubleReader x = xrow.reader();
            while (rows.hasNext()) {
                loading.XpZd(pos, rows.next(), d * x.next());
            }
            vtmp.copy(v.transpose());
            v.hnext(nx);
            v.addXaXt(d, xrow);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            DataWindow range = x.left();
            loading.XpZd(pos, range.next(n), d);
            range.next(nx).addAY(d, data.row(pos));
        }

    }

}
