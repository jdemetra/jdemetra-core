/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.survey;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class WaveSpecificSurveyErrors {

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
            d.drop(2, 0).set(info.v2);
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
            d.range(1, info.nwaves-1).set(info.ar21);
            tr.subDiagonal(info.nwaves-1).range(2, info.nwaves).set(info.ar22);
            tr.subDiagonal(-info.nwaves).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            for (int i=info.nwaves-1; i>1; --i){
                double x1=x.get(i-1);
                double x2=x.get(i+info.nwaves-2);
                double z=x1*info.ar21+x2*info.ar22;
                x.set(i+info.nwaves, x.get(i));
                x.set(i, z);
            }
            // first two iterations
            double z=x.get(0)*info.ar11;
            x.set(1+info.nwaves, x.get(1));
            x.set(1, z);
            x.set(0,0);
            
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
            d.drop(2, 0).add(info.v2);
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
            pf0.diagonal().set(1);
            pf0.subDiagonal(info.nwaves+1).set(info.ar11);
            pf0.subDiagonal(-info.nwaves-1).set(info.ar11);
        }
    }
}
