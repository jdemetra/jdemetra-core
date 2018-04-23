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
import demetra.timeseries.TimeSelector;
import demetra.tramo.TramoException;
import demetra.utilities.Comparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutlierSpec {

    private final ArrayList<String> types=new ArrayList<>();
    private double tc = DEF_DELTATC;
    private boolean ml = false;
    private double cv = 0;
    private TimeSelector span=TimeSelector.all();
    public static final double DEF_DELTATC = .7;
    public static final boolean DEF_EML = false;

    public OutlierSpec() {
    }

    public OutlierSpec(OutlierSpec other) {
       this.types.addAll(other.types);
       this.tc=other.tc;
       this.cv=other.cv;
       this.ml=other.ml;
       this.span=other.span;
    }

    public void reset() {
        types.clear();
        tc = DEF_DELTATC;
        ml = false;
        cv = 0;
        span=TimeSelector.all();
    }


    public String[] getTypes() {
            return types.toArray(new String[types.size()]);
    }

    public void setTypes(@Nonnull String[] types) {
        this.types.clear();
            for (int i = 0; i < types.length; ++i) {
                add(types[i]);
            }
    }

    public void clearTypes() {
        types.clear();
    }

    public boolean contains(String type) {
        return types.contains(type);
    }

    public void add(String type) {
        if (!types.contains(type)) {
            types.add(type);
        }
    }

    public void add(String... types) {
        for (String t : types) {
            add(t);
        }
    }

    public double getDeltaTC() {
        return tc;
    }

    public void setDeltaTC(double value) {
        if (value != 0 && value < .3 || value >= 1) {
            throw new TramoException("TC should belong to [0.3, 1.0[");
        }
        tc = value;
    }

    public double getCriticalValue() {
        return cv;
    }

    public void setCriticalValue(double value) {
        if (value != 0 && value < 2) {
            throw new TramoException("Critical value should be greater than 2.0");
        }
        cv = value;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector value) {
            span = value;
    }

    public boolean isMaximumLikelihood() {
        return ml;
    }

    public void setMaximumLikelihood(boolean value) {
        ml = value;
    }

    public int getAIO() {
        if (types == null) {
            return 0;
        }
        boolean ao = types.contains("AO");
        boolean ls = types.contains("LS");
        boolean tc = types.contains("TC");
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
        if (value == 1) {
            add("AO");
            add("TC");
        } else if (value == 2) {
            add("AO");
            add("TC");
            add("LS");
        } else if (value == 3) {
            add("AO");
            add("LS");
        }
    }

    public boolean isDefault() {
        return (!ml) && cv == 0 && types.isEmpty() && (span == null || span.getType() == TimeSelector.SelectionType.All)
                && tc == DEF_DELTATC;
    }

    public boolean isUsed() {
        return !types.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierSpec && equals((OutlierSpec) obj));
    }

    private boolean equals(OutlierSpec other) {
        return cv == other.cv && ml == other.ml
                && Objects.equals(span, other.span) && tc == other.tc
                && Comparator.equals(types, other.types); // the order could be different. Use Comparator
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Double.hashCode(this.tc);
        hash = 89 * hash + (this.ml ? 1 : 0);
        hash = 89 * hash + Double.hashCode(this.cv);
        hash = 89 * hash + Objects.hashCode(this.span);
        return hash;
    }

    public void remove(String outlierType) {
        types.remove(outlierType);
    }

}
