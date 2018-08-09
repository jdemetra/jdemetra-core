/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.models;

import demetra.arima.AutoCovarianceFunction;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.polynomials.Polynomial;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AR {

    @lombok.Value
    private static class Data {

        double[] phi;
        double var;
        int nlags;

        Polynomial ar() {
            double[] c = new double[1 + phi.length];
            c[0] = 1;
            for (int i = 0; i < phi.length; ++i) {
                c[i + 1] = -phi[i];
            }
            return Polynomial.ofInternal(c);
        }

        int dim() {
            return Math.max(phi.length, nlags);
        }
    }

    private static class Initialization implements ISsfInitialization {

        private final Data info;

        Initialization(final Data info) {
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
        public void Pi0(Matrix pf0) {
        }

        @Override
        public void Pf0(Matrix pf0) {
            AutoCovarianceFunction acf=new AutoCovarianceFunction(Polynomial.ONE, info.ar(), info.var );
            acf.prepare(pf0.getColumnsCount());
            pf0.diagonal().set(acf.get(0));
            for (int i=1; i<pf0.getColumnsCount(); ++i){
                pf0.subDiagonal(i).set(acf.get(i));
                pf0.subDiagonal(-i).set(acf.get(i));
            }
        }

    }

    private static class Dynamics implements ISsfDynamics {

        private final Data info;

        Dynamics(final Data info) {
            this.info = info;
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void S(int pos, Matrix cm) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean hasInnovations(int pos) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void T(int pos, Matrix tr) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void TX(int pos, DataBlock x) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, Matrix p) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XT(int pos, DataBlock x) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isTimeInvariant() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
