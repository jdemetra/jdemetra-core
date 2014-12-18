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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.arima.estimation.ArmaKF;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.IntList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SingleOutlierDetector2 {

    private ArrayList<IOutlierFactory> m_o = new ArrayList<>();
    private SarimaModel m_model, m_stmodel;
    private BackFilter m_ur;
    private TsDomain m_domain;
    private double m_mad;
    private int m_lbound, m_ubound;
    private int m_posmax = -1, m_omax = -1;
    private double m_tmax, m_c;
    private IntList m_excluded = new IntList();
    private double[] m_el;
    private boolean m_bmad = true;
    private double m_ss;

    /**
     *
     */
    public SingleOutlierDetector2() {
    }

    /**
     *
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o) {
        m_o.add(o);
        clear();
    }

    /**
     *
     * @param sty
     * @return
     */
    public boolean calc(IReadDataBlock sty) {
        if (!initialize(sty)) {
            return false;
        }
        m_c = 0;
        m_tmax = 0;
        for (int i = 0; i < getOutlierFactoriesCount(); ++i) {
            processOutlier(i);
        }
        return m_tmax > 0;
    }

    /**
     *
     */
    protected void clear() {
        m_mad = 0;
        m_omax = -1;
        m_posmax = -1;
    }

    /**
     *
     */
    public void clearOutlierFactories() {
        m_o.clear();
        clear();
    }

    /**
     *
     * @param excl
     */
    public void exclude(int[] excl) {
        if (excl == null) {
            return;
        }
        for (int i = 0; i < excl.length; ++i) {
            m_excluded.add(excl[i]);
        }
    }

    /**
     *
     * @param o
     */
    public void exclude(IOutlierVariable o) {
        m_excluded.add(o.getPosition().minus(m_domain.getStart()));
    }

    public void allow(IOutlierVariable o) {
        int pos = o.getPosition().minus(m_domain.getStart());
        int xpos = -1;
        for (int i = 0; i < m_excluded.size(); ++i) {
            if (m_excluded.get(i) == pos) {
                xpos = i;
                break;
            }
        }
        if (xpos >= 0) {
            m_excluded.remove(xpos);
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
        return m_mad;
    }

    /**
     *
     * @return
     */
    public double getMaxCoefficient() {
        return m_c;
    }

    /**
     *
     * @return
     */
    public IOutlierVariable getMaxOutlier() {
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
        return m_omax;
    }

    /**
     *
     * @return
     */
    public int getMaxPosition() {
        return m_posmax;
    }

    /**
     *
     * @return
     */
    public double getMaxTStat() {
        return m_tmax;
    }

    /**
     *
     * @return
     */
    public SarimaModel getModel() {
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
     * @param y
     * @return
     */
    public boolean initialize(IReadDataBlock y) {
        // compute smoothed residuals
        ArmaKF kf = new ArmaKF(m_stmodel);
        Likelihood ll = new Likelihood();
        if (!kf.process(y, ll)) {
            return false;
        }
        m_el = ll.getResiduals();
        setMAD(AbstractOutlierVariable.mad(new DataBlock(m_el), true));
        ll.getSsqErr();
        return true;

    }

    private boolean[] prepare(int i) {
        boolean[] ok = new boolean[m_domain.getLength()];
        IOutlierFactory fac = getOutlierFactory(i);
        TsDomain dom = fac.definitionDomain(m_domain);
        int jstart = dom.getStart().minus(m_domain.getStart());
        int jend = dom.getLast().minus(m_domain.getStart());
        for (int j = jstart; j <= jend; ++j) {
            ok[j] = true;
        }
        for (int j = 0; j < m_excluded.size(); j++) {
            ok[m_excluded.get(j)] = false;
        }
        return ok;
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
            m_lbound = outliersdomain.getStart().minus(common.getStart());
            m_ubound = m_lbound + common.getLength();
        }
        m_excluded.clear();
    }

    /**
     *
     * @param model
     * @param sty
     * @return
     */
    public boolean process(SarimaModel model, IReadDataBlock sty) {
        m_model = model;
        StationaryTransformation st = m_model.stationaryTransformation();
        m_stmodel = (SarimaModel) st.stationaryModel;
        m_ur = st.unitRoots;
        clear();
        return calc(sty);
    }

    private void processOutlier(int idx) {
        int nl = m_el.length;
        int d = m_ur.getDegree();
        int n = nl + d;
        double[] o = new double[2 * n - 1];
        double[] ol = new double[o.length];
        DataBlock O = new DataBlock(o);
        DataBlock OL = new DataBlock(ol);
        IOutlierVariable outlier = getOutlierFactory(idx).create(
                getDomain().getStart());
        TsPeriod ostart = getDomain().getStart().minus(n - 1);
        outlier.data(ostart, O);
        // OL = PHI(B)*O
        m_model.getAR().filter(O, OL);
        // TH(B) O = OL;
        m_model.getMA().solve(ol, o);

        // o contains the filtered outlier

        // we start at the end

        int xstart = 0, xend = o.length;
        for (int i = 0; i < o.length; ++i) {
            if (o[i] != 0) {
                xstart = i;
                break;
            }
        }
        for (int i = o.length; i > 0; --i) {
            if (o[i - 1] != 0) {
                xend = i;
                break;
            }
        }
        //double maxval = 0;
        double sxx = 0;
        for (int i = Math.max(d, xstart); i < Math.min(n, xend); ++i) {
            sxx += o[i] * o[i];
        }

        //int imax = Math.min(n, ub) - lb;

        boolean[] ok = prepare(idx);
        for (int ix = 0; ix < n; ++ix) {
            double rmse = rmse(n - ix - 1 - d);
            if (ok[n - 1 - ix]) {
                // compute sxy
                double sxy = 0;
                // ek goes from 0 to nl
                // if (ix == 0 (last position : n-1)), ok goes from d to n
                // in general, ok goes from d+ix to n+ix (in the range [xstart, xend[)
                int kstart = Math.max(xstart, d + ix), kend = Math.min(n+ix, xend);
                for (int k = kstart; k < kend; ++k) {
                    sxy += o[k] * m_el[k - d - ix];
                }
                double c = sxy / sxx;
                double val = c * Math.sqrt(sxx) / rmse;
                double aval = Math.abs(val);
                if (aval > m_tmax) {
                    m_tmax = aval;
                    m_c = c;
                    m_posmax = n - 1 - ix;
                    m_omax = idx;
                }
            }
            // update sxx
            if (ix < n-1){
                int z=n+ix;
                if (z < xend)
                    sxx+=o[z]*o[z];
                z-=nl;
                if (z >= xstart)
                    sxx-=o[z]*o[z];
            }
        }
    }

    /**
     *
     * @param i
     * @return
     */
    protected double rmse(int i) {
        if (m_bmad) {
            return getMAD();
        } else {
            if (m_ss == 0) {
                m_ss = new DataBlock(m_el).ssq();
            }
            if (i >= 0) {
                double ss = (m_ss - m_el[i] * m_el[i]) / (m_el.length - 1);
                return Math.sqrt(ss);
            } else {
                return Math.sqrt(m_ss / (m_el.length - 1));
            }
        }
    }

    /**
     *
     * @param value
     */
    public void setMAD(double value) {
        m_mad = value;
    }
}
