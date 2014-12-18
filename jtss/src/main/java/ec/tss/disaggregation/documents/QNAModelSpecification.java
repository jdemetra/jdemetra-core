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
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.IModellingSpecification;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean
 */
public class QNAModelSpecification implements IProcSpecification, Cloneable {

    public static final double DEF_EPS = 1e-5;

    // Estimation span
    private TsPeriodSelector span = new TsPeriodSelector();

    // Disaggrgeation model
    private DisaggregationSpecification.Model model = DisaggregationSpecification.Model.Ar1; // Chow-Lin
    private boolean constant = true, trend;
    private Parameter p = new Parameter();
    private TsAggregationType type = TsAggregationType.Sum;

    // Disaggregation algorithm
    private boolean diffuseRegressors, zeroinit;
    private double eps = DEF_EPS;
    

    public void reset() {
        model = DisaggregationSpecification.Model.Ar1;
        constant = true;
        trend = false;
        p = new Parameter();
        type = TsAggregationType.Sum;
        diffuseRegressors = false;
        zeroinit = false;
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
    public DisaggregationSpecification.Model getModel() {
        return model;
    }

    /**
     * @param aModel the model to set
     */
    public void setModel(DisaggregationSpecification.Model aModel) {
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
    
    @Override
    public QNAModelSpecification clone() {
        try {
            QNAModelSpecification spec = (QNAModelSpecification) super.clone();
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
        if (zeroinit) {
            info.set(ZEROINIT, zeroinit);
        }
        if (diffuseRegressors) {
            info.set(DIFFUSEREGS, diffuseRegressors);
        }
        if (eps != DEF_EPS) {
            info.set(EPS, eps);
        }
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
        model = DisaggregationSpecification.Model.valueOf(n);
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
        b = info.get(ZEROINIT, Boolean.class);
        if (b != null) {
            zeroinit = b;
        }
          b = info.get(DIFFUSEREGS, Boolean.class);
        if (b != null) {
            diffuseRegressors = b;
        }
        Double e = info.get(EPS, Double.class);
        if (e != null) {
            eps = e;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        switch (model){
            case Ar1:
                builder.append("Chow-Lin");break;
            case Rw:
                builder.append("Fernandez");break;
            case RwAr1:
                builder.append("Litterman");break;
            default:
                builder.append("OLS");break;
        }
        return builder.toString();
    }

    public boolean equals(QNAModelSpecification other) {
        return other.constant == constant && other.diffuseRegressors == diffuseRegressors
                && other.trend == trend && other.zeroinit == zeroinit
                && other.eps == eps
                && other.model == model && other.p.equals(p)
                && other.span.equals(span);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof QNAModelSpecification && equals((QNAModelSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (this.model != null ? this.model.hashCode() : 0);
        hash = 31 * hash + (this.constant ? 1 : 0);
        hash = 31 * hash + (this.trend ? 1 : 0);
        hash = 31 * hash + (this.diffuseRegressors ? 1 : 0);
        hash = 31 * hash + (this.zeroinit ? 1 : 0);
        return hash;
    }
    public static final String SPAN = "span", MODEL = "model", PARAMETER = "parameter", AGGTYPE = "aggregation",
            CONSTANT = "constant", TREND = "trend", ZEROINIT = "zeroinit", DIFFUSEREGS = "diffuseregs",
            EPS = "precision";
}
