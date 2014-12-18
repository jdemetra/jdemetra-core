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
package ec.tss.disaggregation.documents;

import ec.benchmarking.simplets.TsDisaggregation;
import ec.benchmarking.simplets.TsDisaggregation.SsfOption;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Objects;

/**
 *
 * @author Jean
 */
public class DisaggregationSpecification implements IProcSpecification, Cloneable {

    public static enum Model {

        Wn,
        Ar1,
        Rw,
        RwAr1;

        public boolean hasParameter() {
            return this == Ar1 || this == RwAr1;
        }
        
        public boolean isStationary(){
            return this == Ar1 || this == Wn;
        }

        public int getParametersCount() {
            return (this == Ar1 || this == RwAr1) ? 1 : 0;
        }
    }
    public static final double DEF_EPS = 1e-5;
    private Model model = Model.Ar1;
    private boolean constant = true, trend;
    private boolean log, diffuseRegressors, zeroinit, ml = true;
    private Parameter p = new Parameter();
    private double truncated=0;
    private TsAggregationType type = TsAggregationType.Sum;
    private TsDisaggregation.SsfOption option = TsDisaggregation.SsfOption.DKF;
    private double eps = DEF_EPS;
    private TsPeriodSelector span = new TsPeriodSelector();
    private TsFrequency defaultFrequency = TsFrequency.Quarterly;

    public void reset() {
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
    
    public double getTruncatedRho(){
        return truncated;
    }
    
    public void setTruncatedRho(double lrho){
        if (lrho > 0 || lrho<-1)
            throw new IllegalArgumentException("Truncated value should be in [-1,0]");
        truncated=lrho;
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

    @Override
    public DisaggregationSpecification clone() {
        try {
            DisaggregationSpecification spec = (DisaggregationSpecification) super.clone();
            spec.span = span.clone();
            spec.p = p.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (span != null && span.getType() != PeriodSelectorType.All) {
            info.set(SPAN, span);
        }
        info.set(MODEL, model.name());
        if (p != null && p.getType() != ParameterType.Undefined) {
            info.set(PARAMETER, p);
        }
        info.set(AGGTYPE, type.name());
        info.set(CONSTANT, constant);
        info.set(TREND, trend);
        info.set(FREQ, defaultFrequency.name());
        if (option != SsfOption.DKF) {
            info.set(SSF, option.name());
        }
        if (zeroinit || verbose) {
            info.set(ZEROINIT, zeroinit);
        }
        if (log || verbose) {
            info.set(LOG, log);
        }
        if (diffuseRegressors || verbose) {
            info.set(DIFFUSEREGS, diffuseRegressors);
        }
        if (!ml || verbose) {
            info.set(ML, ml);
        }
        if (eps != DEF_EPS || verbose) {
            info.set(EPS, eps);
        }
        if (truncated != 0 || verbose)
            info.set(TRUNCATED, truncated);
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        reset();
        TsPeriodSelector sel = info.get(SPAN, TsPeriodSelector.class);
        if (sel != null) {
            span = sel;
        }
        String n = info.get(MODEL, String.class);
        if (n == null) {
            return false;
        }
        model = Model.valueOf(n);
        n = info.get(FREQ, String.class);
        if (n != null) {
            defaultFrequency = TsFrequency.valueOf(n);
        }
        Parameter np = info.get(PARAMETER, Parameter.class);
        if (np != null) {
            p = np;
        }
        n = info.get(AGGTYPE, String.class);
        if (n == null) {
            return false;
        }
        type = TsAggregationType.valueOf(n);
        Boolean b = info.get(CONSTANT, Boolean.class);
        if (b != null) {
            constant = b;
        }
        b = info.get(TREND, Boolean.class);
        if (b != null) {
            trend = b;
        }
        n = info.get(option, String.class);
        if (n != null) {
            option = SsfOption.valueOf(n);
        }
        b = info.get(ZEROINIT, Boolean.class);
        if (b != null) {
            zeroinit = b;
        }
        b = info.get(LOG, Boolean.class);
        if (b != null) {
            log = b;
        }
        b = info.get(DIFFUSEREGS, Boolean.class);
        if (b != null) {
            diffuseRegressors = b;
        }
        b = info.get(ML, Boolean.class);
        if (b != null) {
            ml = b;
        }
        Double t=info.get(TRUNCATED, Double.class);
        if (t != null)
            truncated=t;
        Double e = info.get(EPS, Double.class);
        if (e != null) {
            eps = e;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (model == Model.Ar1) {
            builder.append("Chow-Lin");
        } else if (model == Model.Wn) {
            builder.append("OLS");
        } else if (model == Model.RwAr1) {
            builder.append("Litterman");
        } else {
            builder.append("Fernandez");
        }

        return builder.toString();
    }

    public boolean equals(DisaggregationSpecification other) {
        return other.constant == constant && other.diffuseRegressors == diffuseRegressors
                && other.log == log && other.trend == trend && other.zeroinit == zeroinit
                && other.type == type && other.defaultFrequency == defaultFrequency && 
                other.eps == eps && other.ml == ml && truncated==other.truncated 
                && other.model == model && other.option == option && other.p.equals(p)
                && other.span.equals(span);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DisaggregationSpecification && equals((DisaggregationSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.model);
        hash = 11 * hash + (this.constant ? 1 : 0);
        hash = 11 * hash + (this.trend ? 1 : 0);
        hash = 11 * hash + (this.log ? 1 : 0);
        hash = 11 * hash + (this.ml ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.p);
        hash = 11 * hash + Objects.hashCode(this.type);
        hash = 11 * hash + Objects.hashCode(this.defaultFrequency);
        return hash;
    }

    public static final String SPAN = "span", MODEL = "model", PARAMETER = "parameter", AGGTYPE = "aggregation",
            CONSTANT = "constant", TREND = "trend", ZEROINIT = "zeroinit", DIFFUSEREGS = "diffuseregs",
            EPS = "precision", LOG = "log", SSF = "ssfoption", FREQ = "defaultfrequency", ML = "ml", TRUNCATED="truncatedrho";
}
