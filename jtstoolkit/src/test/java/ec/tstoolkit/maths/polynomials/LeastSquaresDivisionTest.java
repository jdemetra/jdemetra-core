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

package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.data.DataBlock;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pcuser
 */
public class LeastSquaresDivisionTest {
    
    public LeastSquaresDivisionTest() {
    }

    @Test
    public void testDivision() {
        int q=10, d=20;
        DataBlock Q=new DataBlock(q), D=new DataBlock(d);
        Q.randomize(0);
        D.randomize(1);
        Polynomial pq=Polynomial.copyOf(Q.getData());
        Polynomial pd=Polynomial.copyOf(D.getData());
        Polynomial pn=pd.times(pq);
        LeastSquaresDivision div=new LeastSquaresDivision();
        div.divide(pn, pd);
//        if (div.isExact()){
//             DataBlock Q2=new DataBlock(div.getQuotient());
//             System.out.println(Q2.distance(Q));
//        }
    }
}
