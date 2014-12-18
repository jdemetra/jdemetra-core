/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BasicSpec implements Cloneable, InformationSetSerializable {

    public static final String SPAN = "span",
            LOG = "log",
            DATATYPE="datatype";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, LOG), Boolean.class);
        dic.put(InformationSet.item(prefix, DATATYPE), String.class);
         dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
   }

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private DataType type_ = DataType.Flow;
    private boolean log_ = false;
 
    public BasicSpec() {
    }

    public void reset() {
        span_ = new TsPeriodSelector();
        log_ = false;
        type_=DataType.Flow;
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

    public boolean isLog() {
        return log_;
    }

    public void setLog(boolean log) {
        log_ = log;
    }
    
    public DataType getDataType(){
        return type_;
    }

    public void setDataType(DataType type){
        type_=type;
    }
    
    public boolean isDefault() {
        return log_ == false && type_ == DataType.Flow && span_.getType() == PeriodSelectorType.All;
    }

    @Override
    public BasicSpec clone() {
        try {
            BasicSpec spec = (BasicSpec) super.clone();
            spec.span_ = span_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(BasicSpec other) {
        if (other == null) {
            return isDefault();
        }
        return log_ == other.log_ && type_ == other.type_ && Objects.equals(span_, other.span_);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof BasicSpec && equals((BasicSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.span_);
        hash = 61 * hash + (this.log_ ? 1 : 0);
        hash = 61 * hash + Objects.hashCode(this.type_);
        return hash;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        if (verbose || log_ ) {
            info.add(LOG, log_);
        }
        if (verbose || type_ != DataType.Flow) {
            info.add(DATATYPE, type_.name());
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
            String t = info.get(DATATYPE, String.class);
            if (t != null) {
                type_ = DataType.valueOf(t);
            }
            Boolean log = info.get(LOG, Boolean.class);
            if (log != null) {
                log_ = log;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
