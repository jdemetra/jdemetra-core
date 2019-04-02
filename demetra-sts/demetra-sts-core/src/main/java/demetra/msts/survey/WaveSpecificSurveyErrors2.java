/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.survey;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.StateComponent;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class WaveSpecificSurveyErrors2 {

    public StateComponent of(double[] v, double[][] ar, int lag) {
        Data info = new Data(v, ar, lag);
        return new StateComponent(new Initialization(info), new Dynamics(info));
    }


    @lombok.Value
    static class Data {

        double[] v;
        double[][] ar;
        int lag;


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

    static class Dynamics implements ISsfDynamics {

        private final Data info;
        private double[] tmp;

        Dynamics(Data info) {
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

    static class Initialization implements ISsfInitialization {

        private final Data info;

        Initialization(Data info) {
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
            Dynamics dyn = new Dynamics(info);
            dyn.addV(0, pf0);
            for (int i = 1; i < info.nwaves(); ++i) {
                dyn.TVT(i, pf0);
                dyn.addV(0, pf0);
            }
        }
    }

}
