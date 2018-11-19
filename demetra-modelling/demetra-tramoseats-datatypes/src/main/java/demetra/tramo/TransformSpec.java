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
package demetra.tramo;

import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.timeseries.TimeSelector;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TransformSpec {


    private TimeSelector span = TimeSelector.all();
    private double fct = DEF_FCT;
    private boolean preliminaryCheck = true;
    private TransformationType fn = TransformationType.None;
    public static final double DEF_FCT = 0.95;

    public TransformSpec() {
    }

    public TransformSpec(TransformSpec other) {
        span=other.span;
        fct=other.fct;
        preliminaryCheck=other.preliminaryCheck;
        fn=other.fn;
    }

    public void reset() {
        span=TimeSelector.all();
        fct = DEF_FCT;
        preliminaryCheck = true;
        fn = TransformationType.None;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector value) {
            span = value;
    }

    public TransformationType getFunction() {
        return fn;
    }

    public void setFunction(@Nonnull TransformationType fn) {
        this.fn = fn;
    }

    public double getFct() {
        return fct;
    }

    public void setFct(double value) {
        fct = value;
    }

    public boolean isPreliminaryCheck() {
        return preliminaryCheck;
    }
    
    public void setPreliminaryCheck(boolean value) {
        preliminaryCheck = value;
    }

    public boolean isDefault() {
        return fn == TransformationType.None
                && fct == DEF_FCT && span.getType() == TimeSelector.SelectionType.All
                && preliminaryCheck;
    }

    @Override
    public TransformSpec clone() {
        try {
            TransformSpec spec = (TransformSpec) super.clone();
            spec.span = span.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(TransformSpec other) {
        if (other == null) {
            return isDefault();
        }
        return fct == other.fct && fn == other.fn && Objects.equals(span, other.span) && preliminaryCheck == other.preliminaryCheck;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TransformSpec && equals((TransformSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.span);
        hash = 61 * hash + Double.hashCode(this.fct);
        hash = 61 * hash + fn.hashCode();
        return hash;
    }

}
