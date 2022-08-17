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
package jdplus.ssf.utility;

import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Randoms {
    
    public FastMatrix randomMatrix(int n) {
        Random rnd = new Random();
        FastMatrix m = FastMatrix.square(n);
        m.set((a, b) -> rnd.nextDouble());
        return m;
    }

    public FastMatrix randomSymmetricMatrix(int n) {
        return SymmetricMatrix.XXt(randomMatrix(n));
    }

    public DataBlock randomArray(int n) {
        Random rnd = new Random();
        DataBlock v = DataBlock.make(n);
        v.set(a -> rnd.nextDouble());
        return v;
    }
    
}
