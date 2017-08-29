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
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;
import demetra.data.DoubleReader;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class RegSsf extends Ssf {

    public static RegSsf create(ISsf model, Matrix X) {
        int mdim = model.getStateDim();
        Xinitializer xinit = new Xinitializer(mdim, model.getInitialization(), X.getColumnsCount());
        Xdynamics xdyn = new Xdynamics(mdim, model.getDynamics(), X.getColumnsCount());
        Xmeasurement xm = new Xmeasurement(mdim, model.getMeasurement(), X);
        return new RegSsf(xinit, xdyn, xm);
    }

    private RegSsf(ISsfInitialization initializer, ISsfDynamics dyn, ISsfMeasurement m) {
        super(initializer, dyn, m);
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

    static class Xinitializer implements ISsfInitialization {

        private final int n, nx;
        private final ISsfInitialization dyn;

        Xinitializer(int n, ISsfInitialization init, int nx) {
            this.dyn = init;
            this.n = n;
            this.nx = nx;
        }

        @Override
        public boolean isValid() {
            return nx > 0;
        }

        @Override
        public int getStateDim() {
            return n+ nx;
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

    static class Xmeasurement implements ISsfMeasurement {

        private final ISsfMeasurement m;
        private final Matrix data;
        private final int n, nx;
        private final DataBlock tmp;

        private Xmeasurement(final int n, final ISsfMeasurement m, final Matrix data) {
            this.data = data;
            this.m = m;
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
            m.Z(pos, range.get());
            range.next(nx).copy(data.row(pos));
        }

        @Override
        public boolean hasErrors() {
            return m.hasErrors();
        }
        
       @Override
        public boolean areErrorsTimeInvariant() {
            return m.areErrorsTimeInvariant();
        }

        @Override
        public boolean hasError(int pos) {
            return m.hasError(pos);
        }

        @Override
        public double errorVariance(int pos) {
            return m.errorVariance(pos);
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            DataWindow range = x.window(0, n);
            double r = m.ZX(pos, range.get());
            return r + range.next(nx).dot(data.row(pos));
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            MatrixWindow v = V.topLeft(n, n);
            double v00 = m.ZVZ(pos, v);
            v.hnext(nx);
            tmp.set(0);
            m.ZM(pos, v, tmp);
            double v01 = tmp.dot(data.row(pos));
            v.vnext(nx);
            double v11 = QuadraticForm.apply(v, data.row(pos));
            return v00 + 2 * v01 + v11;
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            MatrixWindow v = V.topLeft(n, n);
            m.VpZdZ(pos, v, d);
            MatrixWindow vtmp = v.clone();
            vtmp.hnext(nx);
            v.vnext(nx);
            DataBlockIterator rows = v.rowsIterator();
            DataBlock xrow = data.row(pos);
            DoubleReader x = xrow.reader();
            while (rows.hasNext()) {
                m.XpZd(pos, rows.next(), d * x.next());
            }
            vtmp.copy(v.transpose());
            v.hnext(nx);
            v.addXaXt(d, xrow);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            DataWindow range = x.window(0, n);
            m.XpZd(pos, range.get(), d);
            range.next(nx).addAY(d, data.row(pos));
        }

    }

}
