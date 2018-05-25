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
import demetra.maths.polynomials.UnitRoots;
import demetra.modelling.ComponentType;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class PeriodicOutlier extends AbstractOutlier {

    public static final String CODE = "SO";
    public static final String PO = "PO";

    public static class Factory implements IOutlierFactory {

        private final boolean zeroEnded;
        private final int period;

        public Factory(int period, boolean zeroEnded) {
            this.zeroEnded = zeroEnded;
            this.period = period;
        }

        @Override
        public IOutlier make(LocalDateTime position) {
            return new PeriodicOutlier(position, period, zeroEnded);
        }

        @Override
        public void fill(int xpos, DataBlock buffer) {
            double z = -1.0 / (period - 1);
            int len = buffer.length();
            if (zeroEnded) {
                int j = 1;
                do {
                    for (; j < period && xpos > 0; ++j) {
                        buffer.set(--xpos, z);
                    }
                    if (xpos > 0) {
                        buffer.set(--xpos, 1);
                    } else {
                        break;
                    }
                    j = 1;
                } while (true);
            } else {
                for (int i = xpos; i < len;) {
                    buffer.set(i++, 1);
                    for (int j = 1; j < period && i < len; ++i, ++j) {
                        buffer.set(i, z);
                    }
                }
            }
        }

        @Override
        public FilterRepresentation getFilterRepresentation() {
            return new FilterRepresentation(new RationalBackFilter(
                    BackFilter.ONE, new BackFilter(UnitRoots.D(period)), 0), 0);
        }

        @Override
        public int excludingZoneAtStart() {
            return 2 * period;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 2 * period;
        }

        @Override
        public String getCode() {
            return CODE;
        }
    }

    private final boolean zeroEnded;
    private final int period;

    public PeriodicOutlier(LocalDateTime pos, int period, boolean zeroEnded) {
        super(pos, IOutlier.defaultName(CODE, pos, null));
        this.zeroEnded = zeroEnded;
        this.period = period;
    }

    public PeriodicOutlier(LocalDateTime pos, int period, boolean zeroEnded, String name) {
        super(pos, name);
        this.zeroEnded = zeroEnded;
        this.period = period;
    }

    @Override
    protected void data(int pos, DataBlock buffer) {
        int xpos;
        double z = -1.0 / (period - 1);
        int len = buffer.length();
        if (zeroEnded) {
            int j = 1;
            if (pos < 0) {
                return;
            } else if (pos > len) {
                int n = pos - len;
                j = 1 + n % period;
                xpos = len;
            } else {
                xpos = pos;
            }
            do {
                for (; j < period && xpos > 0; ++j) {
                    buffer.set(--xpos, z);
                }
                if (xpos > 0) {
                    buffer.set(--xpos, 1);
                } else {
                    break;
                }
                j = 1;
            } while (true);
        } else {
            if (pos < 0) {
                xpos = pos % period;
            } else {
                xpos = pos;
            }
            if (xpos < 0) {
                int max = Math.min(len, period + xpos);
                for (int j = 0; j < max; ++j) {
                    buffer.set(j, z);
                }
                xpos += period;

            }

            for (int i = xpos; i < len;) {
                buffer.set(i++, 1);
                for (int j = 1; j < period && i < len; ++i, ++j) {
                    buffer.set(i, z);
                }
            }
        }
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public ComponentType getComponentType(){
        return ComponentType.Seasonal;
    }

    @Override
    public PeriodicOutlier rename(String name) {
        return new PeriodicOutlier(position, period, zeroEnded, name);
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof PeriodicOutlier) {
            PeriodicOutlier x = (PeriodicOutlier) other;
            return x.period == period && x.zeroEnded==zeroEnded
                    && x.position.equals(position);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(position);
        hash = 71 * hash + (this.zeroEnded ? 1 : 0);
        hash = 71 * hash + this.period;
        return hash;
    }
}
