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

import demetra.timeseries.TimeSelector;
import demetra.utilities.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
public class OutlierSpec {

    public static enum Method {

        AddOne,
        AddAll
    }
    private ArrayList<SingleOutlierSpec> types = new ArrayList<>();
    private int lsrun = 0;
    private Method method = Method.AddOne;
    private double tc = DEF_TCRATE, defcv = 0;
    private TimeSelector span = TimeSelector.all();
    private int nmax = DEF_NMAX;
    public static final double DEF_TCRATE = .7, DEF_VA = 4.0;
    public static final int DEF_NMAX = 30;

    public OutlierSpec() {
    }

    public OutlierSpec(OutlierSpec other) {
        this.defcv=other.defcv;
        this.lsrun=other.lsrun;
        this.method=other.method;
        this.nmax=other.nmax;
        this.span=other.span;
        this.tc=other.tc;
        other.types.forEach(types::add);
    }

    public void reset() {
        types.clear();
        lsrun = 0;
        method = Method.AddOne;
        tc = DEF_TCRATE;
        defcv = 0;
        span = TimeSelector.all();
        nmax = DEF_NMAX;
    }

    public boolean isUsed() {
        return !types.isEmpty();
    }

    public int getTypesCount() {
        return types.size();
    }

    public SingleOutlierSpec[] getTypes() {
        return types.toArray(new SingleOutlierSpec[types.size()]);
    }

    public void setTypes(SingleOutlierSpec[] value) {
        types.clear();
        if (value != null) {
            for (SingleOutlierSpec sspec : value) {
                add(sspec);
            }
        }
    }

    public void clearTypes() {
        types.clear();
    }

    public void add(String type) {
        SingleOutlierSpec spec = new SingleOutlierSpec(type, defcv);
        add(spec);
    }

    public void add(SingleOutlierSpec spec) {
        int pos = -1;
        for (int i = 0; i < types.size(); ++i) {
            if (types.get(i).getType().equals(spec.getType())) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            types.add(spec);
        } else {
            types.set(pos, spec);
        }
    }

    public SingleOutlierSpec search(String type) {
        for (SingleOutlierSpec s : types) {
            if (s.getType().equals(type)) {
                return s;
            }
        }
        return null;
    }

    public void remove(String type) {
        for (SingleOutlierSpec s : types) {
            if (s.getType().equals(type)) {
                types.remove(s);
                return;
            }
        }
    }

    public int getLSRun() {
        return lsrun;
    }

    public void setLSRun(int value) {
        lsrun = value;
    }

    public int getMaxIter() {
        return nmax;
    }

    @Deprecated
    public void seMaxIter(int value) {
        nmax = value;
    }

    public void setMaxIter(int value) {
        nmax = value;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method value) {
        method = value;
    }

    public TimeSelector getSpan() {
        return span;
    }

    public void setSpan(@Nonnull TimeSelector value) {
            span = value;
    }

    public double getMonthlyTCRate() {
        return tc;
    }

    public void setMonthlyTCRate(double value) {
        tc = value;
    }

    /// <summary>
    /// When the default critical value is changed, all the current outliers' critical values are accordingly modified
    /// </summary>
    public double getDefaultCriticalValue() {
        return defcv;
    }

    public void setDefaultCriticalValue(double value) {
        double old=defcv;
        defcv = value;
        for (int i=0; i<types.size(); ++i){
            SingleOutlierSpec cur = types.get(i);
            if (cur.getCriticalValue() == old){
                types.set(i, new SingleOutlierSpec(cur.getType(), defcv));
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierSpec && equals((OutlierSpec) obj));
    }

    private boolean equals(OutlierSpec other) {
        return defcv == other.defcv && lsrun == other.lsrun && method == other.method && Objects.equals(span, other.span)
                && nmax == other.nmax && tc == other.tc && Comparator.equals(types, other.types);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.method);
        hash = 71 * hash + Double.hashCode(this.tc);
        hash = 71 * hash + Double.hashCode(this.defcv);
        return hash;
    }

}
