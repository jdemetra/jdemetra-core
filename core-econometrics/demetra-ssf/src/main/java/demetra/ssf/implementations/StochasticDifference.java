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
package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class StochasticDifference extends Ssf {

    public static StochasticDifference create(ISsf stmodel, Polynomial ur) {
        double[] coef = ur.getCoefficients();
        int n = 0;
        for (int i = 1; i < coef.length; ++i) {
            if (coef[i] != 0) {
                ++n;
            }
        }
        double[] dw = new double[n];
        int[] dpos = new int[n];
        for (int i = 1, j = 0; i < coef.length; ++i) {
            if (coef[i] != 0) {
                dw[j] = -coef[i];
                dpos[j++] = i - 1;
            }
        }
        Dynamics dyn = new Dynamics(stmodel.getDynamics(), stmodel.getMeasurement(), dpos, dw, ur.getDegree());
        Measurement m = new Measurement(stmodel.getMeasurement(), dpos, dw, ur.getDegree(), stmodel.getStateDim());
        return new StochasticDifference(dyn, m, ur);
    }

    private final Polynomial difference;

    private StochasticDifference(ISsfDynamics dyn, ISsfMeasurement m, Polynomial difference) {
        super(dyn, m);
        this.difference = difference;
    }

    public Polynomial getDifferencing() {
        return difference;
    }

    static class Dynamics implements ISsfDynamics {

        private final ISsfDynamics dyn;
        private final ISsfMeasurement m;
        private final int[] dpos;
        private final double[] dw;
        private final int ddim, sdim, dim;
        private final double[] tmp;

        Dynamics(final ISsfDynamics dyn, final ISsfMeasurement m,
                final int[] dpos, final double[] dw, final int ddim) {
            this.dyn = dyn;
            this.m = m;
            this.dpos = dpos;
            this.dw = dw;
            this.ddim = ddim;
            this.sdim = dyn.getStateDim();
            this.dim = ddim + sdim;
            this.tmp = new double[dpos.length];
        }

        @Override
        public int getStateDim() {
            return dim;
        }

        @Override
        public boolean isTimeInvariant() {
            return dyn.isTimeInvariant();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            return dyn.getInnovationsDim();
        }

        @Override
        public void V(int pos, Matrix qm) {
            dyn.V(pos, qm.extract(ddim, dim, ddim, dim));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return dyn.hasInnovations(pos);
        }

        @Override
        public void S(int pos, Matrix sm) {
            dyn.S(pos, sm.extract(ddim, dim, 0, dyn.getInnovationsDim()));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            dyn.addSU(pos, x.range(ddim, x.length()), u);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            dyn.XS(pos, x.range(ddim, x.length()), xs);
        }

        @Override
        public void T(int pos, Matrix tr) {
            DataBlock row = tr.row(0);
            for (int i = 0; i < dpos.length; ++i) {
                row.set(dpos[i], dw[i]);
            }
            m.Z(pos, row.range(ddim, dim));
            MatrixWindow cur = tr.topLeft(ddim, ddim);
            cur.subDiagonal(-1).set(1);
            cur.next(sdim, sdim);
            dyn.T(pos, cur);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return ddim;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return dyn.a0(a0.range(ddim, dim));
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            return dyn.Pf0(pf0.extract(ddim, dim, ddim, dim));
        }

        @Override
        public void TX(int pos, DataBlock x) {
            DataWindow sx = x.window(ddim, dim);
            double s = m.ZX(pos, sx.get());
            dyn.TX(pos, sx.get());
            for (int i = 0; i < dpos.length; ++i) {
                s += x.get(dpos[i]) * dw[i];
            }
            sx.previous(ddim).fshift(1);
            x.set(0, s);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            double x0 = x.get(0);
            DataBlock dx = x.range(0, ddim);
            dx.bshiftAndZero();
            for (int i = 0; i < dpos.length; ++i) {
                dx.add(dpos[i], x0 * dw[i]);
            }
            DataBlock sx = x.range(ddim, dim);
            dyn.XT(pos, sx);
            m.XpZd(pos, sx, x0);
        }

        @Override
        public void addV(int pos, Matrix p) {
            dyn.addV(pos, p.extract(ddim, dim, ddim, dim));
        }
    }

    static class Measurement implements ISsfMeasurement {

        private final ISsfMeasurement m;
        private final int[] dpos;
        private final double[] dw;
        private final int ddim, sdim, dim;

        Measurement(final ISsfMeasurement m,
                final int[] dpos, final double[] dw, final int ddim, final int sdim) {
            this.m = m;
            this.dpos = dpos;
            this.dw = dw;
            this.ddim = ddim;
            this.sdim = sdim;
            this.dim = ddim + sdim;
        }

        @Override
        public boolean isTimeInvariant() {
            return m.isTimeInvariant();
        }

        @Override
        public void Z(int pos, DataBlock z) {
            for (int i = 0; i < dpos.length; ++i) {
                z.set(dpos[i], dw[i]);
            }
            m.Z(pos, z.range(ddim, dim));
        }

        @Override
        public boolean hasErrors() {
            return m.hasErrors();
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
            double zx = m.ZX(pos, x.range(ddim, dim));
            for (int i = 0; i < dpos.length; ++i) {
                zx += x.get(dpos[i]) * dw[i];
            }
            return zx;
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            // (d z)(v11 v12)(d)
            //      (v21 v22)(z)
            // = (d*v11+z*v21)*d'+(d*v12+z*v22)*z' 
            // = d*v11*d' + z*v22*z' + 2*zv21*d'
            MatrixWindow cur = V.topLeft();
            cur.next(ddim, ddim);
            double zvz = 0;
            for (int i = 0; i < dpos.length; ++i) {
                for (int j = 0; j < dpos.length; ++j) {
                    zvz += cur.get(dpos[i], dpos[j]) * dw[i] * dw[j];
                }
            }
            cur.vnext(sdim);
            for (int i = 0; i < dpos.length; ++i) {
                zvz += 2 * m.ZX(pos, cur.column(dpos[i])) * dw[i];
            }
            cur.hnext(sdim);
            zvz += m.ZVZ(pos, cur);
            return zvz;
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double k) {
            // V+=(d')*k*(d,z)
            //    (z')
            // V+=(d'*k*d d'*k*z)
            //    (z'*k*d z'*k*z)
            MatrixWindow cur = V.topLeft();
            cur.next(ddim, ddim);
            for (int i = 0; i < dpos.length; ++i) {
                for (int j = 0; j < dpos.length; ++j) {
                    cur.add(dpos[i], dpos[j], k * dw[i] * dw[j]);
                }
            }
            MatrixWindow ccur = cur.clone();
            ccur.hnext(sdim);
            cur.vnext(sdim);
            for (int i = 0; i < dpos.length; ++i) {
                m.XpZd(pos, cur.column(dpos[i]), dw[i] * k);
            }
            ccur.copy(cur.transpose());
            cur.hnext(sdim);
            m.VpZdZ(pos, cur, k);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            for (int i = 0; i < dpos.length; ++i) {
                x.add(dpos[i], d * dw[i]);
            }
            m.XpZd(pos, x.range(ddim, dim), d);
        }

        @Override
        public int getStateDim() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
