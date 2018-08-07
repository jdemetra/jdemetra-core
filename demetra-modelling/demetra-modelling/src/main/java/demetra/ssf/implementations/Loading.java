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

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.multivariate.ISsfMeasurements;
import java.util.Iterator;
import demetra.data.DataBlockIterator;
import demetra.ssf.ISsfLoading;

/**
 *
 * @author Jean Palate
 */
public class Loading {

    public static ISsfLoading create(final int mpos) {
        return new Loading1(mpos);
    }

    public static ISsfLoading createLoading(final int mpos, final double b) {
        return b == 1 ? new Loading1(mpos) : new Loading1l(mpos, b);
    }

    public static ISsfLoading createSum() {
        return new SumLoading();
    }

    public static ISsfLoading createPartialSum(final int n) {
        return new PartialSumLoading(n);
    }

    public static ISsfLoading createExtractor(final int i0, final int n, final int inc) {
        return new ExtractorLoading(n, inc, n);
    }

    public static ISsfLoading create(final int[] mpos) {
        return new Loading2(mpos);
    }

    public static ISsfLoading circular(final int period) {
        return new CircularLoading(period, 0);
    }

    public static ISsfLoading circular(final int period, final int pstart) {
        return new CircularLoading(period, pstart);
    }

    public static ISsfLoading cyclical(final int period, final int dim) {
        return new CyclicalLoading(period, 0);
    }

    public static ISsfLoading cyclical(final int period, final int pstart, final int dim) {
        return new CyclicalLoading(period, pstart);
    }

    private static class SumLoading implements ISsfLoading {

        SumLoading() {
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.sum();
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.set(m.row(0), m.row(1), (x, y) -> x + y);
            for (int r = 2; r < m.getRowsCount(); ++r) {
                zm.add(m.row(r));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return V.sum();
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            V.add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(1);
        }

    }

    private static class PartialSumLoading implements ISsfLoading {

        private final int cdim;

        PartialSumLoading(int n) {
            this.cdim = n;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.extract(0, cdim).sum();
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.set(m.row(0), m.row(1), (x, y) -> x + y);
            for (int r = 2; r < cdim; ++r) {
                zm.add(m.row(r));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return V.topLeft(cdim, cdim).sum();
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            V.topLeft(cdim, cdim).add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.extract(0, cdim).add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.extract(0, cdim).set(1);
        }

    }

    private static class ExtractorLoading implements ISsfLoading {

        private final int i0, n, inc;

        ExtractorLoading(int i0, int n, int inc) {
            this.i0 = i0;
            this.n = n;
            this.inc = inc;
        }

        /**
         * Selects specific columns
         *
         * @param m
         * @return
         */
        private Matrix columnExtract(Matrix m) {
            return m.extract(0, i0, m.getRowsCount(), n, 1, inc);
        }

        /**
         * Selects specific rows
         *
         * @param m
         * @return
         */
        private Matrix rowExtract(Matrix m) {
            return m.extract(i0, 0, n, m.getColumnsCount(), inc, 1);
        }

        private Matrix extract(Matrix v) {
            return v.extract(i0, i0, n, n, inc, inc);
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.extract(i0, n, inc).sum();
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            DataBlockIterator rows = rowExtract(m).rowsIterator();
            zm.copy(rows.next());
            while (rows.hasNext()) {
                zm.add(rows.next());
            }
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return extract(V).sum();
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            extract(V).add(d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.extract(i0, n, inc).add(d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.extract(i0, n, inc).set(1);
        }

    }

    private static class Loading1 implements ISsfLoading {

        private final int mpos;

        Loading1(int mpos) {
            this.mpos = mpos;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return m.get(mpos);
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.copy(m.row(mpos));
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return V.get(mpos, mpos);
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            V.add(mpos, mpos, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(mpos, d);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(mpos, 1);
        }

    }

    private static class Loading1l implements ISsfLoading {

        private final int mpos;
        private final double b, b2;

        Loading1l(int mpos, double b) {
            this.mpos = mpos;
            this.b = b;
            this.b2 = b * b;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return b * m.get(mpos);
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.setAY(b, m.row(mpos));

        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            return b2 * V.get(mpos, mpos);
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            V.add(mpos, mpos, d * b2);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            x.add(mpos, d * b);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            z.set(mpos, b);
        }

    }

    private static class Loading2 implements ISsfLoading {

        private final int[] mpos;

        Loading2(int[] mpos) {
            this.mpos = mpos;
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            int n = mpos.length;
            double d = m.get(mpos[0]);
            for (int i = 1; i < n; ++i) {
                d += m.get(mpos[i]);
            }
            return d;
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock zm) {
            zm.copy(m.row(mpos[0]));
            for (int i = 1; i < mpos.length; ++i) {
                zm.add(m.row(mpos[i]));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix V) {
            double d = 0;
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                d += V.get(mpos[i], mpos[i]);
                for (int j = 0; j < i; ++j) {
                    d += 2 * V.get(mpos[i], mpos[j]);
                }
            }
            return d;
        }

        @Override
        public void VpZdZ(int pos, Matrix V, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    V.add(mpos[i], mpos[j], d);
                }
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                x.add(mpos[i], d);
            }
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int n = mpos.length;
            for (int i = 0; i < n; ++i) {
                z.set(mpos[i], 1);
            }
        }

    }

    static class CircularLoading implements ISsfLoading {

        private final int period, start;

        public CircularLoading(int period, int start) {
            this.period = period;
            this.start = start;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                z.set(-1);
            } else {
                z.set(spos, 1);
            }
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                return -x.sum();
            } else {
                return x.get(spos);
            }
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock x) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                for (int i = 0; i < x.length(); ++i) {
                    x.set(i, -m.column(i).sum());
                }
            } else {
                x.copy(m.row(spos));
            }
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                return vm.sum();
            } else {
                return vm.get(spos, spos);
            }
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                vm.add(d);
            } else {
                vm.add(spos, spos, d);
            }
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            if (spos == period - 1) {
                x.add(-d);
            } else {
                x.add(spos, d);
            }
        }
    }

    static class CyclicalLoading implements ISsfLoading {

        private final int period, start;

        public CyclicalLoading(int period, int start) {
            this.period = period;
            this.start = start;
        }

        @Override
        public boolean isTimeInvariant() {
            return false;
        }

        @Override
        public void Z(int pos, DataBlock z) {
            int spos = (start + pos) % period;
            z.set(spos, 1);
        }

        @Override
        public double ZX(int pos, DataBlock x) {
            int spos = (start + pos) % period;
            return x.get(spos);
        }

        @Override
        public void ZM(int pos, Matrix m, DataBlock x) {
            int spos = (start + pos) % period;
            x.copy(m.row(spos));
        }

        @Override
        public double ZVZ(int pos, Matrix vm) {
            int spos = (start + pos) % period;
            return vm.get(spos, spos);
        }

        @Override
        public void VpZdZ(int pos, Matrix vm, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            vm.add(spos, spos, d);
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            if (d == 0) {
                return;
            }
            int spos = (start + pos) % period;
            x.add(spos, d);
        }

    }
}
