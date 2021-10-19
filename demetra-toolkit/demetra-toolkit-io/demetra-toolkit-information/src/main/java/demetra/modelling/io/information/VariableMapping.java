/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.modelling.io.information;

import demetra.data.Parameter;
import demetra.data.Range;
import demetra.information.Information;
import demetra.information.InformationException;
import demetra.information.InformationSet;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class VariableMapping {

    public final String NAME = "name", COEF = "coef",
            ATTRIBUTE = "attribute", ATTRIBUTES = "attribute*";

    public void writeVariableInfo(Variable var, InformationSet info) {
        info.set(NAME, var.getName());
        if (var.getCore().dim() == 1) {
            Parameter coef = var.getCoefficient(0);
            if (coef.isDefined()) {
                info.set(COEF, coef);
            }
        } else {
            Parameter[] coef = var.getCoefficients();
            if (Parameter.hasDefinedParameters(coef)) {
                info.set(COEF, coef);
            }
        }
        Map<String, String> attributes = var.getAttributes();
        int n = attributes.size();
        if (n > 0) {
            String[] a = attributes.keySet().toArray(new String[n]);
            for (int i = 0; i < n; ++i) {
                info.set(ATTRIBUTE + (i+1), new String[]{a[i], attributes.get(a[i])});
            }
        }
    }

    public <T extends ITsVariable> Variable<T> readVariableInfo(T core, InformationSet info) {

        String name = info.get(NAME, String.class);
        List<Information<String[]>> attr = info.select(ATTRIBUTES, String[].class);
        Map<String, String> map;
        if (attr.isEmpty()) {
            map = Collections.emptyMap();
        } else {
            map = new HashMap<>();
            attr.forEach(a -> {
                map.put(a.getValue()[0], a.getValue()[1]);
            });
        }
        if (core.dim() == 1) {
            Parameter p = info.get(COEF, Parameter.class);
            return Variable.variable(name, core, map).withCoefficient(p);
        } else {
            Parameter[] p = info.get(COEF, Parameter[].class);
            return Variable.variable(name, core, map).withCoefficients(p);
        }
    }

    public InformationSet writeIV(Variable<InterventionVariable> var, boolean verbose) {
        InformationSet info = InterventionVariableMapping.write(var.getCore(), verbose);
        writeVariableInfo(var, info);
        return info;
    }

    public Variable<InterventionVariable> readIV(InformationSet info) {
        return readVariableInfo(InterventionVariableMapping.read(info), info);
    }

    public Variable<IOutlier> readO(InformationSet info) {
         return readVariableInfo(OutlierMapping.read(info), info);
    }

    public InformationSet writeO(Variable<IOutlier> var, boolean verbose) {
        InformationSet info = OutlierMapping.write(var.getCore(), verbose);
        writeVariableInfo(var, info);
        return info;
    }

    public Variable<Ramp> readR(InformationSet info) {
         return readVariableInfo(RampMapping.read(info), info);
    }

    public InformationSet writeR(Variable<Ramp> var, boolean verbose) {
        InformationSet info = RampMapping.write(var.getCore());
        writeVariableInfo(var, info);
        return info;
    }

    public Variable<TsContextVariable> readT(InformationSet info) {
         return readVariableInfo(TsContextVariableMapping.read(info), info);
    }

    public InformationSet writeT(Variable<TsContextVariable> var, boolean verbose) {
        InformationSet info = TsContextVariableMapping.write(var, verbose);
        writeVariableInfo(var, info);
        return info;
    }

    public String rangeToShortString(Range<LocalDateTime> seq) {
        StringBuilder builder = new StringBuilder();
        builder.append(seq.start().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                .append(InformationSet.SEP).append(seq.end().toLocalDate().format(DateTimeFormatter.ISO_DATE));
        return builder.toString();
    }

    public Range<LocalDateTime> rangeFromShortString(String s) {
        String[] ss = InformationSet.split(s);
        if (ss.length == 1) {
            LocalDate start = LocalDate.parse(ss[0], DateTimeFormatter.ISO_DATE);
            if (start != null) {
                return Range.of(start.atStartOfDay(), start.atStartOfDay());
            }
        }
        if (ss.length != 2) {
            throw new InformationException(INVALID);
        }
        LocalDate start = LocalDate.parse(ss[0], DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(ss[1], DateTimeFormatter.ISO_DATE);
        return Range.of(start.atStartOfDay(), end.atStartOfDay());
    }

    private final String INVALID = "Invalid range";

}
