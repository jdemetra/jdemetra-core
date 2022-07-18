/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.ssf.akf;

import jdplus.ssf.StateInfo;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.IMultivariateSsfData;

/**
 *
 * @author Jean Palate
 */
public interface IMultivariateAugmentedFilteringResults {
    /**
     *
     */
    void close();

    /**
     *
     * @param ssf
     * @param data
     */
    void open(IMultivariateSsf ssf, IMultivariateSsfData data);

    /**
     *
     * @param t
     * @param pe
     */
    void save(int t, MultivariateAugmentedUpdateInformation pe);
    
    /**
     *
     * @param t
     * @param state
     * @param info
     */
    void save(int t, AugmentedState state, StateInfo info);
   
}
