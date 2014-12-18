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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class AutoModelSpec implements Cloneable, InformationSetSerializable {

    public static final String ENABLED = "enabled",
            ACCEPTDEFAULT = "acceptdefault",
            MIXED = "mixed",
            BALANCED = "balanced",
            CHECKMU = "checkmu",
            HR = "hrinitial",
            LJUNGBOXLIMIT = "ljungboxlimit",
            REDUCECV = "reducecv",
            UB1 = "ub1",
            UB2 = "ub2",
            CANCEL = "cancel",
            ARMALIMIT = "armalimit",
            UBFINAL = "ubfinal";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, CANCEL), Double.class);
        dic.put(InformationSet.item(prefix, UB1), Double.class);
        dic.put(InformationSet.item(prefix, UB2), Double.class);
        dic.put(InformationSet.item(prefix, ARMALIMIT), Double.class);
        dic.put(InformationSet.item(prefix, UBFINAL), Double.class);
        dic.put(InformationSet.item(prefix, LJUNGBOXLIMIT), Double.class);
        dic.put(InformationSet.item(prefix, REDUCECV), Double.class);
        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
        dic.put(InformationSet.item(prefix, ACCEPTDEFAULT), Boolean.class);
        dic.put(InformationSet.item(prefix, MIXED), Boolean.class);
        dic.put(InformationSet.item(prefix, CHECKMU), Boolean.class);
        dic.put(InformationSet.item(prefix, BALANCED), Boolean.class);
        dic.put(InformationSet.item(prefix, HR), Boolean.class);
    }

    private boolean enabled_ = false;
    private OrderSpec diff_, order_;
    private boolean acceptdef_, checkmu_ = true, mixed_ = true, balanced_, hr_;
    private double cancel_ = DEF_CANCEL, fct_ = DEF_FCT, pcr_ = DEF_LJUNGBOX, predcv_ = DEF_PREDCV,
            tsig_ = DEF_TSIG, ub1_ = DEF_UB1, ub2_ = DEF_UB2, ubfinal_ = DEF_UBFINAL;
    public static final double DEF_LJUNGBOX = .95, DEF_TSIG = 1, DEF_PREDCV = .14286, DEF_UBFINAL = 1.05, DEF_UB1 = 1 / .96, DEF_UB2 = .88,
            DEF_CANCEL = 0.1, DEF_FCT = 1 / .9875;

    public AutoModelSpec() {
    }

    public AutoModelSpec(boolean enabled) {
        enabled_ = enabled;
    }

    public void reset() {
        diff_ = null;
        order_ = null;
        enabled_ = false;
        acceptdef_ = false;
        checkmu_ = true;
        mixed_ = true;
        balanced_ = false;
        hr_ = false;
        cancel_ = DEF_CANCEL;
        fct_ = DEF_FCT;
        pcr_ = DEF_LJUNGBOX;
        predcv_ = DEF_PREDCV;
        tsig_ = DEF_TSIG;
        ub1_ = DEF_UB1;
        ub2_ = DEF_UB2;
        ubfinal_ = DEF_UBFINAL;

    }

    @Override
    public AutoModelSpec clone() {
        try {
            AutoModelSpec spec = (AutoModelSpec) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean isAcceptDefault() {
        return acceptdef_;
    }

    public void setAcceptDefault(boolean value) {
        acceptdef_ = value;
    }

    public boolean isCheckMu() {
        return checkmu_;
    }

    public void setCheckMu(boolean value) {
        checkmu_ = value;
    }

    public OrderSpec getArma() {
        return order_;
    }

    public void setArma(OrderSpec value) {
        order_ = value;
    }

    public OrderSpec getDiff() {
        return diff_;
    }

    public void setDiff(OrderSpec value) {
        diff_ = value;
    }

    public boolean isMixed() {
        return mixed_;
    }

    public void setMixed(boolean value) {
        mixed_ = value;
    }

    /// <summary>
    /// pcr
    /// </summary>
    public double getLjungBoxLimit() {
        return pcr_;
    }

    public void setLjungBoxLimit(double value) {
        pcr_ = value;
    }

    /// <summary>
    /// Limit of Arma T-value
    /// </summary>
    public double getArmaSignificance() {
        return tsig_;
    }

    public void setArmaSignificance(double value) {
        if (value < 0.5) {
            throw new X13Exception("Arma T-value limit must be greater than .5");
        }
        tsig_ = value;
    }

    /// <summary>
    /// Precent RSE
    /// fct
    /// </summary>
    public double getPercentRSE() {
        return fct_;
    }

    public void setPercentRSE(double value) {
        if (value < 1) {
            throw new X13Exception("Must be greater than .5");
        }
        fct_ = value;
    }

    public boolean isBalanced() {
        return balanced_;
    }

    public void setBalanced(boolean value) {
        balanced_ = value;
    }

    public boolean isHannanRissannen() {
        return hr_;
    }

    public void setHannanRissanen(boolean value) {
        hr_ = value;
    }

    /// <summary>
    /// Precent reduction in critical value
    /// predcv
    /// </summary>
    public double getPercentReductionCV() {
        return predcv_;
    }

    public void setPercentReductionCV(double value) {
        if (value < 0.05 || value > .3) {
            throw new X13Exception("Percent reduction of critical value must be in [0.05, .3]");
        }
        predcv_ = value;
    }

    /// <summary>
    /// ub1. Default = DEF_UB1
    /// </summary>
    public double getInitialUnitRootLimit() {
        return ub1_;
    }

    public void setInitialUnitRootLimit(double value) {
        if (value <= 1) {
            throw new X13Exception("Initial unit root limit must be greater than 1");
        }
        ub1_ = value;
    }

    /// <summary>
    /// ub1. Default = DEF_UB1
    /// </summary>
    public double getFinalUnitRootLimit() {
        return ub2_;
    }

    public void setFinalUnitRootLimit(double value) {
        if (value >= 1) {
            throw new X13Exception("Final unit root limit must be less than 1");
        }
        ub2_ = value;
    }

    /// <summary>
    /// cancel. Default = DEF_CANCEL
    /// </summary>
    public double getCancelationLimit() {
        return cancel_;
    }

    public void setCancelationLimit(double value) {
        if (value < 0 || value > .2) {
            throw new X13Exception("Cancelation limit must be in [0, .2]");
        }
        cancel_ = value;
    }

    /// <summary>
    /// Unit root limit in the final model
    /// ubfin. Default = DEF_UBFIN
    /// </summary>
    public double getUnitRootLimit() {
        return ubfinal_;
    }

    public void setUnitRootLimit(double value) {
        if (value < 1) {
            throw new X13Exception("Unit root limit must be greater than 1");
        }
        ubfinal_ = value;
    }

    public boolean isDefault() {
        return enabled_ = true && !acceptdef_ && diff_ == null && order_ == null && tsig_ == DEF_TSIG && pcr_ == DEF_LJUNGBOX
                && predcv_ == DEF_PREDCV && ubfinal_ == DEF_UBFINAL && checkmu_ && mixed_ && !balanced_
                && cancel_ == DEF_CANCEL && fct_ == DEF_FCT && ub1_ == DEF_UB1 && ub2_ == DEF_UB2;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setEnabled(boolean value) {
        enabled_ = value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AutoModelSpec && equals((AutoModelSpec) obj));
    }

    private boolean equals(AutoModelSpec other) {
        return acceptdef_ == other.acceptdef_ && tsig_ == other.tsig_ && balanced_ == other.balanced_
                && checkmu_ == other.checkmu_ && Objects.equals(diff_, other.diff_) && enabled_ == other.enabled_
                && hr_ == other.hr_ && pcr_ == other.pcr_ && mixed_ == other.mixed_ && Objects.equals(order_, other.order_)
                && predcv_ == other.predcv_ && ubfinal_ == other.ubfinal_;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Jdk6.Double.hashCode(this.pcr_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.predcv_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.tsig_);
        hash = 79 * hash + Jdk6.Double.hashCode(this.ubfinal_);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(ENABLED, enabled_);
        if (verbose || pcr_ != DEF_LJUNGBOX) {
            info.add(LJUNGBOXLIMIT, pcr_);
        }
        if (verbose || predcv_ != DEF_PREDCV) {
            info.add(REDUCECV, predcv_);
        }
        if (verbose || ub1_ != DEF_UB1) {
            info.add(UB1, ub1_);
        }
        if (verbose || ub2_ != DEF_UB2) {
            info.add(UB2, ub2_);
        }
        if (verbose || cancel_ != DEF_CANCEL) {
            info.add(CANCEL, cancel_);
        }
        if (verbose || acceptdef_) {
            info.add(ACCEPTDEFAULT, acceptdef_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            Boolean enabled = info.get(ENABLED, Boolean.class);
            if (enabled != null) {
                enabled_ = enabled;
            }
            Boolean fal = info.get(ACCEPTDEFAULT, Boolean.class);
            if (fal != null) {
                acceptdef_ = fal;
            }
            Boolean mixed = info.get(MIXED, Boolean.class);
            if (mixed != null) {
                mixed_ = mixed;
            }
            Boolean balanced = info.get(BALANCED, Boolean.class);
            if (balanced != null) {
                balanced_ = balanced;
            }
            Boolean hr = info.get(HR, Boolean.class);
            if (hr != null) {
                hr_ = hr;
            }
            Boolean mu = info.get(CHECKMU, Boolean.class);
            if (mu != null) {
                checkmu_ = mu;
            }
            Double pcr = info.get(LJUNGBOXLIMIT, Double.class);
            if (pcr != null) {
                pcr_ = pcr;
            }
            Double pc = info.get(REDUCECV, Double.class);
            if (pc != null) {
                predcv_ = pc;
            }
            Double ub1 = info.get(UB1, Double.class);
            if (ub1 != null) {
                ub1_ = ub1;
            }
            Double ub2 = info.get(UB2, Double.class);
            if (ub2 != null) {
                ub2_ = ub2;
            }
            Double cancel = info.get(CANCEL, Double.class);
            if (cancel != null) {
                cancel_ = cancel;
            }
            Double tsig = info.get(ARMALIMIT, Double.class);
            if (tsig != null) {
                tsig_ = tsig;
            }
            Double ubf = info.get(UBFINAL, Double.class);
            if (ubf != null) {
                ubfinal_ = ubf;
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
