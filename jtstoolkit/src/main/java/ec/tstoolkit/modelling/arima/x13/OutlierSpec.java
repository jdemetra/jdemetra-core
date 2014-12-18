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
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class OutlierSpec implements Cloneable, InformationSetSerializable {

    public static final String SPAN = "span",
            AO = "ao", LS = "ls", TC = "tc", SO = "so",
            DEFCV = "defcv",
            METHOD = "method",
            LSRUN = "lsrun",
            TCRATE = "tcrate",
            MAXITER = "maxiter";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
        dic.put(InformationSet.item(prefix, AO), Boolean.class);
        dic.put(InformationSet.item(prefix, LS), Boolean.class);
        dic.put(InformationSet.item(prefix, TC), Boolean.class);
        dic.put(InformationSet.item(prefix, SO), Boolean.class);
        dic.put(InformationSet.item(prefix, DEFCV), Double.class);
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, LSRUN), Integer.class);
        dic.put(InformationSet.item(prefix, TCRATE), Double.class);
        dic.put(InformationSet.item(prefix, MAXITER), Integer.class);
    }

    public static enum Method {

        AddOne,
        AddAll
    }
    private ArrayList<SingleOutlierSpec> types_ = new ArrayList<>();
    private int lsrun_ = 0;
    private Method method_ = Method.AddOne;
    private double tc_ = DEF_TCRATE, defcv_ = 0;
    private TsPeriodSelector span_ = new TsPeriodSelector();
    private int nmax_ = DEF_NMAX;
    public static final double DEF_TCRATE = .7, DEF_VA = 4.0;
    public static final int DEF_NMAX = 30;

    public OutlierSpec() {
    }

    public void reset() {
        types_.clear();
        lsrun_ = 0;
        method_ = Method.AddOne;
        tc_ = DEF_TCRATE;
        defcv_ = 0;
        span_ = new TsPeriodSelector();
        nmax_ = DEF_NMAX;
    }

    public boolean isUsed() {
        return !types_.isEmpty();
    }

    public int getTypesCount() {
        return types_.size();
    }

    public SingleOutlierSpec[] getTypes() {
        return Jdk6.Collections.toArray(types_, SingleOutlierSpec.class);
    }

    public void setTypes(SingleOutlierSpec[] value) {
        types_.clear();
        if (value != null) {
            for (SingleOutlierSpec sspec : value) {
                add(sspec);
            }
        }
    }

    public void clearTypes() {
        types_.clear();
    }

    public void add(OutlierType type) {
        SingleOutlierSpec spec = new SingleOutlierSpec();
        spec.setCriticalValue(defcv_);
        spec.setType(type);
        add(spec);
    }

    public void add(SingleOutlierSpec spec) {
        for (SingleOutlierSpec s : types_) {
            if (s.getType() == spec.getType()) {
                s.setCriticalValue(spec.getCriticalValue());
                return;
            }
        }
        types_.add(spec);
    }

    public SingleOutlierSpec search(OutlierType type) {
        for (SingleOutlierSpec s : types_) {
            if (s.getType() == type) {
                return s;
            }
        }
        return null;
    }

    public void remove(OutlierType type) {
        for (SingleOutlierSpec s : types_) {
            if (s.getType() == type) {
                types_.remove(s);
                return;
            }
        }

    }

    public int getLSRun() {
        return lsrun_;
    }

    public void setLSRun(int value) {
        lsrun_ = value;
    }

    public int getMaxIter() {
        return nmax_;
    }

    public void seMaxIter(int value) {
        nmax_ = value;
    }

    public Method getMethod() {
        return method_;
    }

    public void setMethod(Method value) {
        method_ = value;
    }

    public TsPeriodSelector getSpan() {
        return span_;
    }

    public void setSpan(TsPeriodSelector value) {
        if (value == null) {
            span_.all();
        } else {
            span_ = value;
        }
    }

    public double getMonthlyTCRate() {
        return tc_;
    }

    public void setMonthlyTCRate(double value) {
        tc_ = value;
    }

    /// <summary>
    /// When the default critical value is changed, all the current outliers' critical values are accordingly modified
    /// </summary>
    public double getDefaultCriticalValue() {
        return defcv_;
    }

    public void setDefaultCriticalValue(double value) {
        defcv_ = value;
        for (SingleOutlierSpec s : types_) {
            s.setCriticalValue(value);
        }
    }

    @Override
    public OutlierSpec clone() {
        try {
            OutlierSpec spec = (OutlierSpec) super.clone();
            spec.types_ = new ArrayList<>();
            for (SingleOutlierSpec s : types_) {
                spec.types_.add(s.clone());
            }
            spec.span_ = span_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierSpec && equals((OutlierSpec) obj));
    }

    private boolean equals(OutlierSpec other) {
        return defcv_ == other.defcv_ && lsrun_ == other.lsrun_ && method_ == other.method_ && Objects.equals(span_, other.span_)
                && nmax_ == other.nmax_ && tc_ == other.tc_ && Comparator.equals(types_, other.types_);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.method_);
        hash = 71 * hash + Jdk6.Double.hashCode(this.tc_);
        hash = 71 * hash + Jdk6.Double.hashCode(this.defcv_);
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && !isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        for (SingleOutlierSpec s : types_) {
            info.add(s.getType().name().toLowerCase(), s.getCriticalValue());
        }
        if (verbose || defcv_ != 0) {
            info.add(DEFCV, defcv_);
        }
        if (verbose || method_ != Method.AddOne) {
            info.add(METHOD, method_.name());
        }
        if (verbose || lsrun_ != 0) {
            info.add(LSRUN, lsrun_);
        }
        if (verbose || tc_ != DEF_TCRATE) {
            info.add(TCRATE, tc_);
        }
        if (verbose || nmax_ != DEF_NMAX) {
            info.add(MAXITER, nmax_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            reset();
            TsPeriodSelector span = info.get(SPAN, TsPeriodSelector.class);
            if (span != null) {
                span_ = span;
            }
            Double ao = info.get(AO, Double.class);
            if (ao != null) {
                types_.add(new SingleOutlierSpec(OutlierType.AO, ao));
            }
            Double ls = info.get(LS, Double.class);
            if (ls != null) {
                types_.add(new SingleOutlierSpec(OutlierType.LS, ls));
            }
            Double tc = info.get(TC, Double.class);
            if (tc != null) {
                types_.add(new SingleOutlierSpec(OutlierType.TC, tc));
            }
            Double so = info.get(SO, Double.class);
            if (so != null) {
                types_.add(new SingleOutlierSpec(OutlierType.SO, so));
            }

            Double defcv = info.get(DEFCV, Double.class);
            if (defcv != null) {
                defcv_ = defcv;
            }
            Double tcr = info.get(TCRATE, Double.class);
            if (tcr != null) {
                tc_ = tcr;
            }
            String method = info.get(METHOD, String.class);
            if (method != null) {
                method_ = Method.valueOf(method);
            }
            Integer lsrun = info.get(LSRUN, Integer.class);
            if (lsrun != null) {
                lsrun_ = lsrun;
            }
            Integer nmax = info.get(MAXITER, Integer.class);
            if (nmax != null) {
                nmax_ = nmax;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
