/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.examples;

import demetra.data.Data;
import demetra.sts.SeasonalModel;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.sts.LocalLinearTrend;
import jdplus.ssf.sts.Noise;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.SeasonalComponent;

/**
 * Local linear trend + Seasonal + Noise
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Ssf2 {

    public void main(String[] args) {
         ti();
//        tv();
    }

    // Time invariant model
    public void ti() {
        StateComponent cmp = LocalLinearTrend.stateComponent(.1, .01);
        StateComponent seas = SeasonalComponent.of(SeasonalModel.HarrisonStevens, 12, 0.2);
        StateComponent n = Noise.of(1);

        // create a composite state space form
        CompositeSsf ssf = CompositeSsf.builder()
                .add(cmp, LocalLinearTrend.defaultLoading())
                .add(seas, SeasonalComponent.defaultLoading())
                .add(n, Noise.defaultLoading())
                .build();

        // smoothing using Durbin-Koopman for diffuse initialization
        // and with the specified variances (not estimated)
        SsfData data = new SsfData(Data.PROD);
        DefaultSmoothingResults rslts = DkToolkit.sqrtSmooth(ssf, data, true, true);

        // trend at 0, season at 2, noise at 2 (series at 0+1)       
        System.out.println(rslts.getComponent(0));
        System.out.println(rslts.getComponent(2));
        System.out.println(rslts.getComponent(ssf.getStateDim() - 1));
        System.out.println(rslts.getComponentVariance(0));
        System.out.println(rslts.getComponentVariance(2));
        System.out.println(rslts.getComponentVariance(ssf.getStateDim() - 1));

        // some matrices of the model
        FastMatrix T = FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().T(0, T);
        System.out.println("T");
        System.out.println(T);
        FastMatrix V = FastMatrix.square(ssf.getStateDim());
        ssf.dynamics().V(0, V);
        System.out.println("V");
        System.out.println(V);

        DataBlock Z = DataBlock.make(ssf.getStateDim());
        ssf.measurement().loading().Z(0, Z);
        System.out.println("Z");
        System.out.println(Z);

        // likelihood of the model
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, data, true, true);
        System.out.println("likelihood");
        System.out.println(ll);
        System.out.println(ll.e());

    }

    // Time variant model
    public void tv() {
        StateComponent cmp = LocalLinearTrend.stateComponent(.1, .01);
        StateComponent seas = SeasonalComponent.harrisonStevens(12, 0.2);
        StateComponent n = Noise.of(1);

        // create a composite state space form
        ISsfLoading sloading = SeasonalComponent.harrisonStevensLoading(12);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(cmp, LocalLinearTrend.defaultLoading())
                .add(n, Noise.defaultLoading())
                .add(seas, sloading)
                .build();

        // smoothing using Durbin-Koopman for diffuse initialization
        // and with the specified variances (not estimated)
        SsfData data = new SsfData(Data.PROD);
        DefaultSmoothingResults rslts = DkToolkit.sqrtSmooth(ssf, data, true, true);
        
        DataBlock s=DataBlock.make(data.length());
        DataBlock sv=DataBlock.make(data.length());
        
        DataBlock z=DataBlock.make(ssf.getStateDim());
        for (int i=0; i<s.length(); ++i){
            z.set(0);
            sloading.Z(i, z.drop(3, 0));
            s.set(i, z.dot(rslts.a(i)));
            sv.set(i, QuadraticForm.apply(rslts.P(i), z));
        }
         
        System.out.println(rslts.getComponent(0));
        System.out.println(s);
        System.out.println(rslts.getComponent(2));
        System.out.println(rslts.getComponentVariance(0));
        System.out.println(sv);
        System.out.println(rslts.getComponentVariance(2));

        // likelihood of the model
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, data, true, true);
        System.out.println("likelihood");
        System.out.println(ll);
        System.out.println(ll.e());

    }

}
