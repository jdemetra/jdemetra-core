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
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author Jean Palate
 */
public class SsfVarianceAdapter implements ISsf {
    
    private final double var_;
    private final ISsf master_;
    
    public SsfVarianceAdapter(ISsf master, double vmul) {
        master_ = master;
        var_ = vmul;
    }
    
    public double getVarianceMultiplier() {
        return var_;
    }
    
    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        master_.L(pos, k, lm);
    }
    
    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        master_.VpZdZ(pos, vm, d);
    }
    
    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        master_.XpZd(pos, x, d);
    }
    
    @Override
    public void Z(int pos, DataBlock x) {
        master_.Z(pos, x);
    }
    
    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        master_.ZM(pos, m, x);
    }
    
    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        return master_.ZVZ(pos, vm);
    }
    
    @Override
    public double ZX(int pos, DataBlock x) {
        return master_.ZX(pos, x);
    }
    
    @Override
    public void diffuseConstraints(SubMatrix b) {
        master_.diffuseConstraints(b);
    }
    
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        master_.fullQ(pos, qm);
        qm.mul(var_);
    }
    
    @Override
    public int getNonStationaryDim() {
        return master_.getNonStationaryDim();
    }
    
    @Override
    public int getStateDim() {
        return master_.getStateDim();
    }
    
    @Override
    public int getTransitionResCount() {
        return master_.getTransitionResCount();
    }
    
    @Override
    public int getTransitionResDim() {
        return master_.getTransitionResDim();
    }
    
    @Override
    public boolean hasR() {
        return master_.hasR();
    }
    
    @Override
    public boolean hasTransitionRes(int pos) {
        return master_.hasTransitionRes(pos);
    }
    
    @Override
    public boolean hasW() {
        return master_.hasW();
    }
    
    @Override
    public boolean isDiffuse() {
        return master_.isDiffuse();
    }
    
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return master_.isMeasurementEquationTimeInvariant();
    }
    
    @Override
    public boolean isTimeInvariant() {
        return master_.isTimeInvariant();
    }
    
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return master_.isTransitionEquationTimeInvariant();
    }
    
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return master_.isTransitionResidualTimeInvariant();
    }
    
    @Override
    public boolean isValid() {
        return var_ >= 0 && master_.isValid();
    }
    
    @Override
    public void Pf0(SubMatrix pf0) {
        master_.Pf0(pf0);
        pf0.mul(var_);
    }
    
    @Override
    public void Pi0(SubMatrix pi0) {
        master_.Pi0(pi0);
    }
    
    @Override
    public void Q(int pos, SubMatrix qm) {
        master_.Q(pos, qm);
        qm.mul(var_);
    }
    
    @Override
    public void R(int pos, SubArrayOfInt rv) {
        master_.R(pos, rv);
    }
    
    @Override
    public void T(int pos, SubMatrix tr) {
        master_.T(pos, tr);
    }
    
    @Override
    public void TVT(int pos, SubMatrix vm) {
        master_.TVT(pos, vm);
    }
    
    @Override
    public void TX(int pos, DataBlock x) {
        master_.TX(pos, x);
    }
    
    @Override
    public void W(int pos, SubMatrix wv) {
        master_.W(pos, wv);
    }
    
    @Override
    public void XT(int pos, DataBlock x) {
        master_.XT(pos, x);
    }
}
