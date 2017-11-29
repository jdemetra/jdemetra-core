/*
* Copyright 2013 National Bank of Belgium
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
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IFiniteFilter extends IFilter {

    /**
     * Length of the filter
     * @return
     */
    default int length(){
        return getUpperBound()-getLowerBound()+1;
    };

    // FiniteFilterDecomposition Decompose();
    /**
     * Lower bound of the filter (included)
     * @return
     */
    int getLowerBound();

    /**
     * Upper bound of the filter (included)
     * @return
     */
    int getUpperBound();

    /**
     * Weights of the filter; the function is defined for index ranging
 from the lower bound to the upper bound (included)
     * @return
     */
    IntToDoubleFunction weights();
    
    /**
     * Returns all the weights, from lbound to ubound
     * @return 
     */
    default double[] weightsToArray(){
        double[] w=new double[length()];
        IntToDoubleFunction weights = weights();
        for (int i=0, j=getLowerBound(); i<w.length; ++i, ++j){
            w[i]=weights.applyAsDouble(j);
        }
        return w;
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
     * The range of the input is implicitly defined by the filter and by the output.
     * If the filter is defined by w{lb)...w(ub) and the filter output is defined for [start, end[,
     * the input should be defined for [start-lb, end+ub[
     *
     * @param input The input
     * @param rslt The filter output, defined for the range [getStart(), getEnd()[
     */
    void apply(IntToDoubleFunction input, IFilterOutput rslt);
    
    /**
     * Apply the filter on a block of doubles and store the results in the output.
     * 
     *
     * @param in
     * @param out
     * @return
     */
    void apply(DataBlock in, DataBlock out);
    
    /**
     * Applies the filter on the input
     * @param in The input, which must have the same length as the filter
     * @return The product of the filter and of the input  
     */
    double apply(DoubleSequence in);


}
