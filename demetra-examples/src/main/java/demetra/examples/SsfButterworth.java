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
package demetra.examples;

import demetra.data.Data;
import jdplus.arima.ArimaModel;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.ssf.StateComponent;
import jdplus.ssf.arima.SsfArima;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.sts.Noise;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SsfButterworth {
    public void main(String[] args) {
        // RW of order two

        // AR, D, MA, var
        double q = 7;
        int n=4;
        Polynomial p=Polynomial.of(1, -1);
        Polynomial P=p;
        for (int i=1; i<n; ++i){
            P=P.times(p);
        }
        ArimaModel S = new ArimaModel(BackFilter.ONE, new BackFilter(P), BackFilter.ONE, 1);
        StateComponent signal = SsfArima.stateComponent(S);
        StateComponent noise = Noise.of(q);

        // create a composite state space form
        CompositeSsf ssf = CompositeSsf.builder()
                .add(signal, SsfArima.defaultLoading())
                .add(noise, Noise.defaultLoading())
                .build();

        // smoothing using Durbin-Koopman for diffuse initialization
        // and with the specified variances (not estimated)
        SsfData data = new SsfData(Data.NILE);
        DefaultSmoothingResults rslts = DkToolkit.sqrtSmooth(ssf, data, true, true);
        int[] pos = ssf.componentsPosition();
        System.out.println(rslts.getComponent(pos[0]));
        System.out.println(rslts.getComponent(pos[1]));
    }
    
}
