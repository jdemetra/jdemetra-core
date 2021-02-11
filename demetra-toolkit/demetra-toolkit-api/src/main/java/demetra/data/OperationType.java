/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.data;

import nbbrd.design.Development;
import java.util.function.DoubleUnaryOperator;

/**
 * Elementary operations (+,-,*,/)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public enum OperationType {
    /**
     * No operation
     */
    None,
    /**
     * Difference
     */
    Diff,
    /**
     *
     */
    Sum,
    /**
     * Ratio
     */
    Ratio,
    /**
     * Product
     */
    Product;

    public OperationType reverse() {
        switch (this) {
            case Diff:
                return Sum;
            case Sum:
                return Diff;
            case Ratio:
                return Product;
            case Product:
                return Ratio;
            default:
                return None;
        }
    }
    
    public DoubleUnaryOperator asOperator(final double c){
       switch (this) {
            case Diff:
                return x->x-c;
            case Sum:
                return x->x+c;
            case Ratio:
                return x->x*c;
            case Product:
                return x->x/c;
            default:
                return x->x;
        }
        
    }
}
