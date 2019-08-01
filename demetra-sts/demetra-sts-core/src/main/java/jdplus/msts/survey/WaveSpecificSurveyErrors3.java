/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.survey;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateComponent;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import jdplus.maths.matrices.FastMatrix;

/**
 * Model described in the paper of Duncan Elliot
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class WaveSpecificSurveyErrors3 {

    /**
     *
     * @param v Variance of the innovations for each wave
     * @param ar Auto-regressive parameter (e(k, t)= rho[k-1]*e(k-1, t-lag)
     * @param k Scaling factor for the innovation variances (could be time
     * varying or fixed)
     * @param lag Lag between two observations in a given panel
     * @return
     */
    public StateComponent of(double[] v, double[] ar, Matrix k, int lag) {
        Data info = new Data(v, ar, k, lag);
        return new StateComponent(new Initialization(info), new Dynamics(info));
    }

    @lombok.Value
    static class Data {

        double[] v, vc;
        double[] ar;
        Matrix k;
        int lag;

        Data(double[] v, double[] ar, Matrix k, int lag) {
            this.v = v;
            this.ar = ar;
            this.lag = lag;
            vc = v.clone();
            if (k.getRowsCount() == 1) {
                vc[0] *= k.get(0, 0);
                for (int i = 1; i < vc.length; ++i) {
                    double rho = ar[i - 1];
                    vc[i] *= k.get(0, i) * (1 - rho * rho);
                }
                this.k = null;
            } else {
                this.k = k;
                for (int i = 1; i < vc.length; ++i) {
                    double rho = ar[i - 1];
                    vc[i] *= (1 - rho * rho);
                }
            }
        }

        /**
         * Dimension of the state vector
         *
         * @return
         */
        int dim() {
            return lag * v.length;
        }

        /**
         * Gets the number of waves
         *
         * @return
         */
        int nwaves() {
            return v.length;
        }

        double[] vc(int pos) {
            if (k == null) {
                return vc;
            } else {
                double[] kvc = vc.clone();
                DoubleSeqCursor reader = k(pos).cursor();
                for (int i = 0; i < kvc.length; ++i) {
                    kvc[i] *= reader.getAndNext();
                }
                return kvc;
            }
        }

        private DoubleSeq k(int pos) {
            if (pos < 0) {
                return k.row(0);
            } else if (pos >= k.getRowsCount()) {
                return k.row(k.getRowsCount() - 1);
            } else {
                return k.row(pos);
            }
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data info;
        private final double[] tmp;

        Dynamics(Data info) {
            this.info = info;
            tmp = new double[info.nwaves()];
        }

        @Override
        public int getInnovationsDim() {
            return info.nwaves();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            DataBlock d = qm.diagonal().range(0, info.nwaves());
            d.copyFrom(info.vc(pos), 0);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
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
        public void T(int pos, FastMatrix tr) {
            int nw = info.nwaves();
            int k = nw * (info.lag - 1);
            DataBlock d = tr.subDiagonal(k - 1);
            if (info.lag == 1) {
                d.range(0, nw - 1).copyFrom(info.ar, 0);
            } else {
                d.range(1, nw).copyFrom(info.ar, 0);
            }
            if (info.lag > 1) {
                tr.subDiagonal(-nw).set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            // first iterations
            int nw = info.nwaves();
            int start = nw * (info.lag - 1);
            tmp[0] = 0;
            for (int j = 1, k = start; j < nw; ++j, ++k) {
                tmp[j] = x.get(k) * info.ar[j - 1];
            }
            // shift operations
            x.fshift(nw);
            x.range(0, nw).copyFrom(tmp, 0);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            DataBlock d = p.diagonal().range(0, info.nwaves());
            d.add(DataBlock.of(info.vc(pos)));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int nwaves = info.nwaves();
            if (info.lag > 1) {
                x.range(0, nwaves).copyTo(tmp, 0); // we save the first n waves items
                x.bshift(nwaves);
                int k = nwaves * (info.lag - 1);
                for (int l = 1; l < nwaves; ++l, ++k) {
                    x.set(k, tmp[l] * info.ar[l - 1]);
                }
                x.set(k, 0);
            } else {
                for (int l = 1, m = 0; l < nwaves; ++l, ++m) {
                    x.set(m, x.get(l) * info.ar[m]);
                }
                x.set(nwaves - 1, 0);
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
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pi0(FastMatrix pi0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            Dynamics dyn = new Dynamics(info);
            dyn.addV(0, pf0);
            for (int i = 1; i < info.nwaves(); ++i) {
                dyn.TVT(i, pf0);
                dyn.addV(0, pf0);
            }
        }
    }

}
