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
import java.util.Collection;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MultivariateSsfDenton extends AbstractMultivariateSsf {

    private int c_ = 4;
    private int nvars_;
    private double[][] w_;
    private Constraint[] constraints_;

    /**
     *
     * @param c
     * @param nvars
     */
    public MultivariateSsfDenton(int c, int nvars) {
        c_ = c;
        nvars_ = nvars;
    }

    /**
     *
     * @param c
     * @param weights
     */
    public MultivariateSsfDenton(int c, Collection<double[]> weights) {
        c_ = c;
        if (weights != null) {
            w_ = new double[weights.size()][];
            w_ = weights.toArray(w_);
            nvars_ = weights.size();
        }
    }

    public MultivariateSsfDenton(int c, double[][] weights) {
        c_ = c;
        w_ = weights;
        nvars_ = weights.length;
    }

    /**
     *
     * @param b
     */
    @Override
    public void diffuseConstraints(SubMatrix b) {
        for (int j = 1, k = 0; j < 2 * nvars_; j += 2, ++k) {
            b.set(j, k, 1);
        }
    }

    /**
     *
     * @param pos
     * @param qm
     */
    @Override
    public void fullQ(int pos, SubMatrix qm) {
        for (int j = 1; j < 2 * nvars_; j += 2) {
            qm.set(j, j, 1);
        }
    }

    /**
     *
     * @return
     */
    public int getConversionFactor() {
        return c_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getNonStationaryDim() {
        return nvars_;
    }

    /**
     *
     * @return
     */
    @Override
    public int getStateDim() {
        return 2 * nvars_;
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
        return constraints_.length + nvars_;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasR() {
        return true;
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
        if (v < nvars_) {
            return (pos + 1) % c_ == 0;
        } else {
            return (pos + 1) % c_ != 0;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDiffuse() {
        return true;
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
        return false;
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
            } while (rows.next());
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
        /*
         * pf0.Set(0); for (int j = 1; j < 2 * m_nvars; j += 2) pf0[j, j] = 1;
         */
    }

    /**
     *
     * @param pi0
     */
    @Override
    public void Pi0(SubMatrix pi0) {
        for (int j = 1; j < 2 * nvars_; j += 2) {
            pi0.set(j, j, 1);
        }
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
        for (int i = 0, j = 1; i < nvars_; ++i, j += 2) {
            rv.set(i, j);
        }
    }

    /**
     *
     * @param cnt
     */
    public void setConstraints(java.util.Collection<Constraint> cnt) {
        constraints_ = new Constraint[cnt.size()];
        constraints_ = cnt.toArray(constraints_);
    }

    /**
     *
     * @param value
     */
    public void setConversionFactor(int value) {
        c_ = value;
    }

    /**
     *
     * @param pos
     * @param tr
     */
    @Override
    public void T(int pos, SubMatrix tr) {
        tr.set(0);
        for (int i = 0; i < 2 * nvars_; i += 2) {
            tr.set(i + 1, i + 1, 1);
            if ((pos + 1) % c_ != 0) {
                tr.set(i, i + 1, weight(pos, i));
                if (pos % c_ != 0) {
                    tr.set(i, i, 1);
                }
            }
        }
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void TX(int pos, DataBlock x) {
        for (int i = 0, j = 0; i < nvars_; ++i, j += 2) {
            if ((pos + 1) % c_ != 0) {
                double s = x.get(j + 1);
                if (pos % c_ == 0) {
                    x.set(j, mweight(pos, i, s));
                } else {
                    x.add(j, mweight(pos, i, s));
                }
            } else {
                x.set(j, 0);
            }
        }
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
        // set v <= w
        if (w < v) {
            int t = v;
            v = w;
            w = t;
        }
        int iv = 2 * v, iw = 2 * w;
        if (w < nvars_) {
            double dv = weight(pos, v), dw = weight(pos, w);
            vm.add(iv + 1, iw + 1, dv * d * dw);
            if ((pos + 1) % c_ == 0) {
                vm.add(iv, iw, d);
                vm.add(iv, iw + 1, d * dw);
                vm.add(iv + 1, iw, d * dv);
            }
        } else if (v < nvars_) {
            w -= nvars_;
            Constraint wcnt = constraints_[w];
            double dv = weight(pos, v);
            for (int i = 0; i < wcnt.index.length; ++i) {
                int l = wcnt.index[i];
                int il = 2 * l;
                double wl = wcnt.weights[i];
                double dl = weight(pos, l);
                double D = d * wl;
                vm.add(iv + 1, il + 1, dv * D * dl);
                if ((pos + 1) % c_ == 0) {
                    vm.add(iv, il + 1, D * dl);
                }
            }
        } else {
            v -= nvars_;
            w -= nvars_;
            Constraint vcnt = constraints_[v];
            Constraint wcnt = constraints_[w];
            for (int i = 0; i < vcnt.index.length; ++i) {
                int k = vcnt.index[i];
                int ik = 2 * k;
                double dk = mweight(pos, k, vcnt.weights[i]);
                for (int j = 0; j < wcnt.index.length; ++j) {
                    int l = wcnt.index[j];
                    int il = 2 * l;
                    double dl = mweight(pos, l, wcnt.weights[j]);
                    vm.add(ik + 1, il + 1, d * dk * dl);
                }
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

    private double weight(int pos, int v) {
        return w_ == null ? 1 : w_[v][pos];
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
        if (v < nvars_) {
            int iv = 2 * v;
            x.add(iv + 1, mweight(pos, v, d));
            if ((pos + 1) % c_ == 0) {
                x.add(iv, d);
            }
        } else {
            v -= nvars_;
            Constraint cnt = constraints_[v];
            for (int i = 0; i < cnt.index.length; ++i) {
                int k = cnt.index[i];
                int ik = 2 * k;
                x.add(ik + 1, mweight(pos, k, cnt.weights[i] * d));
            }
        }
    }

    /**
     *
     * @param pos
     * @param x
     */
    @Override
    public void XT(int pos, DataBlock x) {
        for (int i = 0, j = 0; i < nvars_; ++i, j += 2) {
            // case I: 0, x1
            if ((pos + 1) % c_ == 0) {
                x.set(j, 0);
            } // case II: 0, w x0 + x1
            else if (pos % c_ == 0) {
                double x0 = x.get(j), x1 = x.get(j + 1);
                x.set(j + 1, x1 + mweight(pos, i, x0));
                x.set(j, 0);
            } // case III: x0, w x0 + x1
            else {
                double x0 = x.get(j), x1 = x.get(j + 1);
                x.set(j + 1, x1 + mweight(pos, i, x0));
            }
        }
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
        if (v < nvars_) {
            int iv = 2 * v;
            if ((pos + 1) % c_ == 0) {
                z.set(iv, 1);
            }
            z.set(iv + 1, weight(pos, v));
        } else {
            int k = v - nvars_;
            Constraint cnt = constraints_[k];
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                int il = 2 * l;
                z.set(il + 1, mweight(pos, l, cnt.weights[i]));
            }
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
        if (v < nvars_) {
            int iv = 2 * v;
            if ((pos + 1) % c_ == 0) {
                x.copy(m.row(iv));
            }
            x.addAY(weight(pos, v), m.row(iv + 1));
        } else {
            int k = v - nvars_;
            Constraint cnt = constraints_[k];
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                int il = 2 * l;
                x.addAY(mweight(pos, l, cnt.weights[i]), m.row(il + 1));
            }
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
        // set v <= w
        if (w < v) {
            int t = v;
            v = w;
            w = t;
        }
        int iv = 2 * v, iw = 2 * w;
        if (w < nvars_) {
            double dv = weight(pos, v), dw = weight(pos, w);
            double s = dv * vm.get(iv + 1, iw + 1) * dw;
            if ((pos + 1) % c_ == 0) {
                s += vm.get(iw, iv);
                s += dv * vm.get(iv + 1, iw);
                s += dw * vm.get(iv, iw + 1);
            }
            return s;
        } else if (v < nvars_) {
            int k = w - nvars_;
            Constraint cnt = constraints_[k];
            double dv = weight(pos, v);
            double s = 0;
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                int il = 2 * l;
                double wl = cnt.weights[i];
                double dl = weight(pos, l);
                double scur = dv * vm.get(iv + 1, il + 1) * dl;
                if ((pos + 1) % c_ == 0) {
                    scur += dl * vm.get(iv, il + 1);
                }
                s += scur * wl;
            }
            return s;
        } else {
            v -= nvars_;
            w -= nvars_;
            Constraint vcnt = constraints_[v];
            Constraint wcnt = constraints_[w];
            double s = 0;
            for (int i = 0; i < vcnt.index.length; ++i) {
                int k = vcnt.index[i];
                int ik = 2 * k;
                double dk = mweight(pos, k, vcnt.weights[i]);
                for (int j = 0; j < wcnt.index.length; ++j) {
                    int l = wcnt.index[j];
                    int il = 2 * l;
                    double dl = mweight(pos, l, wcnt.weights[j]);
                    s += dk * vm.get(ik + 1, il + 1) * dl;
                }
            }
            return s;
        }
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
        if (v < nvars_) {
            int iv = 2 * v;
            double r = ((pos + 1) % c_ != 0) ? 0 : x.get(iv);
            return r + mweight(pos, v, x.get(iv + 1));
        } else {
            int k = v - nvars_;
            Constraint cnt = constraints_[k];
            double sum = 0;
            for (int i = 0; i < cnt.index.length; ++i) {
                int l = cnt.index[i];
                int il = 2 * l;
                sum += mweight(pos, l, x.get(il + 1) * cnt.weights[i]);
            }
            return sum;
        }
    }
}
