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

import demetra.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <T>
 */
@Development(status = Development.Status.Exploratory)
public interface Field<T> extends Ring<T>{

    /**
     * 
     * @return 
     */
    @Override
    T one();

    /**
     * y = 1/x <-> y * x = 1
     * @param x
     * @return 
     */
    T inv(T x);
    
    /**
     * x = a / b <-> x = a * (1 / b)
     * @param a
     * @param b
     * @return 
     */
    default T div(T a, T b){
        return times(a, inv(b));
    }
}
