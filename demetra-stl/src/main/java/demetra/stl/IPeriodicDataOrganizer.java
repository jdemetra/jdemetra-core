/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.stl;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
public interface IPeriodicDataOrganizer {

    int getPeriod();

    IPeriodicDataGetters getters(IDataGetter input);

    IPeriodicDataSelectors selectors(IDataSelector output);
    
    IntToDoubleFunction weights(IntToDoubleFunction fn, int period);

    static IPeriodicDataOrganizer of(final int np) {
        return new IPeriodicDataOrganizer() {
            @Override
            public IPeriodicDataGetters getters(final IDataGetter input) {
                return new IPeriodicDataGetters() {
                    @Override
                    public int getPeriod() {
                        return np;
                    }

                    @Override
                    public IDataGetter get(int period) {
                        return of(input, np, period);
                    }
                };
            }

            @Override
            public IPeriodicDataSelectors selectors(IDataSelector output) {
                return period -> of(output, np, period);
            }

            @Override
            public int getPeriod() {
                return np;
            }

            @Override
            public IntToDoubleFunction weights(final IntToDoubleFunction fn, int period) {
                return fn == null ? null : i->fn.applyAsDouble(i*np+period);
            }
        };
    }

    static IDataGetter of(final IDataGetter src, final int nperiods, final int period) {
        return new IDataGetter() {
            @Override
            public double get(int idx) {
                return src.get(idx * nperiods + period);
            }

            @Override
            public int getStart() {
                return (src.getStart()-nperiods+period+1) / nperiods ;
            }

            @Override
            public int getEnd() {
                return getStart()+getLength();
            }

            @Override
            public int getLength() {
                return 1+(src.getLength()-period-1) / nperiods;
            }
        };
    }

    static IDataSelector of(final IDataSelector src, final int nperiods, final int period) {
        return new IDataSelector() {
            @Override
            public double get(int idx) {
                return src.get(idx * nperiods + period);
            }

            @Override
            public void set(int idx, double value) {
                src.set(idx * nperiods + period, value);
            }

            @Override
            public int getStart() {
                return (src.getStart()-nperiods+period+1) / nperiods ;
            }

            @Override
            public int getEnd() {
                return getStart()+getLength();
            }

            @Override
            public int getLength() {
                return 1+(src.getLength()-period-1) / nperiods;
            }
        };
    }
}
