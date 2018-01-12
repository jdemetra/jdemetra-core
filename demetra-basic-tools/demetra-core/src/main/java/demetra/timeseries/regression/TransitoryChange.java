/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.RegularDomain;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
public class TransitoryChange extends AbstractOutlier {

    static double ZERO = 1e-15;
    public static final String TC = "TC";

    private final double coefficient;

    public TransitoryChange(LocalDateTime pos, double coefficient) {
        super(pos, defaultName(TC, pos, null));
        this.coefficient = coefficient;
    }

    public TransitoryChange(LocalDateTime pos, double coefficient, String name) {
        super(pos, name);
        this.coefficient = coefficient;
    }

    /**
     *
     * @return
     */
    public double getCoefficient() {
        return coefficient;
    }

    @Override
    protected void data(int pos, DataBlock buffer) {
        buffer.set(0);
        double cur = 1;
        int n = buffer.length();
        for (; pos < 0; ++pos) {
            cur *= coefficient;
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }

        for (; pos < n; ++pos) {
            buffer.set(pos, cur);
            cur *= coefficient;
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }
    }

    @Override
    public String getCode() {
        return TC;
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ofInternal(1, -coefficient), 0), 0);
    }

    @Override
    public TransitoryChange rename(String name) {
        return new TransitoryChange(position, coefficient, name);
    }

}
