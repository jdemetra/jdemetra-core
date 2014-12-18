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

package ec.benchmarking.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.multivariate.AbstractMultivariateSsf;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ContemporaneousSsfCholette extends AbstractMultivariateSsf {

    private int nvars_;
    private double rho_;
    private double[][] w_;
    private Constraint[] constraints_;

    /**
     * 
     * @param c
     * @param nvars
     */
    public ContemporaneousSsfCholette(double ro, int nvars) {
        nvars_ = nvars;
        rho_=ro;
    }

    /**
     * 
     * @param c
     * @param weights
     */
    public ContemporaneousSsfCholette(double rho, List<double[]> weights) {
        rho_=rho;
        if (weights != null) {
            w_ = new double[weights.size()][];
            w_ = weights.toArray(w_);
            nvars_ = weights.size();
        }
    }

    /**
     * 
     * @param c
     * @param weights
     */
    public ContemporaneousSsfCholette(double rho, double[][] weights) {
        rho_=rho;
        if (weights != null) {
            w_ = weights;
            nvars_ = weights.length;
        }
    }
    
    public double getRho(){
        return rho_;
    }
    
    public void setRho(double rho){
        rho_=rho;
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        qm.diagonal().set(1);
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return 0;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return nvars_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResCount() {
        return nvars_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getTransitionResDim() {
        return nvars_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getVarsCount() {
        return constraints_.length;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        return false;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean hasTransitionRes(int pos) {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasW() {
        return false;
    }

    /**
     *
     * @param pos
     * @param v
     * @return
     */
    @Override
    public boolean hasZ(int pos, int v) {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
        return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionEquationTimeInvariant() {
        return true;
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isTransitionResidualTimeInvariant() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        return nvars_ > 0 && constraints_.length > 0;
    }

    /**
     *
     * @param pos
     * @param K
     * @param lm
     */
    @Override
    public void L(int pos, SubMatrix K, SubMatrix lm) {
        T(pos, lm);
        for (int i = 0; i < getVarsCount(); ++i) {
            DataBlockIterator rows = lm.rows();
            DataBlock row = rows.getData();
            do {
                double k = -K.get(rows.getPosition(), i);
                XpZd(pos, i, row, k);
            }
            while (rows.next());
        }
    }

    private double mweight(int pos, int v, double m) {
        return w_ == null ? m : w_[v][pos] * m;
    }

    /**
     *
     * @param pf0
     */
    @Override
    public void Pf0(SubMatrix pf0) {
        pf0.diagonal().set(1/(1-rho_*rho_));
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void Q(int pos, SubMatrix qm) {
        qm.diagonal().set(1);
    }

    /**
     *
     * @param pos
     * @param rv
     */
    @Override
    public void R(int pos, SubArrayOfInt rv) {
    }

    /**
     * 
     * @param cnt
     */
    public void setConstraints(java.util.Collection<Constraint> cnt) {
        constraints_ = new Constraint[cnt.size()];
        constraints_ = cnt.toArray(constraints_);
    }

    public void setConstraints(Constraint[] cnt) {
        constraints_ = cnt;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.diagonal().set(rho_);
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        x.mul(rho_);
    }

    @Override
    public void TVT(int pos, SubMatrix V) {
	V.mul(rho_ * rho_);
    }

    @Override
    public void MT(final int pos, final SubMatrix M)
    {
        M.mul(rho_);
    }

    /**
     * 
     * @param pos
     * @param M
     */
    @Override
    public void TM(final int pos, final SubMatrix M)
    {
        M.mul(rho_);
    }
    /**
     *
     * @param pos
     * @param v
     * @param w
     * @param vm
     * @param d
     */
    @Override
    public void VpZdZ(int pos, int v, int w, SubMatrix vm, double d) {
        Constraint vcnt = constraints_[v];
        Constraint wcnt = constraints_[w];
        for (int i = 0; i < vcnt.index.length; ++i) {
            int k = vcnt.index[i];
            double dk = mweight(pos, k, vcnt.weights[i]);
            for (int j = 0; j < wcnt.index.length; ++j) {
                int l = wcnt.index[j];
                double dl = mweight(pos, l, wcnt.weights[j]);
                vm.add(k, l, d * dk * dl);
            }
        }
    }

    /**
     *
     * @param pos
     * @param wv
     */
    @Override
    public void W(int pos, SubMatrix wv) {
    }

    /**
     *
     * @param pos
     * @param v
     * @param x
     * @param d
     */
    @Override
    public void XpZd(int pos, int v, DataBlock x, double d) {
        Constraint cnt = constraints_[v];
        for (int i = 0; i < cnt.index.length; ++i) {
            int k = cnt.index[i];
            x.add(k, mweight(pos, k, cnt.weights[i] * d));
        }
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
        x.mul(rho_);
    }

    /**
     *
     * @param pos
     * @param v
     * @param z
     */
    @Override
    public void Z(int pos, int v, DataBlock z) {
        z.set(0);
        Constraint cnt = constraints_[v];
        for (int i = 0; i < cnt.index.length; ++i) {
            int l = cnt.index[i];
            z.set(l, mweight(pos, l, cnt.weights[i]));
        }
    }

    /**
     *
     * @param pos
     * @param v
     * @param m
     * @param x
     */
    @Override
    public void ZM(int pos, int v, SubMatrix m, DataBlock x) {
        x.set(0);
        Constraint cnt = constraints_[v];
        for (int i = 0; i < cnt.index.length; ++i) {
            int l = cnt.index[i];
            x.addAY(mweight(pos, l, cnt.weights[i]), m.row(l));
        }
    }

    /**
     *
     * @param pos
     * @param v
     * @param w
     * @param vm
     * @return
     */
    @Override
    public double ZVZ(int pos, int v, int w, SubMatrix vm) {
        Constraint vcnt = constraints_[v];
        Constraint wcnt = constraints_[w];
        double s = 0;
        for (int i = 0; i < vcnt.index.length; ++i) {
            int k = vcnt.index[i];
            double dk = mweight(pos, k, vcnt.weights[i]);
            for (int j = 0; j < wcnt.index.length; ++j) {
                int l = wcnt.index[j];
                double dl = mweight(pos, l, wcnt.weights[j]);
                s += dk * vm.get(k, l) * dl;
            }
        }
        return s;

    }

    /**
     *
     * @param pos
     * @param v
     * @param x
     * @return
     */
    @Override
    public double ZX(int pos, int v, DataBlock x) {
        Constraint cnt = constraints_[v];
        double sum = 0;
        for (int i = 0; i < cnt.index.length; ++i) {
            int l = cnt.index[i];
            sum += mweight(pos, l, x.get(l) * cnt.weights[i]);
        }
        return sum;
    }
}
