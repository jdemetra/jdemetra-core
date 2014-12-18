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
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public abstract class AbstractHeteroskedasticSsf implements ISsf{

    private final ISsf ssf;
    private final int sdim;

    public AbstractHeteroskedasticSsf(ISsf ssf){
       this.ssf=ssf;
       sdim=ssf.getStateDim();
    }

    protected abstract double h(int t);

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void Z(int pos, DataBlock x) {
        ssf.Z(pos, sub(x));
        double ht=h(pos);
        if (ht != 0)
            x.mul(sdim, ht);
        else
            x.set(sdim, 0);
    }

    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        ssf.ZM(pos, rsub(m), x);
         double ht=h(pos);
        if (ht != 0)
            x.addAY(ht, m.row(sdim));
    }

    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        double z=ssf.ZVZ(pos, sub(vm));
        double ht=h(pos);
        if (ht != 0){
            z+=2*ht*ssf.ZX(pos, vm.column(sdim).drop(0,1));
            z+=ht*ht*vm.get(sdim, sdim);
        }
        return z;
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        double z=ssf.ZX(pos, sub(x));
         double ht=h(pos);
        if (ht != 0)
            z+=ht*x.get(sdim);
         return z;
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
        ssf.diffuseConstraints(rsub(b));
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        ssf.fullQ(pos, sub(qm));
        qm.set(sdim, sdim, 1);
    }

    @Override
    public int getNonStationaryDim() {
        return ssf.getNonStationaryDim();
    }

    @Override
    public int getStateDim() {
        return ssf.getStateDim()+1;
    }

    @Override
    public int getTransitionResCount() {
        return ssf.getTransitionResCount()+1;
    }

    @Override
    public int getTransitionResDim() {
        return ssf.getTransitionResDim()+1;
    }

    public boolean hasR() {
        return ssf.hasR();
    }

    public boolean hasTransitionRes(int pos) {
        return ssf.hasTransitionRes(pos) || h(pos) !=0;
    }

    public boolean hasW() {
        return ssf.hasW();
    }

    public boolean isDiffuse() {
        return ssf.isDiffuse();
    }

    public boolean isMeasurementEquationTimeInvariant() {
        return false;
    }

    public boolean isTimeInvariant() {
        return false;
    }

    public boolean isTransitionEquationTimeInvariant() {
        return ssf.isTransitionEquationTimeInvariant();
    }

    public boolean isTransitionResidualTimeInvariant() {
        return ssf.isTransitionResidualTimeInvariant();
    }

    public boolean isValid() {
        return ssf.isValid();
    }

    public void Pf0(SubMatrix pf0) {
        ssf.Pf0(sub(pf0));
    }

    public void Pi0(SubMatrix pi0) {
        ssf.Pf0(sub(pi0));
    }

    public void Q(int pos, SubMatrix qm) {
        int r=qm.getColumnsCount()-1;
        ssf.Q(pos, qm.extract(0, r, 0, r));
        qm.set(r, r, 1);
    }

    public void R(int pos, SubArrayOfInt rv) {
        ssf.R(pos, rv.range(0, sdim));
        rv.set(rv.getLength()-1, sdim);
    }

    public void T(int pos, SubMatrix tr) {
        ssf.T(pos, sub(tr));
        tr.column(sdim).set(0);
        tr.row(sdim).set(0);
    }

    public void TVT(int pos, SubMatrix vm) {
        ssf.TVT(pos, sub(vm));
        vm.column(sdim).set(0);
        vm.row(sdim).set(0);
    }

    public void TX(int pos, DataBlock x) {
        ssf.TX(pos, sub(x));
        x.set(sdim, 0);
    }

    public void W(int pos, SubMatrix wv) {
        int row=wv.getRowsCount()-1;
        int col=wv.getColumnsCount()-1;
        ssf.W(pos, wv.extract(0, row, 0, col));
        wv.column(col).set(0);
        wv.set(row,col, 1);
    }

    public void XT(int pos, DataBlock x) {
        ssf.XT(pos, sub(x));
        x.set(sdim, 0);
    }

    private SubMatrix sub(SubMatrix m) {
        return m.extract(0, sdim, 0, sdim);
    }

    private SubMatrix rsub(SubMatrix m) {
        return m.extract(0, sdim, 0, m.getColumnsCount());
    }

    private SubMatrix csub(SubMatrix m) {
        return m.extract(0, m.getRowsCount(), 0, sdim);
    }

    private DataBlock sub(DataBlock x) {
        return x.range(0, sdim);
    }
}
