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
package demetra.regarima;

import demetra.timeseries.TimeSelector;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public class BasicSpec{

    private TimeSelector span = TimeSelector.all();
    private boolean preprocess = true;
    private boolean preliminaryCheck = true;

    public BasicSpec() {
    }

    public BasicSpec(BasicSpec other) {
        this.span=other.span;
        this.preliminaryCheck=other.preliminaryCheck;
        this.preprocess=other.preprocess;
    }

    public void reset() {
        span = TimeSelector.all();
        preprocess = true;
        preliminaryCheck = true;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector value) {
            span = value;
    }

    public boolean isPreprocessing() {
        return preprocess;
    }

    public void setPreprocessing(boolean value) {
        preprocess = value;
    }

    public boolean isPreliminaryCheck() {
        return preliminaryCheck;
    }

    public void setPreliminaryCheck(boolean value) {
        preliminaryCheck = value;
    }

    public boolean isDefault() {
        if (span.getType() != TimeSelector.SelectionType.All) {
            return false;
        }
        return preprocess && preliminaryCheck;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.span);
        hash = 83 * hash + (this.preprocess ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof BasicSpec && equals((BasicSpec) obj));
    }

    private boolean equals(BasicSpec spec) {
        return Objects.equals(spec.span, span) && preprocess == spec.preprocess && preliminaryCheck == spec.preliminaryCheck;
    }

}
