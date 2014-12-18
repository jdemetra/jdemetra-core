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
package ec.tstoolkit.arima.estimation;


import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractRegArimaModel {

    /**
     *
     */
    protected BackFilter m_ur;
    /**
     *
     */
    protected RegModel m_dregs;
    private DataBlock m_y;
    private boolean m_bmean;
    private final java.util.List<DataBlock> m_x;
    private int[] m_missings;

    /**
     *
     */
    protected AbstractRegArimaModel() {
        m_x = new ArrayList<>();
    }

    /**
     * 
     * @param m
     * @param ur
     */
    protected AbstractRegArimaModel(final AbstractRegArimaModel m,
            final boolean ur) {
        m_y = m.m_y;
        m_bmean = m.m_bmean;
        m_missings = m.m_missings;
        m_x = Jdk6.newArrayList(m.m_x);
        if (ur) {
            m_ur = m.m_ur;
            m_dregs = m.m_dregs;
        }
    }

    /**
     * 
     * @param y
     */
    protected AbstractRegArimaModel(final DataBlock y) {
        m_y = y;
        m_x = new ArrayList<>();
    }

    /**
     * 
     * @param x
     */
    public void addX(final DataBlock x) {
        m_x.add(x);
        if (m_dregs != null) {
            if (m_ur.getLength() == 1) {
                m_dregs.addX(x);
            } else {
                DataBlock dx = new DataBlock(m_y.getLength() - m_ur.getDegree());
                m_ur.filter(x, dx);
                m_dregs.addX(dx);
            }
        }
    }

    private void calcdregs() {
        if (m_y == null) {
            return;
        }
        m_dregs = new RegModel();
        int n = m_y.getLength();
        if (m_ur == null) {
            calstationarymodel();
        }
        if (m_ur.getLength() == 1) {
            m_dregs.setY(m_y);

            if (m_missings != null) {
                for (int i = 0; i < m_missings.length; ++i) {
                    DataBlock m = new DataBlock(n);
                    m.set(m_missings[i], 1);
                    m_dregs.addX(m);
                }
            }
            for (int i = 0; i < m_x.size(); ++i) {
                m_dregs.addX(m_x.get(i));
            }
            m_dregs.setMeanCorrection(m_bmean);
        } else {
            int nur = m_ur.getDegree();
            DataBlock dy = new DataBlock(m_y.getLength() - nur);
            m_ur.filter(m_y, dy);
            m_dregs.setY(dy);

            if (m_missings != null) // could be substantially improved... make a window around the
            // missing value.
            {
                for (int i = 0; i < m_missings.length; ++i) {
                    DataBlock m = new DataBlock(n);
                    m.set(m_missings[i], 1);
                    DataBlock dm = new DataBlock(n - nur);
                    m_ur.filter(m, dm);
                    m_dregs.addX(dm);
                }
            }
            for (int i = 0; i < m_x.size(); ++i) {
                DataBlock dx = new DataBlock(n - nur);
                m_ur.filter(m_x.get(i), dx);
                m_dregs.addX(dx);
            }
            m_dregs.setMeanCorrection(m_bmean);
        }
    }

    /**
     * 
     * @param n
     * @return
     */
    public double[] calcMeanReg(final int n) {
        double[] m = new double[n];

        double[] D = m_ur.getWeights();
        int d = D.length - 1;
        m[d] = 1;
        for (int i = d + 1; i < n; ++i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m[i - j] * D[d - j];
            }
            m[i] = s;
        }
        return m;
    }

    /**
     * 
     * @param b
     * @return
     */
    public DataBlock calcRes(final IReadDataBlock b) {
        if (b.getLength() != this.getVarsCount()) {
            return null;
        }

        DataBlock res = m_y.deepClone();
        int idx = 0;
        if (m_bmean) {
            double m = -b.get(idx++);
            res.addAY(m, new DataBlock(calcMeanReg(m_y.getLength())));
        }

        for (int i = 0; i < m_x.size(); ++i) {
            double c = -b.get(idx++);
            res.addAY(c, m_x.get(i));
        }
        return res;
    }

    /**
     *
     */
    protected abstract void calstationarymodel();

    /**
     *
     */
    public void clearX() {
        m_x.clear();
        m_dregs = null;
    }

    @Override
    protected AbstractRegArimaModel clone() {
        try {
            AbstractRegArimaModel model = (AbstractRegArimaModel) super.clone();
            if (m_dregs != null) {
                model.m_dregs = m_dregs.clone();
            }
            return model;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     * 
     * @return
     */
    public BackFilter getDifferencingFilter() {
        if (m_ur == null) {
            calstationarymodel();
        }
        return m_ur;
    }

    /**
     * 
     * @return
     */
    public RegModel getDModel() {
        if (m_dregs == null) {
            calcdregs();
        }
        return m_dregs;
    }

    /**
     * 
     * @return
     */
    public int[] getMissings() {
        return m_missings;
    }

    /**
     * 
     * @return
     */
    public int getMissingsCount() {
        return m_missings == null ? 0 : m_missings.length;
    }

    /**
     * 
     * @return
     */
    public int getObsCount() {
        return m_y.getLength();
    }

    /**
     * 
     * @return
     */
    public int getVarsCount() {
        int n = m_x.size();
        if (m_bmean) {
            ++n;
        }
        if (m_missings != null) {
            n += m_missings.length;
        }
        return n;
    }

    /**
     * 
     * @return
     */
    public int getXCount() {
        return m_x.size();
    }

    /**
     * 
     * @return
     */
    public DataBlock getY() {
        return m_y;
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void insertX(final int pos, final DataBlock x) {
        m_x.add(pos, x);
        if (m_dregs != null) {
            int del = m_missings == null ? 0 : m_missings.length;
            if (m_dregs != null) {
                if (m_ur.getLength() == 1) {
                    m_dregs.insertX(pos + del, x);
                } else {
                    DataBlock dx = new DataBlock(m_y.getLength() - m_ur.getDegree());
                    m_ur.filter(x, dx);
                    m_dregs.insertX(pos, x);
                }
            }
        }
    }

    /**
     * 
     * @return
     */
    public boolean isMeanCorrection() {
        return m_bmean;
    }

    /**
     * 
     * @param idx
     */
    public void removeX(final int idx) {
        m_x.remove(idx);
        int del = m_missings == null ? 0 : m_missings.length;
        if (m_dregs != null) {
            m_dregs.removeX(idx + del);
        }
    }

    /**
     * 
     * @param i0
     * @param n
     */
    public void removeX(final int i0, final int n) {
        int del = m_missings == null ? 0 : m_missings.length;
        for (int i = i0; i < i0 + n; i++) {
            m_x.remove(i);
            if (m_dregs != null) {
                m_dregs.removeX(i + del);
            }
        }
    }

    /**
     * 
     * @param value
     */
    public void setMeanCorrection(final boolean value) {
        m_bmean = value;
        if (m_dregs != null) {
            m_dregs.setMeanCorrection(value);
        }
    }

    /**
     * 
     * @param value
     */
    public void setMissings(final int[] value) {
        m_missings = value;
        m_dregs = null;
    }

    /**
     * 
     * @param value
     */
    public void setY(final DataBlock value) {
        m_y = value;
        if (m_dregs != null) {
            int n = m_y.getLength();
            if (m_ur == null) {
                calstationarymodel();
            }
            if (m_ur.getLength() == 1) {
                m_dregs.setY(m_y);
            } else {
                int nur = m_ur.getDegree();
                DataBlock dy = new DataBlock(m_y.getLength() - nur);
                m_ur.filter(m_y, dy);
                m_dregs.setY(dy);
            }
        }
    }

    /**
     * 
     * @param idx
     * @return
     */
    public DataBlock X(final int idx) {
        return m_x.get(idx);
    }
}
