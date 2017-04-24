/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.data;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Philippe Charles
 */
public final class IntToDoubleSequence implements Sequence.OfDouble {

    private final int length;
    private final IntToDoubleFunction fn;

    public IntToDoubleSequence(int length, IntToDoubleFunction fn) {
        this.length = length;
        this.fn = fn;
    }

    @Override
    public double getDouble(int idx) {
        return fn.applyAsDouble(idx);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int n = length();
        if (n > 0) {
            builder.append(getDouble(0));
            for (int i = 1; i < n; ++i) {
                builder.append('\t').append(getDouble(i));
            }
        }
        return builder.toString();
    }
}
