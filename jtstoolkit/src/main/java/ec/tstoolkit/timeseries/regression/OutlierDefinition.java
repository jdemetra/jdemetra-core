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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.StringFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class OutlierDefinition implements Comparable<OutlierDefinition> {

    public Day position;
    public OutlierType type;
    public boolean prespecified;

    /**
     *
     * @param period
     * @param type
     */
    public OutlierDefinition(TsPeriod period, OutlierType type, boolean prespecified) {
        position = period.firstday();
        this.type = type;
        this.prespecified = prespecified;
    }

    /**
     *
     * @param period
     * @param type
     */
    public OutlierDefinition(Day pos, OutlierType type, boolean prespecified) {
        position = pos;
        this.type = type;
        this.prespecified = prespecified;
    }

    public Day getPosition() {
        return position;
    }

    public void setPosition(Day position) {
        this.position = position;
    }

    public OutlierType getType() {
        return type;
    }

    public void setType(OutlierType type) {
        this.type = type;
    }

    public boolean isPrespecified() {
        return prespecified;
    }

    public void setPrespecified(boolean prespecified) {
        this.prespecified = prespecified;
    }

    public OutlierDefinition prespecify(boolean val) {
        if (val == prespecified) {
            return this;
        } else {
            return new OutlierDefinition(position, type, val);
        }
    }

    public static OutlierDefinition[] prespecify(OutlierDefinition[] o, boolean val) {
        if (o == null || o.length == 0) {
            return o;
        }
        OutlierDefinition[] no = new OutlierDefinition[o.length];

        for (int i = 0; i < no.length; ++i) {
            no[i] = o[i].prespecify(val);
        }
        return no;
    }

    @Override
    public int compareTo(OutlierDefinition o) {
        if (type == o.type) {
            return position.compareTo(o.position);
        } else {
            return type.compareTo(o.type);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierDefinition && equals((OutlierDefinition) obj));
    }

    private boolean equals(OutlierDefinition other) {
        return other.position.equals(position) && other.type.equals(type);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.position);
        hash = 19 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type).append(InformationSet.SEP).append(StringFormatter.convert(position));
        if (this.prespecified) {
            builder.append(InformationSet.SEP).append('f');
        }
        return builder.toString();
    }

    public String toString(TsFrequency freq) {
        StringBuilder builder = new StringBuilder();
        builder.append(type).append(InformationSet.SEP).append(StringFormatter.write(new TsPeriod(freq, position)));
        if (this.prespecified) {
            builder.append(InformationSet.SEP).append('f');
        }
        return builder.toString();
    }

    public static OutlierDefinition fromString(String s) {
        String[] ss = InformationSet.split(s);
        if (ss.length != 2 && ss.length != 3) {
            return null;
        }
        OutlierType type = OutlierType.valueOf(ss[0]);
        if (type == null) {
            return null;
        }
        boolean p = false;
        if (ss.length == 3) {
            if (!ss[2].equals("f")) {
                return null;
            } else {
                p = true;
            }
        }
        Day day = StringFormatter.convertDay(ss[1]);
        if (day != null) {
            return new OutlierDefinition(day, type, p);
        }
        TsPeriod period = StringFormatter.readPeriod(ss[1]);
        if (period != null) {
            return new OutlierDefinition(period, type, p);
        } else {
            return null;
        }
    }

    public static List<OutlierDefinition> of(List<IOutlierVariable> vars) {
        List<OutlierDefinition> defs = new ArrayList<>();
        for (IOutlierVariable var : vars) {
            defs.add(new OutlierDefinition(var.getPosition(), var.getOutlierType(), var.isPrespecified()));
        }
        return defs;
    }

}
