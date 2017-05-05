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
package demetra.maths.matrices.impl;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class HouseholderTest {
    
    public HouseholderTest() {
    }

    @Test
    public void testQ() {
        Matrix X=Matrix.make(20, 5);
        Random rnd=new Random();
        X.set(()->rnd.nextDouble());
        Householder qr=new Householder();
        qr.decompose(X);
        DataBlock x=DataBlock.make(X.getRowsCount());
        x.set(()->rnd.nextDouble());
        DataBlock x0=DataBlock.copyOf(x);
        qr.applyQt(x);
        qr.applyQ(x);
        assertEquals(x.distance(x0), 0, 1e-9);
        x=DataBlock.copyOf(x0).reverse();
        qr.applyQt(x);
        qr.applyQ(x);
        assertEquals(x.distance(x0.reverse()), 0, 1e-9);
    }
    
}
