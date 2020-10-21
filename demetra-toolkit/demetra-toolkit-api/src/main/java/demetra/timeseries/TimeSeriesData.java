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

import java.util.function.ObjDoubleConsumer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;
import demetra.data.Seq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <P> period type
 * @param <O> observation type
 */
public interface TimeSeriesData<P extends TimeSeriesInterval<?>, O extends TimeSeriesObs<P>> extends Seq<O> {

    /**
     * Retrieves the time domain of this time series
     *
     * @return
     */
    @NonNull
    TimeSeriesDomain<P> getDomain();

    /**
     * Retrieves the content of this time series
     *
     * @return The content of this time series.
     */
    @NonNull
    DoubleSeq getValues();

    @NonNegative
    @Override
    default int length() {
        return getValues().length();
    }

    @NonNull
    default P getPeriod(@NonNegative int index) throws IndexOutOfBoundsException {
        return getDomain().get(index);
    }

    default double getValue(@NonNegative int index) throws IndexOutOfBoundsException {
        return getValues().get(index);
    }

    default void forEach(@NonNull ObjDoubleConsumer<P> consumer) {
        for (int i = 0; i < length(); i++) {
            consumer.accept(getPeriod(i), getValue(i));
        }
    }
}
