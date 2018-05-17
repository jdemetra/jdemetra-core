/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.ts;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Utility {

    public Day of(String date) {
        try {
            return Day.fromString(date);
        } catch (ParseException ex) {
            throw new RuntimeException("Unvalid date format");
        }
    }

    public String toString(Date date) {
        return new Day(date).toString();
//        StringBuilder builder = new StringBuilder();
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTime(date);
//        builder.append(gc.get(GregorianCalendar.YEAR)).append('-')
//                .append(gc.get(GregorianCalendar.MONTH)+1).append('-')
//                .append(gc.get(GregorianCalendar.DAY_OF_MONTH));
//        return builder.toString();
    }

    public String toString(Day day) {
        return day == null || day == Day.BEG || day == Day.END ? "" : day.toString();
    }

    public Parameter[] parameters(double[] values) {
        return parameters(values, null);
    }

    public Parameter[] parameters(double[] values, boolean[] fixed) {
        Parameter[] p = new Parameter[values.length];
        for (int i = 0; i < p.length; ++i) {
            if (Double.isFinite(values[i])) {
                if (fixed != null && fixed[i]) {
                    p[i] = new Parameter(values[i], ParameterType.Fixed);
                } else {
                    p[i] = new Parameter(values[i], ParameterType.Initial);
                }
            } else {
                p[i] = new Parameter();
            }
        }
        return p;
    }

    public String outlierName(String code, String date, int frequency) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
        if (frequency == 0) {
            builder.append(date);
        } else {
            TsPeriod p = new TsPeriod(TsFrequency.valueOf(frequency), of(date));
            builder.append(p);
        }
        return builder.append(')').toString();
    }

    @lombok.Value
    public static class Outlier {

        private String code;
        private String position;
        private double coefficient;
    }

    @lombok.Value
    public static class Ramp {

        private String start, end;
        private double coefficient;
    }

    @lombok.Value
    public static class UserDefinedVariable {

        private String name;
        private String component;
        private double coefficient;
    }

    public static final String R = "r", RPREFIX = "r@";

    public static class Dictionary {

        private final Map<String, TsData> dictionary = new HashMap<>();

        public void add(String name, TsData s) {
            dictionary.put(name, s);
        }

        public ProcessingContext toContext() {
            ProcessingContext context = new ProcessingContext();
            if (!dictionary.isEmpty()) {
                TsVariables vars = new TsVariables();
                dictionary.forEach((n, s) -> vars.set(n, new TsVariable(n, s)));
                context.getTsVariableManagers().set(R, vars);
            }
            return context;
        }
    }
}
