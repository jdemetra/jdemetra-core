/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.arima;

import jdplus.arima.AutoCovarianceFunction;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.math.polynomials.Polynomial;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.StateComponent;
import jdplus.ssf.basic.Loading;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 * Dynamics of the state array for y(t) = ar(0) y(t-1)+ ... + ar(p)y(t-p-1) The
 * state array block contains y(t), y(t-1)...y(t-lag), where lag can be greater
 * than the degree of the ar coefficients
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SsfAr {

    public StateComponent of(@NonNull double[] ar, double var, int nlags) {
        return of(ar, var, nlags, false);
    }

    /**
     * @param ar Auto-regressive parameters
     * @param var Innovation variance
     * @param nlags Number of lags. Meaningful if greater than the length of ar.
     * @param zeroinit Zero initialization. Should be false by default
     * @return
     */
    public StateComponent of(@NonNull double[] ar, double var, int nlags, boolean zeroinit) {
        if (ar.length == 0) {
            throw new IllegalArgumentException();
        }
        if (nlags < ar.length) {
            nlags = ar.length;
        }
        Data data = new Data(ar, var, nlags);
        return new StateComponent(new Initialization(data, zeroinit), new Dynamics(data));
    }

    public ISsfLoading defaultLoading(){
        return Loading.fromPosition(0);
    }

    @lombok.Value
    private static class Data {

        double[] phi;
        double var;
        int dim;

        Polynomial ar() {
            double[] c = new double[1 + phi.length];
            c[0] = 1;
            for (int i = 0; i < phi.length; ++i) {
                c[i + 1] = -phi[i];
            }
            return Polynomial.ofInternal(c);
        }

        int dim() {
            return dim;
        }
    }

    private static class Initialization implements ISsfInitialization {

        private final Data info;
        private final boolean zeroinit;

        Initialization(final Data info, final boolean zeroinit) {
            this.info = info;
            this.zeroinit = zeroinit;
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
        public void Pi0(FastMatrix pf0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            if (!zeroinit) {
                AutoCovarianceFunction acf = new AutoCovarianceFunction(Polynomial.ONE, info.ar(), info.var);
                acf.prepare(pf0.getColumnsCount());
                pf0.diagonal().set(acf.get(0));
                for (int i = 1; i < pf0.getColumnsCount(); ++i) {
                    pf0.subDiagonal(i).set(acf.get(i));
                    pf0.subDiagonal(-i).set(acf.get(i));
                }
            } else {
                pf0.set(0, 0, info.var);
            }
        }

    }

    private static class Dynamics implements ISsfDynamics {

        private final Data info;
        private final DataBlock z;

        Dynamics(final Data info) {
            this.info = info;
            z = DataBlock.make(info.dim());
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, info.var);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(0, 0, Math.sqrt(info.var));
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
            tr.subDiagonal(-1).set(1);
            tr.row(0).extract(0, info.phi.length).copyFrom(info.phi, 0);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            double y = 0;
            DoubleSeqCursor reader = x.cursor();
            for (int i = 0; i < info.phi.length; ++i) {
                y += info.phi[i] * reader.getAndNext();
            }
            x.fshift(1);
            x.set(0, y);
        }

        @Override
        public void TVT(final int pos, final FastMatrix vm) {
            z.set(0);
            DataBlockIterator cols = vm.columnsIterator();
            for (int i = 0; i < info.phi.length; ++i) {
                z.addAY(info.phi[i], cols.next());
            }
            TX(pos, z);
            vm.downRightShift(1);
            vm.column(0).copy(z);
            vm.row(0).copy(z);
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, info.var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            double first = x.get(0);
            x.bshift(1);
            x.setLast(0);
            if (first != 0) {
                for (int i = 0; i < info.phi.length; ++i) {
                    x.add(i, first * info.phi[i]);
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

}
