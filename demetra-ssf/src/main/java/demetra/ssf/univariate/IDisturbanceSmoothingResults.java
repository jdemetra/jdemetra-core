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
package demetra.ssf.univariate;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;


/**
 *
 * @author Jean Palate
 */
public interface IDisturbanceSmoothingResults {

    default double e(int pos){
        return 0;
    }
    
    default double eVar(int pos){
        return 0;
    }

    default DataBlock u(int pos) {
        return null;
    }

    default Matrix uVar(int pos) {
        return null;
    }
    
    void saveSmoothedTransitionDisturbances(int pos, DataBlock u, Matrix uVar);
    
    void saveSmoothedMeasurementDisturbance(int pos, double e, double evar);
}
