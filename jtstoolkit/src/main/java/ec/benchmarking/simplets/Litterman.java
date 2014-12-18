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
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.GridSearch;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author pcuser
 */
public class Litterman {

    public Litterman() {
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
        
        TsDisaggregation<SsfRwAr1> disagg = algorithm();
        disagg.useML(m_ml);
        TsDomain ndom = new TsDomain(freq, y.getStart().getYear(), y.getStart().getPosition() * conv,
                y.getLength() * conv + nfcasts);
        
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
        int conv = xfreq / yfreq;
        TsDisaggregation<SsfRwAr1> disagg = algorithm();
        
        if (!disagg.process(model, null)) {
            return false;
        }
        analyse(disagg);
        
        return m_ll != null;
    }
    
    private void analyse(TsDisaggregation<SsfRwAr1> disagg) {
        m_s = disagg.getSmoothedSeries();
        m_es = disagg.getSmoothedSeriesVariance().sqrt();
        m_ro = disagg.getEstimatedSsf().getRho();
        Matrix H = disagg.getObservedInformation();
        if (H != null) {
            m_stdero = Math.sqrt(1 / H.get(0, 0));
        }
        m_fn = disagg.getEstimationFunction();
        m_ll = disagg.getLikelihood();
        m_res = disagg.getFullResiduals();
    }
    
    private TsDisaggregation<SsfRwAr1> algorithm() {
        SsfRwAr1 rwar1 = new SsfRwAr1(-.1);
        rwar1.useZeroInitialization(m_zinit);
        
        TsDisaggregation<SsfRwAr1> disagg = new TsDisaggregation<>();
        disagg.setSsf(rwar1);
        GridSearch gsearch = new GridSearch();
        gsearch.setConvergenceCriterion(m_precision);
        disagg.setMinimizer(gsearch);
        disagg.setMapping(new SsfRwAr1.Mapping(m_zinit));
        disagg.calculateVariance(true);
        disagg.useML(m_ml);
        if (m_diffuseregs) {
            disagg.setDiffuseRegressorsCount(-1);
        }
        return disagg;
    }
    
    private void clear() {
        m_s = null;
        m_es = null;
        m_ll = null;
        m_fn = null;
    }
    
    private TsData m_res;
    private TsData m_s, m_es;
    private TsAggregationType m_type = TsAggregationType.Sum;
    private boolean m_zinit;
    private boolean m_diffuseregs;
    private boolean m_ml = true;
    private boolean m_trend;
    private DiffuseConcentratedLikelihood m_ll;
    private double m_ro, m_stdero;
    private double m_precision = 1e-9;
    private IFunction m_fn;
    
    public boolean isTrend() {
        return m_trend;
    }
    
    public void setTrend(boolean value) {
        m_trend = value;
    }
    
    public double getPrecision() {
        return m_precision;
    }
    
    public void setPrecision(double value) {
        m_precision = value;
    }
    
    public boolean isML() {
        return m_ml;
    }
    
    public void setML(boolean value) {
        m_ml = value;
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
    
    public boolean isDiffuseRegressionCoefficients() {
        return m_diffuseregs;
    }
    
    public void setDiffuseRegressionCoefficients(boolean value) {
        m_diffuseregs = value;
    }
    
    public double getRo() {
        return m_ro;
    }
    
    public double getStdeRo() {
        return m_stdero;
    }
    
    public LikelihoodStatistics getLikelihoodStatistics() {
        if (m_ll == null) {
            return null;
        }
        LikelihoodStatistics stats = LikelihoodStatistics.create(m_ll, m_ll.getN(), 1, 0);
        return stats;
    }
    
    public TsData getResiduals() {
        return m_res;
    }
    
    public DiffuseConcentratedLikelihood getLikelihood() {
        return m_ll;
    }
    
    public IFunction getLikelihoodFunction() {
        return m_fn;
    }
    
    public TsData getDisaggregatedSeries() {
        return m_s;
    }
    
    public TsData getDisaggregatedSeriesStde() {
        return m_es;
    }
}
