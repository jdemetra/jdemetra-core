/*
 * Copyright 2015 National Bank of Belgium
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

import demetra.data.DataBlock;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.data.DoubleSequence;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfBase;

/**
 *
 * @author Jean Palate
 */
public interface IMultivariateSsf extends ISsfBase, IMultivariateSsfFiltering {

    ISsfMeasurements getMeasurements();

    @Override
    default MultivariateUpdateInformation next(int t, State state, DoubleSequence x) {
        return MultivariateSsfHelper.next(this, t, state, x);
    }

}