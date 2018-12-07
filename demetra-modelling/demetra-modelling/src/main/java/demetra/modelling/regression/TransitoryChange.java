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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TransitoryChange extends AbstractOutlier {

    static double ZERO = 1e-15;
    public static final String CODE = "TC";

    public static final class Factory implements IOutlierFactory {

        private final double coefficient;

        public Factory(double coefficient) {
            this.coefficient = coefficient;
        }

        @Override
        public IOutlier make(LocalDateTime position) {
            return new TransitoryChange(position, coefficient);
        }

        @Override
        public void fill(int outlierPosition, DataBlock buffer) {
            double cur = 1;
            int n = buffer.length();
            for (int pos = outlierPosition; pos < n; ++pos) {
                buffer.set(pos, cur);
                cur *= coefficient;
                if (Math.abs(cur) < ZERO) {
                    return;
                }
            }
        }

        @Override
        public FilterRepresentation getFilterRepresentation() {
            return new FilterRepresentation(new RationalBackFilter(
                    BackFilter.ONE, BackFilter.ofInternal(1, -coefficient), 0), 0);
        }

        @Override
        public int excludingZoneAtStart() {
            return 0;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 1;
        }

        @Override
        public String getCode() {
            return CODE;
        }

    }

    private final double coefficient;

    public TransitoryChange(LocalDateTime pos, double coefficient) {
        super(pos, IOutlier.defaultName(CODE, pos, null));
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
        return CODE;
    }

    @Override
    public TransitoryChange rename(String name) {
        return new TransitoryChange(position, coefficient, name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SwitchOutlier) {
            TransitoryChange x = (TransitoryChange) other;
            return this.coefficient == x.coefficient && this.position.equals(x.position);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.coefficient) ^ (Double.doubleToLongBits(this.coefficient) >>> 32));
        hash = 97 * hash + Objects.hash(position);
        return hash;
    }
}
