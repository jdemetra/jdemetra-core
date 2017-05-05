/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.data.Sequence;
import internal.Tripwire;
import java.util.function.ObjDoubleConsumer;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <P> period type
 * @param <V> value type
 * @param <O> observation type
 */
public interface ITimeSeries<P extends ITimePeriod, V extends Number, O extends TimeObservation<P, V>> extends Sequence<O> {

    /**
     * Retrieves the time domain of this time series
     *
     * @return
     */
    ITimeDomain<P> getDomain();

    /**
     * Retrieves the content of this time series
     *
     * @return The content of this time series.
     */
    Sequence<V> getValues();

    @Nonnegative
    @Override
    default int length() {
        return getValues().length();
    }

    @Nonnull
    default P getPeriod(@Nonnegative int index) throws IndexOutOfBoundsException {
        return getDomain().elementAt(index);
    }

    default V getValue(@Nonnegative int index) throws IndexOutOfBoundsException {
        return getValues().elementAt(index);
    }

    interface OfDouble<P extends ITimePeriod, O extends TimeObservation.OfDouble<P>> extends ITimeSeries<P, Double, O> {

        @Override
        Sequence.OfDouble getValues();

        default double getDoubleValue(@Nonnegative int index) throws IndexOutOfBoundsException {
            return getValues().get(index);
        }

        @Override
        default Double getValue(int index) throws IndexOutOfBoundsException {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling ITimeSeries.OfDouble.getValue()");
            }
            return getDoubleValue(index);
        }

        default void forEach(@Nonnull ObjDoubleConsumer<P> consumer) {
            for (int i = 0; i < length(); i++) {
                consumer.accept(getPeriod(i), getDoubleValue(i));
            }
        }
    }
}
