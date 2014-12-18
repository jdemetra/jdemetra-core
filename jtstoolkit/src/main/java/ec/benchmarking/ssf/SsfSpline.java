/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/
package ec.benchmarking.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * State space representation of a non parametric spline model
 *
 * @author Jean Palate
 */
public class SsfSpline implements ISsf {


    SsfSpline() {
    }

    /**
     * T - K*Z
     */
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        T(pos, lm);
        lm.column(0).sub(k);
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
        b.diagonal().set(1);
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(0, 0, 1.0 / 3);
        qm.set(0, 1, 1.0 / 2);
        qm.set(1, 0, 1.0 / 2);
        qm.set(1, 1, 1);
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
        fullQ(pos,qm);
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void T(int pos, SubMatrix tr) {
        tr.diagonal().set(1);
        tr.set(0,1,1);
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
        vm.row(0).add(vm.row(1));
        vm.column(0).add(vm.column(1));
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
