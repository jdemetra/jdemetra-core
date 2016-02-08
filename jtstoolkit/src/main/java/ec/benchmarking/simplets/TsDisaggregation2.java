/*
* Copyright 2016 National Bank of Belgium
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
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class TsDisaggregation2 {

    public static enum Model {

        Wn,
        Ar1,
        Rw,
        RwAr1,
        I2, I3;

        public boolean hasParameter() {
            return this == Ar1 || this == RwAr1;
        }

        public boolean isStationary() {
            return this == Ar1 || this == Wn;
        }

        public int getParametersCount() {
            return (this == Ar1 || this == RwAr1) ? 1 : 0;
        }

        public int getDifferencingOrder() {
            switch (this) {
                case Rw:
                case RwAr1:
                    return 1;
                case I2:
                    return 2;
                case I3:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    public static final double DEF_EPS = 1e-5;
    private Model model = Model.Ar1;
    private boolean constant = true, trend;
    private boolean log, diffuseRegressors, zeroinit, ml = true;
    private Parameter p = new Parameter();
    private double truncated = 0;
    private TsAggregationType type = TsAggregationType.Sum;
    private TsDisaggregation.SsfOption option = TsDisaggregation.SsfOption.DKF;
    private double eps = DEF_EPS;
    private TsPeriodSelector span = new TsPeriodSelector();
    private TsFrequency defaultFrequency = TsFrequency.Quarterly;
    private TsDisaggregation<? extends ISsf> result;
    private int nindicators;

    public boolean process(TsData y, TsVariableList x) {
        DisaggregationModel dmodel = prepare(y, x);
        if (dmodel == null) {
            return false;
        }
        TsDisaggregation<? extends ISsf> disagg;

        switch (model) {
            case Ar1:
                disagg = initChowLin();
                break;
            case Wn:
                disagg = initOLS();
                break;
            case Rw:
                disagg = initFernandez();
                break;
            case RwAr1:
                disagg = initLitterman();
                break;
            default:
                disagg = initI(model.getDifferencingOrder());
        }

        disagg.useML(ml);
        disagg.calculateVariance(
                true);
        disagg.setSsfOption(option);
        if (diffuseRegressors) {
            disagg.setDiffuseRegressorsCount(dmodel.getX().getVariablesCount());
        }
        disagg.setEpsilon(eps);
        if (!disagg.process(dmodel, null)) {
            return false;
        } else {
            int n = dmodel.getX().getVariablesCount();
            if (isConstant()) {
                --n;
            }
            if (isTrend()) {
                --n;
            }
            result = disagg;
            nindicators = n;
            return true;
        }
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return result.getLikelihood();
    }

    public LikelihoodStatistics getLikelihoodStatistics() {
        DiffuseConcentratedLikelihood ll = result.getLikelihood();
        IParametricMapping<? extends ISsf> mapping = result.getMapping();
        return LikelihoodStatistics.create(ll, ll.getN(), mapping == null ? 0 : mapping.getDim(), 0);
    }

    public IFunction getEstimationFunction() {
        return result.getEstimationFunction();
    }

    public IFunctionInstance getMin() {
        return result.getMin();
    }

    public Parameter getEstimatedParameter() {
        Matrix i = result.getObservedInformation();
        if (i == null) {
            return null;
        }
        IParametricMapping<ISsf> mapping = (IParametricMapping<ISsf>) result.getMapping();
        IReadDataBlock p = mapping.map(result.getEstimatedSsf());
        Parameter x = new Parameter(p.get(0), ParameterType.Estimated);
        x.setStde(Math.sqrt(1 / i.get(0, 0)));
        return x;
    }

    public ISsf getEstimatedSsf() {
        return result.getEstimatedSsf();
    }

    public void reset() {
        result = null;
        nindicators = 0;
        model = Model.Ar1;
        constant = true;
        trend = false;
        log = false;
        diffuseRegressors = false;
        zeroinit = false;
        ml = true;
        p = new Parameter();
        type = TsAggregationType.Sum;
        option = TsDisaggregation.SsfOption.DKF;
        eps = DEF_EPS;
        span = new TsPeriodSelector();
    }

    /**
     * @return the type
     */
    public TsAggregationType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(TsAggregationType type) {
        this.type = type;
    }

    /**
     * @return the eps
     */
    public double getEpsilon() {
        return eps;
    }

    /**
     * @param eps the eps to set
     */
    public void setEpsilon(double eps) {
        this.eps = eps;
    }

    /**
     * @return the zeroinit
     */
    public boolean isZeroInitialization() {
        return zeroinit;
    }

    /**
     * @param zeroinit the zeroinit to set
     */
    public void setZeroInitialization(boolean zeroinit) {
        this.zeroinit = zeroinit;
    }

    public boolean isML() {
        return ml;
    }

    public void setML(boolean ml) {
        this.ml = ml;
    }

    /**
     * @return the p
     */
    public Parameter getParameter() {
        return p;
    }

    /**
     * @param p the p to set
     */
    public void setParameter(Parameter p) {
        this.p = p;
    }

    /**
     * @return the span
     */
    public TsPeriodSelector getSpan() {
        return span;
    }

    /**
     * @param span the span to set
     */
    public void setSpan(TsPeriodSelector span) {
        if (span == null) {
            throw new AssertionError("Span should not be null");
        }
        this.span = span;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * @param aModel the model to set
     */
    public void setModel(Model aModel) {
        model = aModel;
    }

    /**
     * @return the constant
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * @param constant the constant to set
     */
    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    /**
     * @return the trend
     */
    public boolean isTrend() {
        return trend;
    }

    /**
     * @param trend the trend to set
     */
    public void setTrend(boolean trend) {
        this.trend = trend;
    }

    /**
     * @return the log
     */
    public boolean isLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    /**
     * @return the diffuseRegressors
     */
    public boolean isDiffuseRegression() {
        return diffuseRegressors;
    }

    /**
     * @param diffuseRegressors the diffuseRegressors to set
     */
    public void setDiffuseRegression(boolean diffuseRegressors) {
        this.diffuseRegressors = diffuseRegressors;
    }

    public double getTruncatedRho() {
        return truncated;
    }

    public void setTruncatedRho(double lrho) {
        if (lrho > 0 || lrho < -1) {
            throw new IllegalArgumentException("Truncated value should be in [-1,0]");
        }
        truncated = lrho;
    }

    /**
     * @return the option
     */
    public TsDisaggregation.SsfOption getOption() {
        return option;
    }

    /**
     * @param option the option to set
     */
    public void setOption(TsDisaggregation.SsfOption option) {
        this.option = option;
    }

    public TsFrequency getDefaultFrequency() {
        return defaultFrequency;
    }

    /**
     * @param freq
     */
    public void setDefaultFrequency(TsFrequency freq) {
        this.defaultFrequency = freq;
    }

    private TsDisaggregation<SsfAr1> initChowLin() {
        TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
        SsfAr1 ssf = new SsfAr1();
        Parameter p = getParameter();
        if (p != null && p.isFixed()) {
            ssf.setRho(p.getValue());
        } else {
            disagg.setMapping(new SsfAr1.Mapping(isZeroInitialization(), getTruncatedRho(), 1));
        }
        ssf.useZeroInitialization(isZeroInitialization());
        disagg.setSsf(ssf);
        return disagg;
    }

    private TsDisaggregation<SsfAr1> initOLS() {
        TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
        SsfAr1 ssf = new SsfAr1();
        ssf.setRho(0);
        disagg.setSsf(ssf);
        return disagg;
    }

    private TsDisaggregation<SsfRwAr1> initLitterman() {
        TsDisaggregation<SsfRwAr1> disagg = new TsDisaggregation<>();
        SsfRwAr1 ssf = new SsfRwAr1();
        Parameter p = getParameter();
        if (p != null && p.isFixed()) {
            ssf.setRho(p.getValue());
        } else {
            disagg.setMapping(new SsfRwAr1.Mapping(isZeroInitialization(), getTruncatedRho(), 1));
        }
        ssf.useZeroInitialization(isZeroInitialization());
        disagg.setSsf(ssf);
        return disagg;
    }

    private TsDisaggregation<SsfRw> initFernandez() {
        TsDisaggregation<SsfRw> disagg = new TsDisaggregation<>();
        SsfRw ssf = new SsfRw();
        ssf.useZeroInitialization(isZeroInitialization());
        disagg.setSsf(ssf);
        return disagg;
    }

    private TsDisaggregation<SsfArima> initI(int diff) {
        TsDisaggregation<SsfArima> disagg = new TsDisaggregation<>();
        ArimaModel sarima = new ArimaModel(null, new BackFilter(UnitRoots.D(1, diff)), null, 1);
        SsfArima ssf = new SsfArima(sarima);
        disagg.setSsf(ssf);
        return disagg;
    }

    private DisaggregationModel prepare(final TsData y, TsVariableList x) {
        if (y == null) {
            return null;
        }
        TsData yc = y.select(getSpan());
        DisaggregationModel model = new DisaggregationModel(defaultFrequency);
        model.setY(yc);
        TsVariableList xc;
        if (x == null || x.isEmpty()) {
            if (defaultFrequency == TsFrequency.Undefined || !yc.getFrequency().contains(defaultFrequency)) {
                return null;
            } else {
                model.setDefaultForecastCount(defaultFrequency.intValue());
                xc = new TsVariableList();
            }
        } else {
            xc = x.clone();
        }
        if (isConstant() && (getModel().isStationary() || isZeroInitialization())) {
            xc.add(new Constant());
        }
        if (isTrend()) {
            xc.add(new LinearTrend(y.getStart().firstday()));
        }
        if (!xc.isEmpty()) {
            model.setX(xc);
        }
        model.setAggregationType(getType());
        return model;
    }
    
    public TsData getDisaggregtedSeries(){
        return result == null ? null : result.getSmoothedSeries();
    }

    public TsData getDisaggregtedSeriesVariance(){
        return result == null ? null : result.getSmoothedSeriesVariance();
    }
}
