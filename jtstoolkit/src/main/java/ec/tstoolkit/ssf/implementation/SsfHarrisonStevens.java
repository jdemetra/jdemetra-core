/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf.implementation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author PCUser
 */
public class SsfHarrisonStevens implements ISsf {

    private final int s_;
    private final double[] var_;
    private final Matrix V_;

    public SsfHarrisonStevens(final int s, final double var) {
        s_ = s;
        var_ = null;
        V_ = Matrix.square(s - 1);
        V_.set(-1.0 / s);
        V_.diagonal().add(1);
        V_.mul(var);
    }

    public SsfHarrisonStevens(final double[] var) {
        s_ = var.length;
        var_ = var.clone();
        Matrix C = new Matrix(s_ - 1, s_);
        C.set(-1.0 / s_);
        C.diagonal().add(1);
        Matrix D = Matrix.diagonal(var);
        V_ = SymmetricMatrix.quadraticFormT(D, C);
    }
    
    public double[] getVariances(){
        return var_;
    }

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            DataBlockIterator columns = lm.columns();
            DataBlock col = columns.getData();
            do {
                col.copy(k);
            } while (columns.next());
        } else {
            lm.column(spos).setAY(-1, k);
        }
        lm.diagonal().add(1);
    }

    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            vm.add(d);
        } else {
            vm.add(spos, spos, d);
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            x.add(-d);
        } else {
            x.add(spos, d);
        }
    }

    @Override
    public void Z(int pos, DataBlock x) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            x.set(-1);
        } else {
            x.set(spos, 1);
        }
    }

    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            for (int i = 0; i < x.getLength(); ++i) {
                x.set(i, -m.column(i).sum());
            }
        } else {
            x.copy(m.row(spos));
        }
    }

    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            return vm.sum();
        } else {
            return vm.get(spos, spos);
        }
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        int spos = pos % s_;
        if (spos == s_ - 1) {
            return -x.sum();
        } else {
            return x.get(spos);
        }
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.copy(V_.subMatrix());
    }

    @Override
    public int getNonStationaryDim() {
        return s_ - 1;
    }

    @Override
    public int getStateDim() {
        return s_ - 1;
    }

    @Override
    public int getTransitionResCount() {
        return s_ - 1;
    }

    @Override
    public int getTransitionResDim() {
        return s_ - 1;
    }

    @Override
    public boolean hasR() {
        return false;
    }

    @Override
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    @Override
    public boolean hasW() {
        return false;
    }

    @Override
    public boolean isDiffuse() {
        return true;
    }

    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return false;
    }

    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return true;
    }

    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void Pf0(SubMatrix pf0) {
    }

    @Override
    public void Pi0(SubMatrix pi0) {
        pi0.diagonal().set(1);
    }

    @Override
    public void Q(int pos, SubMatrix qm) {
        qm.copy(V_.subMatrix());
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void T(int pos, SubMatrix tr) {
        tr.diagonal().set(1);
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
    }

    @Override
    public void TX(int pos, DataBlock x) {
    }

    @Override
    public void W(int pos, SubMatrix wv) {
    }

    @Override
    public void XT(int pos, DataBlock x) {
    }

}
