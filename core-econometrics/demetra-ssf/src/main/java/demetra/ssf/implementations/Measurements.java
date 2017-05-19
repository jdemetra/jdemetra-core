/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
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

import demetra.data.CellReader;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.univariate.ISsf;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class Measurements {

    public static ISsfMeasurements create(final int dim, final int[] mpos, final double[] var) {
        return new Measurements1(dim, mpos, var);
    }

    public static ISsfMeasurements create(final int dim, final int[] mpos) {
        return new Measurements1(dim, mpos, null);
    }

    public static ISsfMeasurements proxy(ISsfMeasurement m) {
        return new Proxy(m);
    }

    public static ISsfMeasurements of(ISsf... ssf) {
        ISsfMeasurement[] m = new ISsfMeasurement[ssf.length];
        int dim = ssf[0].getStateDim();
        m[0] = ssf[0].getMeasurement();
        for (int i = 1; i < ssf.length; ++i) {
            if (dim != ssf[0].getStateDim()) {
                return null;
            }
            m[i] = ssf[i].getMeasurement();
        }
        return new MeasurementsVector(m, dim);
    }

    public static ISsfMeasurements create(ISsfMeasurements measurements, DataBlock var) {
        if (measurements.getMaxCount() != var.length() || !measurements.isHomogeneous()) {
            return null;
        }
        return new Extension(measurements, DataBlock.copyOf(var));
    }

    public static ISsfMeasurements create(ISsfMeasurement[] m, int dim) {
        return new MeasurementsVector(m, dim);
    }

    static class Proxy implements ISsfMeasurements {

        private final ISsfMeasurement m_;

        Proxy(ISsfMeasurement m) {
            m_ = m;
        }

        @Override
        public boolean isTimeInvariant() {
            return m_.isTimeInvariant();
        }

        @Override
        public int getCount(int pos) {
            return 1;
        }

        @Override
        public int getMaxCount() {
            return 1;
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            m_.Z(pos, z);
        }

        @Override
        public boolean hasErrors() {
            return m_.hasErrors();
        }

        @Override
        public boolean hasIndependentErrors() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return m_.hasError(pos);
        }

        @Override
        public void H(int pos, Matrix h) {
            double v = m_.errorVariance(pos);
            if (v != 0) {
                h.set(0, 0, v);
            }
        }

        @Override
        public void R(int pos, Matrix r) {
            double v = m_.errorVariance(pos);
            if (v != 0) {
                r.set(0, 0, Math.sqrt(v));
            }
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return m_.ZX(pos, m);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
            return m_.ZVZ(pos, V);
        }

        @Override
        public void addH(int pos, Matrix V) {
            double v = m_.errorVariance(pos);
            if (v != 0) {
                V.add(0, 0, v);
            }
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
            m_.VpZdZ(pos, V, d);
        }

        @Override
        public void XpZd(int pos, int ivar, DataBlock x, double d) {
            m_.XpZd(pos, x, d);
        }

        @Override
        public int getStateDim() {
            return m_.getStateDim();
        }

        @Override
        public boolean isValid() {
            return m_.isValid();
        }
    }

    static class Extension implements ISsfMeasurements {

        private final Matrix E, R;
        private final DataBlock var, se;
        private final ISsfMeasurements details;

        Extension(final ISsfMeasurements loadings, final DataBlock var) {
            this.details = loadings;
            E = null;
            R = null;
            this.var = var;
            se = DataBlock.copyOf(var);
            se.apply(x -> Math.sqrt(x));
        }

        Extension(final ISsfMeasurements loadings, final Matrix V) {
            this.details = loadings;
            E = V.deepClone();
            R = E.deepClone();
            SymmetricMatrix.lcholesky(R, 1e-9);
            var = null;
            se = null;
        }

        Extension(final ISsfMeasurements loadings) {
            this.details = loadings;
            E = null;
            R = null;
            var = null;
            se = null;
        }

        @Override
        public boolean hasErrors() {
            return var == null && E == null;
        }

        @Override
        public boolean hasIndependentErrors() {
            return E == null;
        }

        @Override
        public boolean hasError(int pos) {
            return hasErrors();
        }

        @Override
        public void H(int pos, Matrix h) {
            if (E != null) {
                h.copy(E);
            } else if (var != null) {
                h.diagonal().copy(var);
            }
        }

        @Override
        public void R(int pos, Matrix r) {
            if (R != null) {
                r.copy(R);
            } else if (var != null) {
                r.diagonal().copy(se);
            }
        }

        @Override
        public void addH(int pos, Matrix V) {
            if (E != null) {
                V.add(E);
            } else if (var != null) {
                V.diagonal().add(var);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return details.isTimeInvariant();
        }

        @Override
        public int getCount(int pos) {
            return details.getCount(pos);
        }

        @Override
        public int getMaxCount() {
            return details.getMaxCount();
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            details.Z(pos, var, z);
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return details.ZX(pos, var, m);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
            return details.ZVZ(pos, ivar, jvar, V);
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
            details.VpZdZ(pos, ivar, jvar, V, d);
        }

        @Override
        public void XpZd(int pos, int ivar, DataBlock x, double d) {
            details.XpZd(pos, ivar, x, d);
        }

        @Override
        public int getStateDim() {
            return details.getStateDim();
        }

        @Override
        public boolean isValid() {
            return details.isValid();
        }
    }

    static class Measurements1 implements ISsfMeasurements {

        private final int sdim;
        private final int[] mpos_;
        private final double[] var;

        Measurements1(final int sdim, int[] mpos, double[] var) {
            this.sdim = sdim;
            mpos_ = mpos;
            this.var = var;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public int getCount(int pos) {
            return mpos_.length;
        }

        @Override
        public int getMaxCount() {
            return mpos_.length;
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            z.set(mpos_[var], 1);
        }

        @Override
        public boolean hasErrors() {
            return var != null;
        }

        @Override
        public boolean hasIndependentErrors() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            return var != null;
        }

        @Override
        public void H(int pos, Matrix h) {
            if (var != null) {
                h.diagonal().copyFrom(var, 0);
            }
        }

        @Override
        public void R(int pos, Matrix r) {
            if (var != null) {
                DataBlock diagonal = r.diagonal();
                diagonal.copyFrom(var, 0);
                diagonal.apply(x -> Math.sqrt(x));
            }
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return m.get(mpos_[var]);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
            return V.get(mpos_[ivar], mpos_[jvar]);
        }

        @Override
        public void addH(int pos, Matrix V) {
            if (var != null) {
                V.diagonal().add(DataBlock.ofInternal(var));
            }
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
            V.add(mpos_[ivar], mpos_[jvar], d);
        }

        @Override
        public void XpZd(int pos, int ivar, DataBlock x, double d) {
            x.add(mpos_[ivar], d);
        }

        @Override
        public int getStateDim() {
            return sdim;
        }

        @Override
        public boolean isValid() {
            for (int i = 0; i < mpos_.length; ++i) {
                if (mpos_[i] >= sdim) {
                    return false;
                }
            }
            return true;
        }
    }

    static class MeasurementsVector implements ISsfMeasurements {

        private final int sdim;
        private final ISsfMeasurement[] ms;
        private final DataBlock tmp;

        MeasurementsVector(ISsfMeasurement[] ms, int dim) {
            this.ms = ms;
            tmp = DataBlock.make(dim);
            sdim = dim;
        }

        @Override
        public boolean isTimeInvariant() {
            for (int i = 0; i < ms.length; ++i) {
                if (!ms[i].isTimeInvariant()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int getCount(int pos) {
            return ms.length;
        }

        @Override
        public int getMaxCount() {
            return ms.length;
        }

        @Override
        public boolean isHomogeneous() {
            return true;
        }

        @Override
        public void Z(int pos, int var, DataBlock z) {
            ms[var].Z(pos, z);
        }

        @Override
        public boolean hasErrors() {
            for (int i = 0; i < ms.length; ++i) {
                if (ms[i].hasErrors()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasIndependentErrors() {
            return true;
        }

        @Override
        public boolean hasError(int pos) {
            for (int i = 0; i < ms.length; ++i) {
                if (ms[i].hasError(pos)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void H(int pos, Matrix h) {
            DataBlock diagonal = h.diagonal();
            for (int i = 0; i < ms.length; ++i) {
                diagonal.set(ms[i].errorVariance(pos));
            }
        }

        @Override
        public void R(int pos, Matrix r) {
            DataBlock diagonal = r.diagonal();
            for (int i = 0; i < ms.length; ++i) {
                double v = ms[i].errorVariance(pos);
                if (v != 0) {
                    diagonal.set(Math.sqrt(v));
                }
            }
        }

        @Override
        public double ZX(int pos, int var, DataBlock m) {
            return ms[var].ZX(pos, m);
        }

        @Override
        public double ZVZ(int pos, int ivar, int jvar, Matrix V) {
            if (ivar == jvar) {
                return ms[ivar].ZVZ(pos, V);
            } else {
                tmp.set(0);
                ms[ivar].ZM(pos, V, tmp);
                return ms[jvar].ZX(pos, tmp);
            }
        }

        @Override
        public void addH(int pos, Matrix V) {
            DataBlock diagonal = V.diagonal();
            for (int i = 0; i < ms.length; ++i) {
                diagonal.add(ms[i].errorVariance(pos));
            }
        }

        @Override
        public void VpZdZ(int pos, int ivar, int jvar, Matrix V, double d) {
            if (d == 0) {
                return;
            }
            if (ivar == jvar) {
                ms[ivar].VpZdZ(pos, V, d);
            } else {
                tmp.set(0);
                Z(pos, jvar, tmp);
                DataBlockIterator columns = V.columnsIterator();
                CellReader cell = tmp.reader();
                while (columns.hasNext()) {
                    ms[ivar].XpZd(pos, columns.next(), cell.next() * d);
                }
            }
        }

        @Override
        public void XpZd(int pos, int ivar, DataBlock x, double d) {
            ms[ivar].XpZd(pos, x, d);
        }

        @Override
        public int getStateDim() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }
    }
}
