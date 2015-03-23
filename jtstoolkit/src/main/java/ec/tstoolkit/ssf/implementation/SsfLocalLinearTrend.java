/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.ssf.implementation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author PCUser
 */
public class SsfLocalLinearTrend implements ISsf {

    private final double var_, svar_;

    public SsfLocalLinearTrend(double var, double svar) {
        var_ = var;
        svar_ = svar;
    }

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        lm.column(1).set(1);
        lm.set(0, 0, 1 - k.get(0));
        lm.set(1, 0, -k.get(1));
    }

    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        vm.add(0, 0, d);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        x.add(0, d);
    }

    @Override
    public void Z(int pos, DataBlock x) {
        x.set(0, 1);
    }

    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        x.copy(m.row(0));
    }

    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        return vm.get(0, 0);
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        return x.get(0);
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
        b.set(1);
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(0, 0, var_);
        qm.set(1, 1, svar_);
    }

    @Override
    public int getNonStationaryDim() {
        return 2;
    }

    @Override
    public int getStateDim() {
        return 2;
    }

    @Override
    public int getTransitionResCount() {
        return 2;
    }

    @Override
    public int getTransitionResDim() {
        return 2;
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
        return true;
    }

    @Override
    public boolean isTimeInvariant() {
        return true;
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
        return var_ >= 0 && svar_ >= 0;
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
        qm.set(0, 0, var_);
        qm.set(1, 1, svar_);
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(0, 0, 1);
        tr.set(0, 1, 1);
        tr.set(1, 1, 1);
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
        double v01 = vm.get(0, 1), v11 = vm.get(1, 1);
        vm.add(0, 0, 2 * v01 + v11);
        vm.add(0, 1, v11);
        vm.add(1, 0, v11);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        x.add(0, x.get(1));
    }

    @Override
    public void W(int pos, SubMatrix wv) {
    }

    @Override
    public void XT(int pos, DataBlock x) {
        x.add(1, x.get(0));
    }

}
