/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
