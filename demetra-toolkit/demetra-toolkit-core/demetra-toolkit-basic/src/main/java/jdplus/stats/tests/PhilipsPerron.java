/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests;

/**
 *
 * @author PALATEJ
 */
public class PhilipsPerron {
    
    

    private double s2(double[] u, int n, int l) {

        double tmp1 = 0.0;
        for (int i = 1; i <=l; i++) {
            double tmp2 = 0.0;
            for (int j = i; j < n; j++) {
                tmp2 += u[j] * u[j-1];
            }
            tmp2 *= 1.0 - i / (l + 1.0);
            tmp1 += tmp2;
        }
        tmp1 /= n;
        tmp1 *= 2.0;
        return tmp1;
    }

}
