/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.r;

import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.FiniteFilter;
import demetra.maths.linearfilters.SymmetricFilter;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {
    public double[] filter(double[] data, int horizon, int degree, String kernel, String endpoints, double ic){
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = demetra.maths.linearfilters.LocalPolynomialFilters.of(horizon, degree, weights);
        FiniteFilter[] afilters=null;
        if (endpoints == "DAF"){
            afilters=new FiniteFilter[horizon];
            for (int i=0; i<afilters.length; ++i){
                afilters[i]=demetra.maths.linearfilters.LocalPolynomialFilters.directAsymmetricFilter(horizon, i, degree, weights);
            }
        }else{
            int u=0;
            switch (endpoints){
                case "LC": u=0;break;
                case "QL": u=1;break;
                case "CQ": u=2;break;
            }
            afilters=new FiniteFilter[horizon];
            for (int i=0; i<afilters.length; ++i){
                afilters[i]=demetra.maths.linearfilters.LocalPolynomialFilters.asymmetricFilter(filter, i, u, new double[]{ic}, null);
            }
        }
        
        DoubleSequence rslt = demetra.maths.linearfilters.LocalPolynomialFilters.filter(DoubleSequence.ofInternal(data)
                , filter, afilters);
        return rslt.toArray();
    }
    
    IntToDoubleFunction weights(int horizon, String filter){
        switch (filter){
            case "None": return demetra.maths.linearfilters.LocalPolynomialFilters.constant();
            case "Fn2": return demetra.maths.linearfilters.LocalPolynomialFilters.fn2(horizon);
            case "Fn3": return demetra.maths.linearfilters.LocalPolynomialFilters.fn3(horizon);
            default: return demetra.maths.linearfilters.LocalPolynomialFilters.hendersonWeights(horizon);
        }
    }
    

    
}
