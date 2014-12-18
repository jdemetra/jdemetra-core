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
package ec.tstoolkit.maths.matrices.lapack;

/**
 *
 * @author PCuser
 */
public class Dscal {

    /**
     *
     * @param n
     * @param da
     * @param dx
     * @param ix
     * @param incx
     */
    public static void fn(int n, double da, double[] dx, int ix, int incx) {
        if (n <= 0 || incx <= 0) {
            return;
        }
        if (incx != 1) {
	    //
            // code for increment not equal to 1
            //
            int nincx = ix + n * incx;
            for (int i = ix; i < nincx; i += incx) {
                dx[i] *= da;
            }
        } else {
            for (int i = 0; i < n; ++i) {
                dx[ix++] *= da;
            }
        }
    }

}
