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
import ec.tstoolkit.design.Immutable;
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
@Development(status = Development.Status.Release)
@Immutable
public class OutlierDefinition implements Comparable<OutlierDefinition> {

    private final Day position;
    private final String code;

    /**
     *
     * @param period
     * @param type
     */
    public OutlierDefinition(TsPeriod period, OutlierType type) {
        position = period.firstday();
        this.code = type.name();
    }

    /**
     *
     * @param pos
     * @param type
     */
    public OutlierDefinition(Day pos, OutlierType type) {
        position = pos;
        this.code = type.name();
    }

    /**
     *
     * @param period
     * @param type
     */
    public OutlierDefinition(TsPeriod period, String code) {
        position = period.firstday();
        this.code = code;
    }

    /**
     *
     * @param pos
     * @param type
     */
    public OutlierDefinition(Day pos, String code) {
        position = pos;
        this.code = code;
    }

    public Day getPosition() {
        return position;
    }

    public OutlierType getType() {
        try {
            return OutlierType.valueOf(code);
        } catch (IllegalArgumentException ex) {
            return OutlierType.Undefined;
        }
    }

    public String getCode() {
        return code;
    }

    @Override
    public int compareTo(OutlierDefinition o) {
        if (code.equals(o.code)) {
            return position.compareTo(o.position);
        } else {
            return code.compareTo(o.code);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof OutlierDefinition && equals((OutlierDefinition) obj));
    }

    private boolean equals(OutlierDefinition other) {
        return other.position.equals(position) && other.code.equals(code);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.position);
        hash = 19 * hash + Objects.hashCode(this.code);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(InformationSet.SEP).append(StringFormatter.convert(position));
        return builder.toString();
    }

    public String toString(TsFrequency freq) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(InformationSet.SEP).append(StringFormatter.write(new TsPeriod(freq, position)));
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
            return new OutlierDefinition(day, type);
        }
        TsPeriod period = StringFormatter.readPeriod(ss[1]);
        if (period != null) {
            return new OutlierDefinition(period, type);
        } else {
            return null;
        }
    }

    public static List<OutlierDefinition> of(List<IOutlierVariable> vars) {
        List<OutlierDefinition> defs = new ArrayList<>();
        for (IOutlierVariable var : vars) {
            defs.add(new OutlierDefinition(var.getPosition(), var.getOutlierType()));
        }
        return defs;
    }

}
