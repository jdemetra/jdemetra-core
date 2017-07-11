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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TransitoryChange extends AbstractOutlierVariable {

    public static final String CODE = "TC";

    private final double coefficient;
    private final boolean monthlyCoefficient;

    static double ZERO = 1e-15;

    /**
     *
     * @param p
     */
    public TransitoryChange(Day p) {
        super(p);
        coefficient = TransitoryChangeFactory.DEF_TCRATE;
        monthlyCoefficient = false;
    }

    /**
     *
     * @param p
     * @param c
     */
    public TransitoryChange(Day p, double c) {
        super(p);
        coefficient = c;
        monthlyCoefficient = false;
    }

    /**
     *
     * @param p
     * @param c
     */
    public TransitoryChange(Day p, double c, boolean monthlyCoefficient) {
        super(p);
        coefficient = c;
        this.monthlyCoefficient = monthlyCoefficient;
    }

    private double coefficient(int freq) {
        double c = coefficient;
        if (monthlyCoefficient) {
            int r = 12 / freq;
            if (r > 1) {
                c = Math.pow(c, r);
            }
        }
        return c;
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {

        data.set(0);
        double cur = 1;
        int n = data.getLength();
        TsPeriod pstart = new TsPeriod(start.getFrequency(), position);
        int i = pstart.minus(start);
        double c=coefficient(start.getFrequency().intValue());
        for (; i < 0; ++i) {
            cur *= c;
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }

        for (; i < n; ++i) {
            data.set(i, cur);
            cur *= c;
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }
    }

    /**
     *
     * @return
     */
    public double getCoefficient() {
        return coefficient;
    }
    
    public boolean isMonthlyCoefficient(){
        return monthlyCoefficient;
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.TC;
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        int n = domain.search(position);
        return (n >= 0 && n < domain.getLength() - 1);
    }

    @Override
    public FilterRepresentation getFilterRepresentation(int freq) {

        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.of(new double[]{1, -coefficient(freq)})), 0);
    }
}
