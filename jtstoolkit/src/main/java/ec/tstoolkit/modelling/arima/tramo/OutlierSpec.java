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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutlierSpec implements Cloneable, InformationSetSerializable {
    public static final String SPAN = "span",
            TYPES = "types",
            VA = "va",
            EML = "eml",
            DELTATC = "deltatc";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
        dic.put(InformationSet.item(prefix, EML), Boolean.class);
        dic.put(InformationSet.item(prefix, TYPES), String[].class);
        dic.put(InformationSet.item(prefix, VA), Double.class);
        dic.put(InformationSet.item(prefix, DELTATC), Double.class);
    }

    private ArrayList<OutlierType> types_;
    private double tc_ = DEF_DELTATC;
    private boolean eml_ = false;
    private double cv_ = 0;
    private TsPeriodSelector span_;
    public static final double DEF_DELTATC = .7;
    public static final boolean DEF_EML = false;

    public OutlierSpec() {
        span_ = new TsPeriodSelector();
        types_ = new ArrayList<>(4);
    }

    public void reset() {
        types_.clear();
        tc_ = DEF_DELTATC;
        eml_ = false;
        cv_ = 0;
        span_ = new TsPeriodSelector();
    }

    @Override
    public OutlierSpec clone() {
        try {
            OutlierSpec spec = (OutlierSpec) super.clone();
            spec.types_ = new ArrayList<>(types_);
            spec.span_ = span_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public OutlierType[] getTypes() {
        if (types_.isEmpty()) {
            return null;
        } else {
            return Jdk6.Collections.toArray(types_, OutlierType.class);
        }
    }

    public void setTypes(OutlierType[] types) {
        this.types_.clear();
        if (types != null) {
            for (int i = 0; i < types.length; ++i) {
                add(types[i]);
            }
        }
    }

    public void clearTypes() {
        types_.clear();
    }

    public boolean contains(OutlierType type) {
        return types_.contains(type);
    }

    public void add(OutlierType type) {
        if (!types_.contains(type)) {
            types_.add(type);
        }
    }

    public void addRange(Collection<OutlierType> types) {
        for (OutlierType t : types) {
            add(t);
        }
    }

    public double getDeltaTC() {
        return tc_;
    }

    public void setDeltaTC(double value) {
        if (value != 0 && value < .3 || value >= 1) {
            throw new TramoException("TC should belong to [0.3, 1.0[");
        }
        tc_ = value;
    }

    public double getCriticalValue() {
        return cv_;
    }

    public void setCriticalValue(double value) {
        if (value != 0 && value < 2) {
            throw new TramoException("Critical value should be not be smaller than 2.0");
        }
        cv_ = value;
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

    public boolean isEML() {
        return eml_;
    }

    public void setEML(boolean value) {
        eml_ = value;
    }

    public int getAIO() {
        if (types_ == null) {
            return 0;
        }
        boolean ao = types_.contains(OutlierType.AO);
        boolean ls = types_.contains(OutlierType.LS);
        boolean tc = types_.contains(OutlierType.TC);
        if (ao && ls && !tc) {
            return 3;
        } else if (ao && tc && !ls) {
            return 1;
        } else {
            return 2;
        }
    }

    public void setAIO(int value) {
        clearTypes();
        switch (value) {
            case 1:
                add(OutlierType.AO);
                add(OutlierType.TC);
                break;
            case 2:
                add(OutlierType.AO);
                add(OutlierType.TC);
                add(OutlierType.LS);
                break;
            case 3:
                add(OutlierType.AO);
                add(OutlierType.LS);
                break;
            default:
                break;
        }
    }

    public boolean isDefault() {
        return (!eml_) && cv_ == 0 && types_.isEmpty() && (span_ == null || span_.getType() == PeriodSelectorType.All)
                && tc_ == DEF_DELTATC;
    }

    public boolean isUsed() {
        return !types_.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierSpec && equals((OutlierSpec) obj));
    }

    private boolean equals(OutlierSpec other) {
        return cv_ == other.cv_ && eml_ == other.eml_
                && Objects.equals(span_, other.span_) && tc_ == other.tc_
                && Comparator.equals(types_, other.types_); // the order could be different. Use Comparator
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Jdk6.Double.hashCode(this.tc_);
        hash = 89 * hash + (this.eml_ ? 1 : 0);
        hash = 89 * hash + Jdk6.Double.hashCode(this.cv_);
        hash = 89 * hash + Objects.hashCode(this.span_);
        return hash;
    }

    public void remove(OutlierType outlierType) {
        types_.remove(outlierType);
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        if (!types_.isEmpty()) {
            String[] types = new String[types_.size()];
            for (int i = 0; i < types.length; ++i) {
                types[i] = types_.get(i).name();
            }
            info.add(TYPES, types);
        }
        if (verbose || cv_ != 0) {
            info.add(VA, cv_);
        }
        if (verbose || eml_ != DEF_EML) {
            info.add(EML, eml_);
        }
        if (verbose || tc_ != DEF_DELTATC) {
            info.add(DELTATC, tc_);
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
            String[] types = info.get(TYPES, String[].class);
            if (types != null) {
                for (int i = 0; i < types.length; ++i) {
                    types_.add(OutlierType.valueOf(types[i]));
                }
            }
            Double cv = info.get(VA, Double.class);
            if (cv != null) {
                cv_ = cv;
            }
            Double tc = info.get(DELTATC, Double.class);
            if (tc != null) {
                tc_ = tc;
            }
            Boolean eml = info.get(EML, Boolean.class);
            if (eml != null) {
                eml_ = eml;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }
}
