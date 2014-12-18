/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.ISsf;

/**
 * Encapsulation of a univariate ISsf in a multivariate interface
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class UnivariateSsf extends AbstractMultivariateSsf {
    
    private final ISsf ssf;
    
    public UnivariateSsf(final ISsf ssf) {
        this.ssf = ssf;
    }
    
    @Override
    public int getVarsCount() {
        return 1;
    }
    
    @Override
    public boolean hasZ(int pos, int v) {
        return true;
    }
    
    @Override
    public void L(int pos, SubMatrix K, SubMatrix lm) {
        ssf.L(pos, K.column(0), lm);
    }
    
    @Override
    public void VpZdZ(int pos, int v, int w, SubMatrix vm, double d) {
        ssf.VpZdZ(pos, vm, d);
    }
    
    @Override
    public void XpZd(int pos, int v, DataBlock x, double d) {
        ssf.XpZd(pos, x, d);
    }
    
    @Override
    public void Z(int pos, int v, DataBlock z) {
        ssf.Z(pos, z);
    }
    
    @Override
    public double ZVZ(int pos, int v, int w, SubMatrix vm) {
        return ssf.ZVZ(pos, vm);
    }
    
    @Override
    public double ZX(int pos, int v, DataBlock x) {
        return ssf.ZX(pos, x);
    }
    
    @Override
    public void diffuseConstraints(SubMatrix b) {
        ssf.diffuseConstraints(b);
    }
    
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        ssf.fullQ(pos, qm);
    }
    
    @Override
    public int getNonStationaryDim() {
        return ssf.getNonStationaryDim();
    }
    
    @Override
    public int getStateDim() {
        return ssf.getStateDim();
    }
    
    @Override
    public int getTransitionResCount() {
        return ssf.getTransitionResCount();
    }
    
    @Override
    public int getTransitionResDim() {
        return ssf.getTransitionResDim();
    }
    
    @Override
    public boolean hasR() {
        return ssf.hasR();
    }
    
    @Override
    public boolean hasTransitionRes(int pos) {
        return ssf.hasTransitionRes(pos);
    }
    
    @Override
    public boolean hasW() {
        return ssf.hasW();
    }
    
    @Override
    public boolean isDiffuse() {
        return ssf.isDiffuse();
    }
    
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return ssf.isMeasurementEquationTimeInvariant();
    }
    
    @Override
    public boolean isTimeInvariant() {
        return ssf.isTimeInvariant();
    }
    
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return ssf.isTransitionEquationTimeInvariant();
    }
    
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return ssf.isTransitionResidualTimeInvariant();
    }
    
    @Override
    public boolean isValid() {
        return ssf.isValid();
    }
    
    @Override
    public void Pf0(SubMatrix pf0) {
        ssf.Pf0(pf0);
    }
    
    @Override
    public void Pi0(SubMatrix pi0) {
        ssf.Pi0(pi0);
    }
    
    @Override
    public void Q(int pos, SubMatrix qm) {
        ssf.Q(pos, qm);
    }
    
    @Override
    public void R(int pos, SubArrayOfInt rv) {
        ssf.R(pos, rv);
    }
    
    @Override
    public void T(int pos, SubMatrix tr) {
        ssf.T(pos, tr);
    }
    
    @Override
    public void TX(int pos, DataBlock x) {
        ssf.TX(pos, x);
    }
    
    @Override
    public void W(int pos, SubMatrix wv) {
        ssf.W(pos, wv);
    }
    
    @Override
    public void XT(int pos, DataBlock x) {
        ssf.XT(pos, x);
    }
}