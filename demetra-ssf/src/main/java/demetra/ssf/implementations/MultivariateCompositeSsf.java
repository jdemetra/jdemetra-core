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
package demetra.ssf.implementations;

import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;
import demetra.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate
 */
public class MultivariateCompositeSsf extends MultivariateSsf {
    
    public static MultivariateCompositeSsf create(Matrix ecorr, ISsf... ssfs){
        CompositeDynamics dyn=CompositeDynamics.of(ssfs);
        ISsfMeasurements m=CompositeMeasurements.of(ecorr, ssfs);
        return new MultivariateCompositeSsf(dyn, m);
    }

    private MultivariateCompositeSsf(ISsfDynamics dyn, ISsfMeasurements m) {
        super(dyn, m);
    }

}
