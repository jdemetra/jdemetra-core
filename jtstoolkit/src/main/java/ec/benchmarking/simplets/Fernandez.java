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

package ec.benchmarking.simplets;

import ec.benchmarking.DisaggregationModel;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@Deprecated
public class Fernandez {

    public Fernandez() {
    }

    public boolean process(TsData y, TsFrequency freq, int nfcasts) {
        clear();
        int yfreq = y.getFrequency().intValue();
        int ifreq = freq.intValue();
        if (ifreq % yfreq != 0) {
            return false;
        }
        int conv = ifreq / yfreq;
        DisaggregationModel model = new DisaggregationModel(freq);
        model.setY(y);
        if (m_trend) {
            model.getX().add(new LinearTrend(y.getStart().firstday()));
        }
        model.setAggregationType(m_type);

        TsDisaggregation<SsfRw> disagg = algorithm();

        TsDomain ndom = new TsDomain(freq, y.getStart().getYear(), y.getStart().getPosition() * conv, y.getLength() * conv + nfcasts);

        if (!disagg.process(model, ndom)) {
            return false;
        }

        analyse(disagg);
        return m_ll != null;
    }

    public boolean process(TsData y, TsVariableList x) {
        clear();
        if (x == null) {
            return false;
        }
        DisaggregationModel model = new DisaggregationModel(TsFrequency.Undefined);
        model.setY(y);
        if (m_trend) {
            model.getX().add(new LinearTrend(y.getStart().firstday()));
        }
        for (ITsVariable var : x.items()) {
            model.getX().add(var);
        }
        model.setAggregationType(m_type);

        int xfreq = x.getFrequency().intValue();
        int yfreq = y.getFrequency().intValue();
        if (xfreq == 0 || xfreq % yfreq != 0) {
            return false;
        }
        TsDisaggregation<SsfRw> disagg = algorithm();

        if (!disagg.process(model, null)) {
            return false;
        }

        analyse(disagg);

        return m_ll != null;
    }

    private void analyse(TsDisaggregation<SsfRw> disagg) {
        m_s = disagg.getSmoothedSeries();
        m_es = disagg.getSmoothedSeriesVariance().sqrt();
        m_ll = disagg.getLikelihood();
        m_res = disagg.getFullResiduals();
    }

    private TsDisaggregation<SsfRw> algorithm() {
        SsfRw rw = new SsfRw();
        rw.useZeroInitialization(m_zinit);

        TsDisaggregation<SsfRw> disagg = new TsDisaggregation<>();
        disagg.setSsf(rw);
        disagg.calculateVariance(true);
        return disagg;
    }

    private void clear() {
        m_s = null;
        m_es = null;
        m_ll = null;
    }
    private TsData m_res;
    private TsData m_s, m_es;
    private TsAggregationType m_type = TsAggregationType.Sum;
    private boolean m_zinit;
    private boolean m_trend;
    private DiffuseConcentratedLikelihood m_ll;

    public TsData getResiduals() {
        return m_res;
    }

    public boolean isTrend() {
        return m_trend;
    }

    public void setTrend(boolean value) {
        m_trend = value;
    }

    public TsAggregationType getAggregationType() {
        return m_type;
    }

    public void setAggregationType(TsAggregationType value) {
        m_type = value;
    }

    public boolean isZeroInitialization() {
        return m_zinit;
    }

    public void setZeroInitialization(boolean value) {
        m_zinit = value;
    }

    public LikelihoodStatistics getLikelihoodStatistics() {
        if (m_ll == null) {
            return null;
        }
        LikelihoodStatistics stats = LikelihoodStatistics.create(m_ll, m_ll.getN(), 0, 0);
        return stats;
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return m_ll;
    }

    public TsData getDisaggregatedSeries() {
        return m_s;
    }

    public TsData getDisaggregatedSeriesStde() {
        return m_es;
    }
}
