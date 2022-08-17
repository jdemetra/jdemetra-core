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
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.StateComponent;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.sts.LocalLinearTrend;
import jdplus.ssf.sts.Noise;
import jdplus.ssf.univariate.SsfData;

/**
 * Local linear trend + Noise
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Ssf1 {
    public void main(String[] args){
        StateComponent cmp = LocalLinearTrend.stateComponent(.1, .01);
        StateComponent n = Noise.of(1);
        
        // create a composite state space form
        CompositeSsf ssf = CompositeSsf.builder()
                .add(cmp, LocalLinearTrend.defaultLoading())
                .add(n, Noise.defaultLoading())
                .build();
        
        // smoothing using Durbin-Koopman for diffuse initialization
        // and with the specified variances (not estimated)
        SsfData data= new SsfData(Data.NILE);
        DataBlockStorage rslts = DkToolkit.fastSmooth(ssf, data);
        
        // trend at 0, slope at 1, noise at 2 (series at 0+1)       
        System.out.println(rslts.item(0));
        System.out.println(rslts.item(1));
        System.out.println(rslts.item(2));
        
        // some matrices of the model
        FastMatrix T=FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().T(0, T);
        System.out.println("T");
        System.out.println(T);
        FastMatrix V=FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().V(0, V);
        System.out.println("V");
        System.out.println(V);
        
        DataBlock Z=DataBlock.make(ssf.getStateDim());
        ssf.measurement().loading().Z(0, Z);
        System.out.println("Z");
        System.out.println(Z);
        
        // likelihood of the model
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, data, true, true);
        System.out.println("likelihood");
        System.out.println(ll);
        System.out.println(ll.e());
        
    }
}
