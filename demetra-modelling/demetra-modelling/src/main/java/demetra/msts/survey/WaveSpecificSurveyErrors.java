/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.survey;

import demetra.arima.AutoCovarianceFunction;
import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.maths.polynomials.Polynomial;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.SsfComponent;
import demetra.ssf.StateComponent;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class WaveSpecificSurveyErrors {

    public StateComponent of(double ar11, double ar21, double ar22, int nwaves) {
        Data info = new Data(ar11, ar21, ar22, nwaves);
        return new StateComponent(new Initialization(info), new Dynamics(info));
    }

    public StateComponent of(double[][] ar, int lag) {
        Data2 info = new Data2(ar, lag);
        return new StateComponent(new Initialization2(info), new Dynamics2(info));
    }

    @lombok.Value
    static class Data {

        double ar11, ar21, ar22;
        int nwaves;
        double v1, v2;

        Data(double ar11, double ar21, double ar22, int nwaves) {
            this.ar11 = ar11;
            this.ar21 = ar21;
            this.ar22 = ar22;
            this.nwaves = nwaves;
            this.v1 = 1 - ar11 * ar11;
            double mar22 = 1 - ar22, par22 = 1 + ar22;
            this.v2 = par22 / mar22 * (mar22 * mar22 - ar21 * ar21);
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data info;

        Dynamics(Data info) {
            this.info = info;
        }

        @Override
        public int getInnovationsDim() {
            return info.nwaves;
        }

        @Override
        public void V(int pos, Matrix qm) {
            DataBlock d = qm.diagonal();
            d.set(0, 1);
            d.set(1, info.v1);
            d.range(2, info.nwaves).set(info.v2);
        }

        @Override
        public void S(int pos, Matrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        public void T(int pos, Matrix tr) {
            DataBlock d = tr.subDiagonal(-1);
            d.set(0, info.ar11);
            d.range(1, info.nwaves - 1).set(info.ar21);
            tr.subDiagonal(info.nwaves - 2).range(2, info.nwaves).set(info.ar22);
            tr.subDiagonal(-info.nwaves).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            for (int i = info.nwaves - 1; i > 1; --i) {
                double x1 = x.get(i - 1);
                double x2 = x.get(i + info.nwaves - 2);
                double z = x1 * info.ar21 + x2 * info.ar22;
                x.set(i + info.nwaves, x.get(i));
                x.set(i, z);
            }
            // first two iterations
            double z = x.get(0) * info.ar11;
            x.set(1 + info.nwaves, x.get(1));
            x.set(1, z);
            x.set(info.nwaves, x.get(0));
            x.set(0, 0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, Matrix p) {
            DataBlock d = p.diagonal();
            d.add(0, 1);
            d.add(1, info.v1);
            d.range(2, info.nwaves).set(info.v2);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            // first items
            int last = 2 * info.nwaves - 1;
            x.set(0, info.ar11 * x.get(1) + x.get(info.nwaves));
            x.set(1, info.ar21 * x.get(2) + x.get(info.nwaves + 1));
            for (int i = 2; i < info.nwaves - 1; ++i) {
                double z = x.get(i);
                x.set(i, info.ar21 * x.get(i + 1) + x.get(i + info.nwaves));
                x.set(i + info.nwaves - 2, info.ar22 * z);
            }
            double z = x.get(info.nwaves - 1);
            x.set(info.nwaves - 1, x.get(last));
            x.set(last - 2, info.ar22 * z);
            x.set(last - 1, 0);
            x.set(last, 0);
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    static class Initialization implements ISsfInitialization {

        private final Data info;

        Initialization(Data info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return 2 * info.nwaves;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pi0(Matrix pi0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            int n = info.nwaves;
            pf0.diagonal().set(1);
            pf0.extract(0, n, n, 2 * n).subDiagonal(-1).set(info.ar11);
            pf0.extract(n, 2 * n, 0, n).subDiagonal(1).set(info.ar11);
        }
    }

    @lombok.Value
    static class Data2 {

        double[][] ar;
        int lag;
        double[] v, c;

        Data2(double[][] ar, int lag) {
            this.ar = ar;
            this.lag = lag;
            // compute the variances
            this.v = var(ar);
            this.c = cor(ar);
        }

        /**
         * Variances corresponding to the ar coefficients
         *
         * @param ar
         * @return
         */
        static double[] var(double[][] ar) {
            double[] v = new double[ar.length];
            for (int i = 0; i < v.length; ++i) {
                switch (ar[i].length) {
                    case 0:
                        v[i] = 1;
                        break;
                    case 1:
                        double c = ar[i][0];
                        v[i] = 1 / (1 - c * c);
                        break;
                    default:
                        v[i] = acf(ar[i]).get(0);
                }

            }
            return v;
        }

        /**
         * Gets the maximum number of auto-regressive terms (= max degree)
         *
         * @return
         */
        int nar() {
            int n = 0;
            for (int i = 0; i < ar.length; ++i) {
                int nn = ar[i].length;
                if (nn > n) {
                    n = nn;
                }
            }
            return n;
        }

        /**
         * Dimension of the state vector
         *
         * @return
         */
        int dim() {
            return lag * ar.length * nar();
        }

        /**
         * Gets the number of waves
         *
         * @return
         */
        int nwaves() {
            return ar.length;
        }

        DataBlock v() {
            return DataBlock.ofInternal(v);
        }

        // 
        /**
         * Put in d the ar-coef of degree j, starting with wave j (first wave is
         * 0)
         *
         * @param d The buffer that will contain the coefficients. Its length
         * should be nwaves()-j
         * @param j The degree or wave identifier
         */
        void fillAr(DataBlock d, int j) {
            for (int k = j; k < ar.length; ++k) {
                double[] cur = ar[k];
                if (j <= cur.length) {
                    d.set(k - j, cur[j - 1]);
                }
            }
        }

        /**
         * Gets the ar-coeff for the given wave and degree
         *
         * @param wave
         * @param degree
         * @return
         */
        double ar(int wave, int degree) {
            double[] cur = ar[wave];
            if (cur.length < degree) {
                return 0;
            } else {
                return cur[degree - 1];
            }
        }
    }

    static double[] cor(double[][] ar) {
        double[] c = new double[ar.length];
        for (int i = 0; i < c.length; ++i) {
            c[i] = acf(ar[i]).get(0);
        }
        return c;
    }

    Polynomial ar(double[] ar) {
        double[] c = new double[1 + ar.length];
        c[0] = 1;
        for (int i = 1; i < c.length; ++i) {
            c[i] = -ar[i - 1];
        }
        return Polynomial.ofInternal(c);
    }

    AutoCovarianceFunction acf(double[] ar) {
        return new AutoCovarianceFunction(Polynomial.ONE, ar(ar), 1);
    }

    static class Dynamics2 implements ISsfDynamics {

        private final Data2 info;
        private double[] tmp;

        Dynamics2(Data2 info) {
            this.info = info;
            tmp = new double[info.nwaves()];
        }

        @Override
        public int getInnovationsDim() {
            return info.nwaves();
        }

        @Override
        public void V(int pos, Matrix qm) {
            DataBlock d = qm.diagonal().range(0, info.nwaves());
            d.copyFrom(info.v, 0);
        }

        @Override
        public void S(int pos, Matrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        public void T(int pos, Matrix tr) {
            int k = info.nwaves() * (info.lag - 1);
            int nar = info.nar();
            for (int j = 1; j <= nar; ++j, k += info.lag * info.nwaves()) {
                int dstart = k - j;
                DataBlock d = tr.subDiagonal(dstart);
                if (dstart >= 0) {
                    d = d.range(j, info.nwaves());
                } else {
                    d = d.range(0, info.nwaves() + dstart);
                }
                info.fillAr(d, j);
            }
            tr.subDiagonal(-info.nwaves()).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {

            // first iterations
            int start = info.nwaves() * (info.lag - 1), del = info.nwaves() * info.lag - 1;
            int nar = info.nar();
            for (int j = 0; j < info.nwaves(); ++j) {
                int d = Math.min(nar, j);
                double s = 0;
                for (int k = 1, l = start + j - 1; k <= d; ++k, l += del) {
                    s += x.get(l) * info.ar(j, k);
                }
                tmp[j] = s;
            }
            // shift operations
            x.fshift(info.nwaves());
            x.range(0, info.nwaves()).copyFrom(tmp, 0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, Matrix p) {
            DataBlock d = p.diagonal().range(0, info.nwaves());
            d.add(info.v());
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int nwaves = info.nwaves();
            x.range(0, nwaves).copyTo(tmp, 0); // we save the first n waves items
            x.bshift(nwaves);
            int n = info.dim();
            x.range(n - nwaves, n).set(0);
            int k = nwaves * (info.lag - 1), dk = nwaves * info.lag;
            int nar = info.nar();
            for (int j = 1; j <= nar; ++j, k += dk) {
                for (int l = j, m = k; l < nwaves; ++l, ++m) {
                    x.add(m, tmp[l] * info.ar(l, j));
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

    static class Initialization2 implements ISsfInitialization {

        private final Data2 info;

        Initialization2(Data2 info) {
            this.info = info;
        }

        @Override
        public int getStateDim() {
            return info.dim();
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pi0(Matrix pi0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            pf0.diagonal().set(1);
//            int d = info.ar.length;
//            int m = info.lag * info.ar.length;
//            for (int i = 0; i < info.) {
//                
//            }
        }
    }

}
