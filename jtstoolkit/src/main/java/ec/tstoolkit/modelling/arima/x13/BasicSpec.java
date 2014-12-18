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
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class BasicSpec implements Cloneable, InformationSetSerializable {

    private TsPeriodSelector span_ = new TsPeriodSelector();
    private boolean preprocess_ = true;

    public BasicSpec() {
    }
    
    public void reset(){
        span_ = new TsPeriodSelector();
        preprocess_ = true;
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

    public boolean isPreprocessing() {
        return preprocess_;
    }

    public void setPreprocessing(boolean value) {
        preprocess_ = value;
    }

    public boolean isDefault() {
        if (span_.getType() != PeriodSelectorType.All) {
            return false;
        }
        return preprocess_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.span_);
        hash = 83 * hash + (this.preprocess_ ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof BasicSpec && equals((BasicSpec) obj));
    }
    
    private boolean equals(BasicSpec spec) {
        return Objects.equals(spec.span_, span_) && preprocess_ == spec.preprocess_;
    }

    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || span_.getType() != PeriodSelectorType.All) {
            info.add(SPAN, span_);
        }
        if (verbose || !preprocess_) {
            info.add(PREPROCESS, preprocess_);
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
            Boolean preprocess = info.get(PREPROCESS, Boolean.class);
            if (preprocess != null) {
                preprocess_ = preprocess;
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        for (int i = 0; i < DICTIONARY.length; ++i) {
//            dic.add(InformationSet.item(prefix, DICTIONARY[i]));
//        }
//    }
    /////////////////////////////////////////////////////////////////////////
    public static final String SPAN = "span", PREPROCESS = "preprocess";
    private static final String[] DICTIONARY = new String[]{
        SPAN, PREPROCESS
    };
}
