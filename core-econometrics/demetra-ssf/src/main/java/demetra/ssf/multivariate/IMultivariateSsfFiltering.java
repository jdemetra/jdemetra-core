/*
 * Copyright 2016-2017 National Bank of Belgium
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
package demetra.ssf.multivariate;

import demetra.ssf.State;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
public interface IMultivariateSsfFiltering {
    /**
     * Computes a(t+1|t), P(t
     * @param t Time position
     * @param state On entry, contains a(t|t-1), P(t|t-1). On exit, contains 
     * a(t+1|t), P(t+1|t)
     * @param x Observations in t
     * @return Update information corresponding to the new observations
     */
    MultivariateUpdateInformation next(int t, State state, Doubles x);
}
