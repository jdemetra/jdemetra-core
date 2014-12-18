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
package ec.satoolkit.benchmarking;

import ec.benchmarking.simplets.TsCholette;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;

/**
 * This class specifies the way the uni-variate benchmarking routine (Cholette)
 * will be used. See for instance the X13 documentation for further details on
 * the method and on the parameters The main parameters are: - enabled (false by
 * default!) - target: the target series; original(default) or calendar adjusted
 * - rho: the smoothing parameter (should be in [0,1]; 0 = no time structure, 1
 * = "Denton"-like - lambda: modifies the penalty function; 0 = additive, 0.5 =
 * proportional, 1 = multiplicative
 *
 * @author Jean Palate
 */
public class SaBenchmarkingSpec implements IProcSpecification, Cloneable {

    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    public static final String ENABLED = "enabled",
            TARGET = "target",
            FORECAST = "forecast",
            LAMBDA = "lambda",
            RHO = "rho",
            BIAS = "bias";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, BIAS), String.class);
        dic.put(InformationSet.item(prefix, TARGET), String.class);
        dic.put(InformationSet.item(prefix, FORECAST), Boolean.class);
        dic.put(InformationSet.item(prefix, RHO), Double.class);
        dic.put(InformationSet.item(prefix, LAMBDA), Double.class);
    }

    private boolean enabled_ = false, forecast_ = false;
    private Target target_ = Target.Original;
    private double rho_ = DEF_RHO;
    private double lambda_ = DEF_LAMBDA;
    private TsCholette.BiasCorrection bias_ = TsCholette.BiasCorrection.None;

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean enabled) {
        enabled_ = enabled;
    }

    /**
     * @return the target_
     */
    public Target getTarget() {
        return target_;
    }

    /**
     * Sets the target for the benchmarking
     *
     * @param target the target to set
     */
    public void setTarget(Target target) {
        this.target_ = target;
    }

    /**
     *
     * @return the rho_
     */
    public double getRho() {
        return rho_;
    }

    /**
     * @param rho the rho to set
     */
    public void setRho(double rho) {
        this.rho_ = rho;
    }

    /**
     * @return the lambda_
     */
    public double getLambda() {
        return lambda_;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda_ = lambda;
    }

    /**
     * @return the bias_
     */
    public TsCholette.BiasCorrection getBias() {
        return bias_;
    }

    /**
     * @param bias the bias to set
     */
    public void setBias(TsCholette.BiasCorrection bias) {
        this.bias_ = bias;
    }

    public boolean isUsingForecast() {
        return forecast_;
    }

    public void useForecast(boolean bf) {
        forecast_ = bf;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || enabled_) {
            info.add(ENABLED, enabled_);
        }
        if (verbose || forecast_ )
            info.add(FORECAST, forecast_);
        if (verbose || target_ != Target.Original) {
            info.add(TARGET, target_.name());
        }
        if (verbose || lambda_ != DEF_LAMBDA) {
            info.add(LAMBDA, lambda_);
        }
        if (verbose || rho_ != DEF_RHO) {
            info.add(RHO, rho_);
        }
        if (verbose || bias_ != TsCholette.BiasCorrection.None) {
            info.add(BIAS, bias_.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            Boolean enabled = info.get(ENABLED, Boolean.class);
            if (enabled != null) {
                enabled_ = enabled;
            }
            Boolean f = info.get(FORECAST, Boolean.class);
            if (f != null) {
                forecast_ = f;
            }
            Double rho = info.get(RHO, Double.class);
            if (rho != null) {
                rho_ = rho;
            }
            Double lambda = info.get(LAMBDA, Double.class);
            if (lambda != null) {
                lambda_ = lambda;
            }
            String target = info.get(TARGET, String.class);
            if (target != null) {
                target_ = Target.valueOf(target);
            }
            String bias = info.get(BIAS, String.class);
            if (bias != null) {
                bias_ = TsCholette.BiasCorrection.valueOf(bias);
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

    public static enum Target {

        Original,
        CalendarAdjusted
    }

    @Override
    public SaBenchmarkingSpec clone() {
        try {
            return (SaBenchmarkingSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(SaBenchmarkingSpec other) {
        return enabled_ == other.enabled_ && target_ == other.target_ && rho_ == other.rho_
                && lambda_ == other.lambda_ && bias_ == other.bias_ && forecast_ == other.forecast_;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SaBenchmarkingSpec && equals((SaBenchmarkingSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.enabled_ ? 1 : 0);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.rho_) ^ (Double.doubleToLongBits(this.rho_) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.lambda_) ^ (Double.doubleToLongBits(this.lambda_) >>> 32));
        return hash;
    }

}
