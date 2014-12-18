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
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.GridSearch;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 * Implementation of the Chow-Lin disaggregation model
 * y(t) = X(t)b + e(t)
 * e(t) = rho*e(t-1)
 * e(-1) unknown (default) or 0
 * sum(y(t in T))=Y(T)
 * 
 * The current implementation uses the ordinary Kalman smoother
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ChowLin {

    private TsData m_res;
    private TsData m_s, m_es;
    private TsAggregationType m_type = TsAggregationType.Sum;
    private boolean m_zinit;
    private boolean m_diffuseregs;
    private boolean ml_ = true;
    private boolean const_ = true, trend_;
    private DiffuseConcentratedLikelihood ll_;
    private double rho_, stdeRho_;
    private double eps_ = 1e-9;
    private IFunction fn_;

    /**
     * Creates a new ChowLin algorithm
     */
    public ChowLin() {
    }

    /**
     * Process temporal disaggregation without regression variables
     * @param y The aggregated data
     * @param freq The new frequency
     * @param nfcasts The number of forecasts
     * @return True if the disaggregation was successful, false otherwise.
     */
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
        if (const_) {
            model.getX().add(new Constant());
        }
        if (trend_) {
            model.getX().add(new LinearTrend(y.getStart().firstday()));
        }
        model.setAggregationType(m_type);

        TsDisaggregation<SsfAr1> disagg = algorithm();
        disagg.useML(ml_);

        TsDomain ndom = new TsDomain(freq, y.getStart().getYear(),
                y.getStart().getPosition() * conv, y.getLength() * conv + nfcasts);

        if (!disagg.process(model, ndom)) {
            return false;
        }

        analyse(disagg);
        return ll_ != null;
    }

    /**
     * Process temporal disaggregation with regression variables
     * @param y The aggregated data
     * @param x The regression variables. They should have the same (or compatible) frequency
     * @return True if the disaggregation was successful, false otherwise.
     */
    public boolean process(TsData y, TsVariableList x) {
        clear();
        if (x == null) {
            return false;
        }
        DisaggregationModel model = new DisaggregationModel(TsFrequency.Undefined);
        model.setY(y);
        if (const_) {
            model.getX().add(new Constant());
        }
        if (trend_) {
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
        TsDisaggregation<SsfAr1> disagg = algorithm();

        if (!disagg.process(model, null)) {
            return false;
        }

        analyse(disagg);

        return ll_ != null;
    }

    private void analyse(TsDisaggregation<SsfAr1> disagg) {
        m_s = disagg.getSmoothedSeries();
        m_es = disagg.getSmoothedSeriesVariance().sqrt();
        rho_ = disagg.getEstimatedSsf().getRho();
        Matrix H = disagg.getObservedInformation();
        if (H != null) {
            stdeRho_ = Math.sqrt(1 / H.get(0, 0));
        }
        fn_ = disagg.getEstimationFunction();
        ll_ = disagg.getLikelihood();
        m_res = disagg.getFullResiduals();
    }

    private TsDisaggregation<SsfAr1> algorithm() {
        SsfAr1 ar1 = new SsfAr1(.1);
        ar1.useZeroInitialization(m_zinit);

        TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
        disagg.setSsf(ar1);
        GridSearch gsearch = new GridSearch();
        gsearch.setConvergenceCriterion(eps_);
        disagg.setMinimizer(gsearch);
        disagg.setMapping(new SsfAr1.Mapping(m_zinit));
        disagg.calculateVariance(true);
        disagg.useML(ml_);
        if (m_diffuseregs) {
            disagg.setDiffuseRegressorsCount(-1);
        }
        return disagg;
    }

    private void clear() {
        m_s = null;
        m_es = null;
        ll_ = null;
        fn_ = null;
        stdeRho_=0;
    }
    
    public boolean isConstant() {
        return const_;
    }

    public void setConstant(boolean value) {
        const_ = value;
    }

    public boolean isTrend() {
        return trend_;
    }

    public void setTrend(boolean value) {
        trend_ = value;
    }

    public double getPrecision() {
        return eps_;
    }

    public void setPrecision(double value) {
        eps_ = value;
    }

    public boolean isMaximumLikelihood() {
        return ml_;
    }

    public void setMaximumLikelihood(boolean value) {
        ml_ = value;
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

    public double getRho() {
        return rho_;
    }

    public double getRhoStde() {
        return stdeRho_;
    }

    public LikelihoodStatistics getLikelihoodStatistics() {
        if (ll_ == null) {
            return null;
        }
        LikelihoodStatistics stats = LikelihoodStatistics.create(ll_, ll_.getN(), 1, 0);
        return stats;
    }

    public TsData getResiduals() {
        return m_res;
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return ll_;
    }

    public IFunction getLikelihoodFunction() {
        return fn_;
    }

    /**
     * Gets the disaggregated series
     * @return The disaggregated series (internal object)
     */
    public TsData getDisaggregatedSeries() {
        return m_s;
    }

    /**
     * Gets the standard error of the disaggregated series
     * @return The standard error of the disaggregated series (internal object)
     */
    public TsData getDisaggregatedSeriesStde() {
        return m_es;
    }
}
