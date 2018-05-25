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
package demetra.x12;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class AutoModelSpec{

    private boolean enabled = false;
    private OrderSpec diff, order;
    private boolean acceptdef = DEF_ACCEPTDEF, checkmu = DEF_CHECKMU, mixed = DEF_MIXED, balanced = DEF_BALANCED, hr = DEF_HR;
    private double cancel = DEF_CANCEL, fct = DEF_FCT, pcr = DEF_LJUNGBOX, predcv = DEF_PREDCV,
            tsig = DEF_TSIG, ub1 = DEF_UB1, ub2 = DEF_UB2, ubfinal = DEF_UBFINAL;
    public static final double DEF_LJUNGBOX = .95, DEF_TSIG = 1, DEF_PREDCV = .14286, DEF_UBFINAL = 1.05, DEF_UB1 = 1 / .96, DEF_UB2 = .88,
            DEF_CANCEL = 0.1, DEF_FCT = 1 / .9875;
    public static final boolean DEF_ACCEPTDEF = false, DEF_CHECKMU = true, DEF_MIXED = true,
            DEF_BALANCED = false, DEF_HR = false;

    public AutoModelSpec(boolean enabled) {
        this.enabled = enabled;
    }
    
    public AutoModelSpec(AutoModelSpec other){
        this.enabled=other.enabled;
        this.acceptdef=other.acceptdef;
        this.balanced=other.balanced;
        this.cancel=other.cancel;
        this.checkmu=other.checkmu;
        this.diff=other.diff;
        this.fct=other.fct;
        this.hr=other.hr;
        this.mixed=other.mixed;
        this.order=other.order;
        this.pcr=other.pcr;
        this.predcv=other.predcv;
        this.tsig=other.tsig;
        this.ub1=other.ub1;
        this.ub2=other.ub2;
        this.ubfinal=other.ubfinal;
    }

    public void reset() {
        diff = null;
        order = null;
        enabled = false;
        acceptdef = DEF_ACCEPTDEF;
        checkmu = DEF_CHECKMU;
        mixed = DEF_MIXED;
        balanced = DEF_BALANCED;
        hr = DEF_HR;
        cancel = DEF_CANCEL;
        fct = DEF_FCT;
        pcr = DEF_LJUNGBOX;
        predcv = DEF_PREDCV;
        tsig = DEF_TSIG;
        ub1 = DEF_UB1;
        ub2 = DEF_UB2;
        ubfinal = DEF_UBFINAL;

    }

    public boolean isAcceptDefault() {
        return acceptdef;
    }

    public void setAcceptDefault(boolean value) {
        acceptdef = value;
    }

    public boolean isCheckMu() {
        return checkmu;
    }

    public void setCheckMu(boolean value) {
        checkmu = value;
    }

    public OrderSpec getArma() {
        return order;
    }

    public void setArma(OrderSpec value) {
        order = value;
    }

    public OrderSpec getDiff() {
        return diff;
    }

    public void setDiff(OrderSpec value) {
        diff = value;
    }

    public boolean isMixed() {
        return mixed;
    }

    public void setMixed(boolean value) {
        mixed = value;
    }

    /// <summary>
    /// pcr
    /// </summary>
    public double getLjungBoxLimit() {
        return pcr;
    }

    public void setLjungBoxLimit(double value) {
        pcr = value;
    }

    /// <summary>
    /// Limit of Arma T-value
    /// </summary>
    public double getArmaSignificance() {
        return tsig;
    }

    public void setArmaSignificance(double value) {
        if (value < 0.5) {
            throw new X12Exception("Arma T-value limit must be greater than .5");
        }
        tsig = value;
    }

    /// <summary>
    /// Precent RSE
    /// fct
    /// </summary>
    public double getPercentRSE() {
        return fct;
    }

    public void setPercentRSE(double value) {
        if (value < 1) {
            throw new X12Exception("Must be greater than .5");
        }
        fct = value;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public void setBalanced(boolean value) {
        balanced = value;
    }

    public boolean isHannanRissannen() {
        return hr;
    }

    public void setHannanRissanen(boolean value) {
        hr = value;
    }

    /// <summary>
    /// Precent reduction in critical value
    /// predcv
    /// </summary>
    public double getPercentReductionCV() {
        return predcv;
    }

    public void setPercentReductionCV(double value) {
        if (value < 0.05 || value > .3) {
            throw new X12Exception("Percent reduction of critical value must be in [0.05, .3]");
        }
        predcv = value;
    }

    /// <summary>
    /// ub1. Default = DEF_UB1
    /// </summary>
    public double getInitialUnitRootLimit() {
        return ub1;
    }

    public void setInitialUnitRootLimit(double value) {
        if (value <= 1) {
            throw new X12Exception("Initial unit root limit must be greater than 1");
        }
        ub1 = value;
    }

    /// <summary>
    /// ub1. Default = DEF_UB1
    /// </summary>
    public double getFinalUnitRootLimit() {
        return ub2;
    }

    public void setFinalUnitRootLimit(double value) {
        if (value >= 1) {
            throw new X12Exception("Final unit root limit must be less than 1");
        }
        ub2 = value;
    }

    /// <summary>
    /// cancel. Default = DEF_CANCEL
    /// </summary>
    public double getCancelationLimit() {
        return cancel;
    }

    public void setCancelationLimit(double value) {
        if (value < 0 || value > .2) {
            throw new X12Exception("Cancelation limit must be in [0, .2]");
        }
        cancel = value;
    }

    /// <summary>
    /// Unit root limit in the final model
    /// ubfin. Default = DEF_UBFIN
    /// </summary>
    public double getUnitRootLimit() {
        return ubfinal;
    }

    public void setUnitRootLimit(double value) {
        if (value < 1) {
            throw new X12Exception("Unit root limit must be greater than 1");
        }
        ubfinal = value;
    }

    public boolean isDefault() {
        return !enabled && !acceptdef && diff == null && order == null && tsig == DEF_TSIG && pcr == DEF_LJUNGBOX
                && predcv == DEF_PREDCV && ubfinal == DEF_UBFINAL && checkmu && mixed && !balanced
                && cancel == DEF_CANCEL && fct == DEF_FCT && ub1 == DEF_UB1 && ub2 == DEF_UB2;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AutoModelSpec && equals((AutoModelSpec) obj));
    }

    private boolean equals(AutoModelSpec other) {
        return enabled == other.enabled && acceptdef == other.acceptdef
                && Objects.equals(diff, other.diff) && Objects.equals(order, other.order)
                && balanced == other.balanced && checkmu == other.checkmu
                && mixed == other.mixed && hr == other.hr
                && tsig == other.tsig && pcr == other.pcr && predcv == other.predcv
                && ub1 == other.ub1 && ub2 == other.ub2 && cancel == other.cancel
                && ubfinal == other.ubfinal && fct == other.fct;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Double.hashCode(this.pcr);
        hash = 79 * hash + Double.hashCode(this.predcv);
        hash = 79 * hash + Double.hashCode(this.tsig);
        hash = 79 * hash + Double.hashCode(this.ubfinal);
        return hash;
    }

}
