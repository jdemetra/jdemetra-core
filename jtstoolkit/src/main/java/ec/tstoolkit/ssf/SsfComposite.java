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
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 *
 * @author pcuser
 */
public class SsfComposite implements ISsf {

    private final ICompositeModel model_;
    private final int[] m_dim, m_cdim;
    private final int m_fdim;
    private final double[] tmp_;

    public SsfComposite(ICompositeModel model) {
        model_ = model;
        int n = model_.getComponentsCount();
        m_dim = new int[n];
        m_cdim = new int[n];
        m_dim[0] = model_.getComponent(0).getStateDim();
        for (int i = 1; i < m_dim.length; ++i) {
            m_dim[i] = model_.getComponent(i).getStateDim();
            m_cdim[i] = m_cdim[i - 1] + m_dim[i - 1];
        }
        m_fdim = m_cdim[n - 1] + m_dim[n - 1];
        tmp_ = new double[m_fdim];
    }

    public ICompositeModel getCompositeModel(){
        return model_;
    }

    @Override
    public void L(int pos, DataBlock k, SubMatrix lm) {
        // raw implementation. Should be improved
        T(pos, lm);
        DataBlock tmp = new DataBlock(tmp_);
        tmp.set(0);
        Z(pos, tmp);
        DataBlockIterator cols = lm.columns();
        DataBlock col = cols.getData();
        do {
            double z = -tmp.get(cols.getPosition());
            col.addAY(z, k);
        } while (cols.next());
    }

    @Override
    public void VpZdZ(int pos, SubMatrix vm, double d) {
        // raw implementation. Should be improved
        DataBlock tmp = new DataBlock(tmp_);
        tmp.set(0);
        Z(pos, tmp);
        DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            col.addAY(d * tmp.get(cols.getPosition()), tmp);
        } while (cols.next());
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        for (int i = 0; i < m_dim.length; ++i) {
            DataBlock xi = sub(i, x);
            double h = model_.getWeight(i, pos);
            if (h != 0) {
                model_.getComponent(i).XpZd(pos, xi, d * h);
            }
        }
    }

    @Override
    public void Z(int pos, DataBlock x) {
        for (int i = 0; i < m_dim.length; ++i) {
            DataBlock xi = sub(i, x);
            double h = model_.getWeight(i, pos);
            if (h != 0) {
                model_.getComponent(i).Z(pos, xi);
                xi.mul(h);
            }
        }
    }

    @Override
    public void ZM(int pos, SubMatrix m, DataBlock x) {
        DataBlock tmp = new DataBlock(x.getLength());
        boolean ok = false;
        for (int i = 0; i < m_dim.length; ++i) {
            double h = model_.getWeight(i, pos);
            if (h != 0) {
                if (!ok) {
                    model_.getComponent(i).ZM(pos, rsub(i, m), x);
                    x.mul(h);
                    ok = true;
                } else {
                    model_.getComponent(i).ZM(pos, rsub(i, m), tmp);
                    x.addAY(h, tmp);
                }
            }
        }
    }

    @Override
    public double ZVZ(int pos, SubMatrix vm) {
        double z = 0;
        for (int col = 0; col < model_.getComponentsCount(); ++col) {
            double hi = model_.getWeight(col, pos);
            if (hi != 0) {
                ISsf ccmp= model_.getComponent(col);
                z +=ccmp.ZVZ(pos, sub(col, col, vm)) * hi * hi;
                DataBlock zm = new DataBlock(tmp_, 0, m_dim[col], 1);
                for (int row = col + 1; row < model_.getComponentsCount(); ++row) {
                    double hj = model_.getWeight(row, pos);
                    if (hj != 0) {
                        // compute Zv, using the buffer
                        model_.getComponent(row).ZM(pos, sub(row, col, vm), zm);
                        z += 2 * hi * hj * ccmp.ZX(pos, zm);
                    }
                }
            }
        }
        return z;
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        double d = 0;
        for (int i = 0; i < m_dim.length; ++i) {
            double h = model_.getWeight(i, pos);
            if (h != 0) {
                d += h * model_.getComponent(i).ZX(pos, sub(i, x));
            }
        }
        return d;
    }

    @Override
    public void diffuseConstraints(SubMatrix b) {
        // statedim * diffusedim

        for (int i = 0, j = 0; i < m_dim.length; ++i) {
            int nst = model_.getComponent(i).getNonStationaryDim();
            if (nst != 0) {
                SubMatrix bi = b.extract(m_cdim[i], m_dim[i], j, j + nst);
                model_.getComponent(i).diffuseConstraints(bi);
                j += nst;
            }
        }
    }

    @Override
    public void fullQ(int pos, SubMatrix qm) {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            model_.getComponent(i).fullQ(pos, sub(i, i, qm));
        }
    }

    @Override
    public int getNonStationaryDim() {
        int n = 0;
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            n += model_.getComponent(i).getNonStationaryDim();
        }
        return n;
    }

    @Override
    public int getStateDim() {
        return m_fdim;
    }

    @Override
    public int getTransitionResCount() {
        int n = 0;
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            n += model_.getComponent(i).getTransitionResCount();
        }
        return n;
    }

    @Override
    public int getTransitionResDim() {
        int n = 0;
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            n += model_.getComponent(i).getTransitionResDim();
        }
        return n;
    }

    @Override
    public boolean hasR() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).hasR()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTransitionRes(int pos) {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).hasTransitionRes(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasW() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).hasW()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDiffuse() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).isDiffuse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMeasurementEquationTimeInvariant() {
        if (!model_.hasConstantWeights()) {
            return false;
        }
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (!model_.getComponent(i).isMeasurementEquationTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTimeInvariant() {
        if (!model_.hasConstantWeights()) {
            return false;
        }
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (!model_.getComponent(i).isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTransitionEquationTimeInvariant() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (!model_.getComponent(i).isTransitionEquationTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isTransitionResidualTimeInvariant() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (!model_.getComponent(i).isTransitionResidualTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValid() {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            if (!model_.getComponent(i).isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Pf0(SubMatrix pf0) {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            model_.getComponent(i).Pf0(sub(i, i, pf0));
        }
    }

    @Override
    public void Pi0(SubMatrix pi0) {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            model_.getComponent(i).Pi0(sub(i, i, pi0));
        }
    }

    @Override
    public void Q(int pos, SubMatrix qm) {
        for (int i = 0, j = 0; i < model_.getComponentsCount(); ++i) {
            int rdim = model_.getComponent(i).getTransitionResDim();
            if (rdim > 0) {
                model_.getComponent(i).Q(pos, qm.extract(j, j + rdim, j, j + rdim));
                j += rdim;
            }
        }
    }

    @Override
    public void R(int pos, SubArrayOfInt rv) {
        // dim(rv) = rescount
        for (int i = 0, j = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).hasR()) {
                int rcount = model_.getComponent(i).getTransitionResCount();
                if (rcount > 0) {
                    SubArrayOfInt rvi = rv.range(j, j + rcount);
                    model_.getComponent(i).R(pos, rvi);
                    rvi.add(m_cdim[i]);
                    j += rcount;
                }
            } else {
                for (int k = 0; k < m_dim[k]; ++k) {
                    rv.set(j++, m_cdim[i] + k);
                }
            }
        }
    }

    @Override
    public void T(int pos, SubMatrix tr) {
        for (int i = 0; i < model_.getComponentsCount(); ++i) {
            model_.getComponent(i).T(pos, sub(i, i, tr));
        }
    }

    @Override
    public void TVT(int pos, SubMatrix vm) {
//        for (int i = 0; i < model_.getComponentsCount(); ++i) {
//            // diagonal item
//            model_.getComponent(i).TVT(pos, sub(i, i, vm));
//            for (int j = 0; j < i; ++j) {
//                //Ti * Vij * Tj'
//                SubMatrix vij = sub(i, j, vm);
//                SubMatrix vji = sub(j, i, vm);
//                DataBlockIterator cols = vij.columns();
//                DataBlockIterator rows = vij.rows();
//                DataBlock col = cols.getData();
//                DataBlock row = rows.getData();
//                do {
//                    model_.getComponent(i).TX(pos, col);
//                } while (cols.next());
//                do {
//                    model_.getComponent(j).TX(pos, row);
//                } while (rows.next());
//                vji.copy(vij.transpose());
//            }
//        }
      DataBlockIterator cols = vm.columns();
        DataBlock col = cols.getData();
        do {
            TX(pos, col);
        } while (cols.next());

        DataBlockIterator rows = vm.rows();
        DataBlock row = rows.getData();
        do {
            TX(pos, row);
        } while (rows.next());
   }

    @Override
    public void TX(int pos, DataBlock x) {
        for (int i = 0; i < m_dim.length; ++i) {
            DataBlock xi = sub(i, x);
            model_.getComponent(i).TX(pos, xi);
        }
    }

    @Override
    public void W(int pos, SubMatrix wv) {
        // dim(W) = rcount * rdim
        for (int i = 0, j = 0, k = 0; i < model_.getComponentsCount(); ++i) {
            if (model_.getComponent(i).hasW()) {
                int rcount = model_.getComponent(i).getTransitionResCount();
                int rdim = model_.getComponent(i).getTransitionResDim();
                if (rdim > 0) {
                    model_.getComponent(i).W(pos, wv.extract(j, j + rcount, k, k + rdim));
                    j += rcount;
                    k += rdim;
                }
            } else {
                SubMatrix wjj=wv.extract(j, j + m_dim[i], k, k + m_dim[i]);
                model_.getComponent(i).fullQ(pos, wjj);
                j += m_dim[i];
                k += m_dim[i];
            }
        }
    }

    @Override
    public void XT(int pos, DataBlock x) {
        for (int i = 0; i < m_dim.length; ++i) {
            DataBlock xi = sub(i, x);
            model_.getComponent(i).XT(pos, xi);
        }
    }

    private SubMatrix sub(int i, int j, SubMatrix m) {
        return m.extract(m_cdim[i], m_cdim[i] + m_dim[i], m_cdim[j], m_cdim[j] + m_dim[j]);
    }

    private SubMatrix rsub(int i, SubMatrix m) {
        return m.extract(m_cdim[i], m_cdim[i] + m_dim[i], 0, m.getColumnsCount());
    }

    private SubMatrix csub(int i, SubMatrix m) {
        return m.extract(0, m.getRowsCount(), m_cdim[i], m_cdim[i] + m_dim[i]);
    }

    private DataBlock sub(int i, DataBlock x) {
        return x.range(m_cdim[i], m_cdim[i] + m_dim[i]);
    }
}
