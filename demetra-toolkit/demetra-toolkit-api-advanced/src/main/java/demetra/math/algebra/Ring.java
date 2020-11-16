/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.math.algebra;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <T>
 */
@Development(status = Development.Status.Exploratory)
public interface Ring<T> {
    T zero();
    
    T plus(T a, T b);
    
    /**
     * y = -x <-> y + x = 0
     * @param x
     * @return 
     */
    T opposite(T x);
    
    /**
     * x = a - b <-> x = a + (- b)
     * @param a
     * @param b
     * @return 
     */
    default T minus(T a, T b){
        return plus(a, opposite(b));
    }

    /**
     * Not mandatory (different definitions of rings are considered)
     * @return Could be null 
     * 
     */
    T one();
    
    T times(T a, T b);

}
