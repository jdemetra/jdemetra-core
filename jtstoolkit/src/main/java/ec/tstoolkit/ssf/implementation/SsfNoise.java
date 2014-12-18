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

package ec.tstoolkit.ssf.implementation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * Represents a white noise.
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class SsfNoise implements ISsf {

    private final double var_;

    public SsfNoise() {
        var_ = 1;
    }

    public SsfNoise(double var) {
        var_ = var;
    }

    public double getVariance(){
        return var_;
    }

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        lm.set(0, 0, -k.get(0));
    }

    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        vm.add(0,0,d);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        x.add(0, d);
    }

    @Override
    public void Z(int pos, DataBlock x) {
        x.set(0,1);
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
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.set(0, 0, var_);
    }

    @Override
    public int getNonStationaryDim() {
        return 0;
    }

    @Override
    public int getStateDim() {
        return 1;
    }

    @Override
    public int getTransitionResCount() {
        return 1;
    }

    @Override
    public int getTransitionResDim() {
        return 1;
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
        return false;
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
        pf0.set(0,0,var_);
    }

    @Override
    public void Pi0(SubMatrix pf0) {
    }

    @Override
    public void Q(int pos, SubMatrix qm) {
        qm.set(0, 0, var_);
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    @Override
    public void T(int pos, SubMatrix tr) {
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
        vm.set(0,0,0);
    }

    @Override
    public void TX(int pos, DataBlock x) {
        x.set(0,0);
    }

    @Override
    public void W(int pos, SubMatrix wv) {
    }

    @Override
    public void XT(int pos, DataBlock x) {
        x.set(0,0);
    }
}
