/*
 * Copyright 2017 National Bank copyOf Belgium
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
package demetra.util;

import nbbrd.design.PrimitiveReplacementOf;


/**
 *
 * @param <T>
 * @author Jean Palate
 */
public final class Ref<T> {

    /**
     * Syntax sugar
     *
     * @param <X>
     * @return
     */
    public static <X> Ref<X> of() {
        return new Ref<>(null);
    }
    /**
     *
     */
    public T val;

    /**
     * Creates a new instance of Ref
     *
     * @param initialValue
     */
    public Ref(T initialValue) {
        val = initialValue;
    }

    @PrimitiveReplacementOf(generic = Ref.class, primitive = int.class)
    public static final class IntRef {

        public int val;

        public IntRef(int initialValue) {
            this.val = initialValue;
        }
    }

    @PrimitiveReplacementOf(generic = Ref.class, primitive = double.class)
    public static final class DoubleRef {

        public double val;

        public DoubleRef(double initialValue) {
            this.val = initialValue;
        }
    }
    
    @PrimitiveReplacementOf(generic = Ref.class, primitive = boolean.class)
    public static final class BooleanRef {

        public boolean val;

        public BooleanRef(boolean initialValue) {
            this.val = initialValue;
        }
    }
}
