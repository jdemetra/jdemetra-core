/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.x13.r;

import demetra.data.Data;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import org.junit.Test;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class X11Test {

    public X11Test() {
    }

    @Test
    public void testSomeMethod() {
        X11Results rslt = X11.process(Data.TS_PROD, X11Spec.DEFAULT);
        Matrix data = rslt.getData("all", Matrix.class);
//        System.out.println(data);
    }

    @Test
    public void testBF() {
        X11Spec spec = X11Spec.builder()
                .backcastHorizon(10)
                .forecastHorizon(19)
                .build();
        X11Results rslt = X11.process(Data.TS_PROD, spec);
        Matrix data = rslt.getData("all", Matrix.class);
        byte[] buffer = X11.toBuffer(rslt);
        System.out.println(buffer.length);
//        System.out.println(data);
    }
}
