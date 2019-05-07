/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data.analysis;

import demetra.design.Development;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public enum WindowFunction {

    Square,
    Welch,
    Tukey,
    Bartlett,
    Hamming,
    Parzen;

    /**
     * Returns the window function, defined on ]-1, 1[ 
     * The window function is strictly positive,  even (f(-x)=f(x)) and
     * reaches its max at 0, with f(0)=1
     * 
     * @return The function
     */
    public DoubleUnaryOperator window() {
        switch (this) {
            case Welch:
                return x-> 1.0 - x*x;
            case Tukey:
                return x->0.5 * (1 + Math.cos(Math.PI * x));
            case Bartlett:
                return x->1-x;
            case Hamming:
                return x->0.54 + 0.46 * Math.cos(Math.PI * x);
            case Parzen:
                return x-> x<=.5 ? (1.0 - 6.0 * Math.pow(x, 2.0) + 6 * Math.pow(x, 3.0)) 
                        :2 * Math.pow(1.0 - x, 3);
            case Square:
                return x->1;
        }
        return null;
    }
    
    /**
     * Return the weights of the half of the window
     * (the full window length is 2*windowLength-1)
     * @param length The number of items to be returned 
     * @return w[0],...,w[(windowLength-1)/windowLength]
     */
    public double[] discreteWindow(int length){
        double[] win=new double[length];
        double dlen=length;
        DoubleUnaryOperator fn=window();
        for (int i=0; i<win.length; ++i){
            win[i]=fn.applyAsDouble(i/dlen);
        }
        return win;
    }
    
    /**
     * Applies the window on a given even function
     * @param fn An even function (f(x)=f(-x))
     * @param windowLength 
     * @return w[-len]f(-len)+...+w[0]f(0)+...w[len]f(len) =
     *    w[0]f(0)+2*w[1]f(1)+...+2*w[len]f(len)
     */
    public double computeSymmetric(IntToDoubleFunction fn, int windowLength){
        double[] window=discreteWindow(windowLength);
        double v=fn.applyAsDouble(0)*window[0];
        for (int i=1; i<windowLength; ++i){
            v+=2*window[i]*fn.applyAsDouble(i);
        }
        return v;
    }
    
    /**
     * Applies the window on a given function
     * @param fn A function
     * @param windowLength 
     * @return w[-len]f(-len)+...+w[0]f(0)+...w[len]f(len) 
      */
    public double compute(IntToDoubleFunction fn, int windowLength){
        double[] window=discreteWindow(windowLength);
        double v=fn.applyAsDouble(0)*window[0];
        for (int i=1; i<windowLength; ++i){
            v+=window[i]*(fn.applyAsDouble(i)+fn.applyAsDouble(-i));
        }
        return v;
    }

}
