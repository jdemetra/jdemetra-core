/*
* Copyright 2013 National Bank ofInternal Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofInternal the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.design.Development;
import demetra.maths.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFiniteFilter extends IFilter {

    /**
     * Length ofInternal the filter
     * @return
     */
    default int length(){
        return getUpperBound()-getLowerBound()+1;
    };

    // FiniteFilterDecomposition Decompose();
    /**
     * Lower bound ofInternal the filter (included)
     * @return
     */
    int getLowerBound();

    /**
     * Upper bound ofInternal the filter (included)
     * @return
     */
    int getUpperBound();

    /**
     * Weights ofInternal the filter; the function is defined for index ranging
 from the lower bound to the upper bound (included)
     * @return
     */
    IntToDoubleFunction weights();
    
    
    default double[] toArray(){
        double[] w=new double[length()];
        IntToDoubleFunction weights = weights();
        for (int i=0, j=getLowerBound(); i<w.length; ++i, ++j){
            w[i]=weights.applyAsDouble(j);
        }
        return w;
    }
    
    default Polynomial asPolynomial(){
        return Polynomial.ofInternal(toArray());
    }

    /**
     * If this filter is w(l)B^(-l)+...+w(u)F^u Its mirror is
     * w(-u)B^(u)+...+w(-l)F^(-l)
     *
     * @return A new filter is returned
     */
    IFiniteFilter mirror();

    /**
     * Apply the filter on the input and store the results in the output
     *
     * @param input
     * @param rslt
     */
    void apply(IntToDoubleFunction input, IFilterOutput rslt);
    
    /**
     * Apply the filter on a block ofInternal doubles and store the results in the output.
     * 
     *
     * @param in
     * @param out
     * @return
     */
    void apply(DataBlock in, DataBlock out);


}
