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
package ec.tss.disaggregation.documents;

import ec.tss.disaggregation.processors.CalendarizationProcessor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Specification containing parameters for the Calendarization process
 *
 * @author Mats Maggi
 */
public class CalendarizationSpecification implements IProcSpecification, Cloneable {

    public static final String WEIGHTS = "dailyweights", AGGREGATE_FREQUENCY = "aggregatefrequency";

    private double[] weights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    private TsFrequency aggFrequency = TsFrequency.Monthly;

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public TsFrequency getAggFrequency() {
        return aggFrequency;
    }

    public void setAggFrequency(TsFrequency aggFrequency) {
        this.aggFrequency = aggFrequency;
    }

    @Override
    public CalendarizationSpecification clone() {
        CalendarizationSpecification clone = new CalendarizationSpecification();
        clone.setAggFrequency(aggFrequency);
        clone.setWeights(Arrays.copyOf(weights, weights.length));
        return clone;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ALGORITHM, CalendarizationProcessor.DESCRIPTOR);

        if (weights != null || verbose) {
            info.set(WEIGHTS, weights);
        }

        if (aggFrequency != null || verbose) {
            info.set(AGGREGATE_FREQUENCY, aggFrequency);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        double[] w = info.get(WEIGHTS, double[].class);
        if (w != null) {
            weights = w;
        }

        String f = info.get(AGGREGATE_FREQUENCY, String.class);
        if (f != null) {
            aggFrequency = TsFrequency.valueOf(f);
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CalendarizationSpecification && equals((CalendarizationSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Arrays.hashCode(this.weights);
        hash = 89 * hash + Objects.hashCode(this.aggFrequency);
        return hash;
    }

    public boolean equals(CalendarizationSpecification spec) {
        return Arrays.equals(weights, spec.getWeights()) && aggFrequency.equals(spec.getAggFrequency());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Daily Weights = (");
        for (int i = 0; i < weights.length; i++) {
            builder.append(String.valueOf(weights[i]));
            builder.append(i <= weights.length - 1 ? ", " : "), ");
        }
        builder.append("Agg. Freq. = ").append(aggFrequency.toString());
        return builder.toString();
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, WEIGHTS), double[].class);
        dic.put(InformationSet.item(prefix, AGGREGATE_FREQUENCY), TsFrequency.class);
    }

}
