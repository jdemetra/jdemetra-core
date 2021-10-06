/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sts;

import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.implementations.Loading;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class PeriodicComponent {

    public StateComponent stateComponent(final double period, final int[] harmonics, final double cvar) {
        Data data = new Data(period, harmonics, cvar);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }

    public ISsfLoading defaultLoading(int k) {
        if (k == 0) {
            return Loading.fromPosition(0);
        }
        int[] pos = new int[k];
        for (int i = 0, p = 0; i < k; ++i, p += 2) {
            pos[i] = p;
        }
        return Loading.fromPositions(pos);
    }

    static class Data {

        private final double var;
        private final double period;
        private final int[] k;

        public Data(double period, int[] k, double var) {
            this.var = var;
            this.period = period;
            this.k = k;
        }
    }

    static class Initialization implements ISsfInitialization {

        final Data data;

        Initialization(Data data) {
            this.data = data;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return data.k.length * 2;
        }

        @Override
        public int getStateDim() {
            return data.k.length * 2;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix p) {
        }

        @Override
        public void Pi0(Matrix p) {
            p.set(1);
        }
    }

    static class Dynamics implements ISsfDynamics {

        final Data data;
        private final double[] ccos, csin;
        private double e;

        Dynamics(Data data) {
            this.data = data;
            e = Math.sqrt(data.var);
            double q = 2 * Math.PI / data.period;
            int nh=data.k.length;
            ccos = new double[nh];
            csin = new double[nh];
            for (int i = 0; i < nh; ++i) {
                double f = q * data.k[i];
                ccos[i] = Math.cos(f);
                csin[i] = Math.sin(f);
            }
        }

        @Override
        public int getInnovationsDim() {
            return data.var == 0 ? 0 : 2 * data.k.length;
        }

        @Override
        public void V(int pos, Matrix v) {
            v.diagonal().set(data.var);
        }

        @Override
        public void S(int pos, Matrix s) {
            s.diagonal().set(e);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.var != 0;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, Matrix tr) {
            for (int i = 0, p = 0; i < data.k.length; ++i, p += 2) {
                tr.set(p, p, ccos[i]);
                tr.set(p, p + 1, csin[i]);
                tr.set(p + 1, p, -csin[i]);
                tr.set(p + 1, p + 1, ccos[i]);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            for (int i = 0, p = 0; i < data.k.length; ++i, p += 2) {
                double a = x.get(p), b = x.get(p + 1);
                x.set(p, a * ccos[i] + b * csin[i]);
                x.set(p + 1, -a * csin[i] + b * ccos[i]);
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.addAY(e, u);
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.diagonal().add(data.var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            for (int i = 0, p = 0; i < data.k.length; ++i, p += 2) {
                double a = x.get(p), b = x.get(p + 1);
                x.set(p, a * ccos[i] - b * csin[i]);
                x.set(p + 1, a * csin[i] + b * ccos[i]);
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.setAY(e, x);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

}
