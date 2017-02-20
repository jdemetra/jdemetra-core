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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.TableOfBoolean;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.IRobustStandardDeviationComputer;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.DoubleList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Beta)
public abstract class AbstractSingleOutlierDetector<T extends IArimaModel> {

    protected final IRobustStandardDeviationComputer sdevComputer;
    private final ArrayList<IOutlierFactory> m_o = new ArrayList<>();
    private final DoubleList m_ow = new DoubleList();
    private RegArimaModel<T> m_model;
    private TsDomain m_domain;
    private int m_lbound, m_ubound;
    private Matrix m_T, m_c;

    private TableOfBoolean m_bT;

    private int m_posmax = -1, m_omax = -1;

    /**
     *
     * @param sdevComputer
     */
    public AbstractSingleOutlierDetector(IRobustStandardDeviationComputer sdevComputer) {
        this.sdevComputer = sdevComputer;
    }

    /**
     *
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o) {
        m_o.add(o);
        m_ow.add(1);
        clear(true);
    }

    /**
     *
     * @param o
     * @param weight
     */
    public void addOutlierFactory(IOutlierFactory o, double weight) {
        m_o.add(o);
        m_ow.add(weight);
        clear(true);
    }

    /**
     * @return the sdevComputer
     */
    public IRobustStandardDeviationComputer getStandardDeviationComputer() {
        return sdevComputer;
    }

    /**
     *
     * @return
     */
    protected abstract boolean calc();

    /**
     *
     * @param all
     */
    protected void clear(boolean all) {
        sdevComputer.reset();
        m_model = null;
        m_omax = -1;
        m_posmax = -1;
        if (all) {
            m_T = null;
            m_c = null;
            m_bT = null;
        } else if (m_T != null) {
            m_T.clear();
            m_c.clear();
        }
    }

    /**
     *
     */
    public void clearOutlierFactories() {
        m_o.clear();
        m_ow.clear();
        clear(true);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double coeff(int pos, int outlier) {
        return m_c.get(pos, outlier);
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void exclude(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < m_bT.getRowsCount()) {
            m_bT.set(pos, ioutlier, false);
            m_T.set(pos, ioutlier, 0);
        }
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void allow(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < m_bT.getRowsCount()) {
            m_bT.set(pos, ioutlier, true);
            m_T.set(pos, ioutlier, 0);
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < m_o.size(); ++j) {
                exclude(pos[i], j);
            }
        }
    }

    /**
     *
     * @param pos
     */
    public void allow(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < m_o.size(); ++j) {
                allow(pos[i], j);
            }
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int pos) {
        for (int j = 0; j < m_o.size(); ++j) {
            exclude(pos, j);
        }
    }

    /**
     *
     * @param o
     */
    public void exclude(IOutlierVariable o) {
            TsPeriod start = new TsPeriod(m_domain.getFrequency(), o.getPosition());
        for (int i = 0; i < m_o.size(); ++i) {
            IOutlierFactory exemplar = m_o.get(i);
            if (exemplar.getOutlierCode().equals(o.getCode())) {
                int pos = start.minus(m_domain.getStart());
                exclude(pos, i);
                break;
            }
        }
    }

    /**
     *
     * @param o
     */
    public void allow(IOutlierVariable o) {
        for (int i = 0; i < m_o.size(); ++i) {
            IOutlierFactory exemplar = m_o.get(i);
            TsPeriod start = new TsPeriod(m_domain.getFrequency(), o.getPosition());
            if (exemplar.getOutlierCode().equals(o.getCode())) {
                int pos = start.minus(m_domain.getStart());
                allow(pos, i);
                break;
            }
        }
    }

    /**
     *
     * @param outliers
     */
    public void exclude(IOutlierVariable[] outliers) {
        for (IOutlierVariable o : outliers) {
            exclude(o);
        }
    }

    /**
     *
     * @param outliers
     */
    public void exclude(Iterator<IOutlierVariable> outliers) {
        while (outliers.hasNext()) {
            exclude(outliers.next());
        }
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void exclude(TsPeriod pos, int ioutlier) {
        int r = pos.minus(m_domain.getStart());
        if (r >= 0) {
            exclude(r, ioutlier);
        }
    }

    /**
     *
     * @return
     */
    public TsDomain getDomain() {
        return m_domain;
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return m_lbound;
    }

    /**
     *
     * @return
     */
    public double getMAD() {
        return sdevComputer.get();
    }

    /**
     *
     * @return
     */
    public IOutlierVariable getMaxOutlier() {
        if (m_posmax == -1) {
            searchMax();
        }
        if (m_omax == -1) {
            return null;
        }
        return m_o.get(m_omax).create(m_domain.get(m_posmax));
    }

    /**
     *
     * @return
     */
    public int getMaxOutlierType() {
        if (m_omax == -1) {
            searchMax();
        }
        return m_omax;
    }

    /**
     *
     * @return
     */
    public int getMaxPosition() {
        if (m_posmax == -1) {
            searchMax();
        }
        return m_posmax;
    }

    /**
     *
     * @return
     */
    public double getMaxTStat() {
        if (m_omax == -1) {
            searchMax();
        }
        double tmax = T(m_posmax, m_omax);
        return tmax;
    }

    /**
     *
     * @return
     */
    public RegArimaModel<T> getModel() {
        return m_model;
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return m_o.size();
    }

    /**
     *
     * @param i
     * @return
     */
    public IOutlierFactory getOutlierFactory(int i) {
        return m_o.get(i);
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        return m_ubound;
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public boolean isDefined(int pos, int outlier) {
        return m_bT.get(pos, outlier);
    }

    /**
     *
     * @param estimationdomain
     * @param outliersdomain
     */
    public void prepare(TsDomain estimationdomain, TsDomain outliersdomain) {
        m_domain = estimationdomain;
        if (outliersdomain == null) {
            m_lbound = 0;
            m_ubound = estimationdomain.getLength();
        } else {
            TsDomain common = estimationdomain.intersection(outliersdomain);
            m_lbound = common.getStart().minus(estimationdomain.getStart());
            m_ubound = m_lbound + common.getLength();
        }

        prepareT(estimationdomain.getLength());
    }

    /**
     *
     * @param n
     */
    protected void prepareT(int n) {
        m_T = new Matrix(n, m_o.size());
        m_c = new Matrix(n, m_o.size());
        m_bT = new TableOfBoolean(n, m_o.size());
        for (int i = 0; i < m_o.size(); ++i) {
            IOutlierFactory fac = getOutlierFactory(i);
            TsDomain dom = fac.definitionDomain(m_domain);
            int jstart = Math.max(m_lbound, dom.getStart().minus(m_domain.getStart()));
            int jend = Math.min(m_ubound, dom.getEnd().minus(m_domain.getStart()));
            for (int j = jstart; j < jend; ++j) {
                m_bT.set(j, i, true);
            }
        }
    }

    /**
     *
     * @param model
     * @return
     */
    public boolean process(RegArimaModel<T> model) {
        clear(false);
        m_model = model.clone();
        return calc();
    }

    private void searchMax() {
        if (m_T == null) {
            return;
        }
        double max = 0;
        int imax = -1;
        double[] T = m_T.internalStorage();
        for (int i = 0, c = 0; c < m_T.getColumnsCount(); ++c) {
            double w = m_ow.get(c);

            for (int r = 0; r < m_T.getRowsCount(); ++r, ++i) {
                double cur = Math.abs(T[i]) * w;
                if (cur > max) {
                    imax = i;
                    max = cur;
                }
            }
            if (imax == -1) {
                return;
            }
        }
        m_posmax = imax % m_T.getRowsCount();
        m_omax = imax / m_T.getRowsCount();
    }

    /**
     *
     * @param pos
     * @param outlier
     * @param val
     */
    protected void setT(int pos, int outlier, double val) {
        m_T.set(pos, outlier, val);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @param val
     */
    protected void setCoefficient(int pos, int outlier, double val) {
        m_c.set(pos, outlier, val);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double T(int pos, int outlier) {
        return m_T.get(pos, outlier);
    }

}
