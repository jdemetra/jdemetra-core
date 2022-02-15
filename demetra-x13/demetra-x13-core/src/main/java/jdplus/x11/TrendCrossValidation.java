/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x11;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.x11.SeasonalFilterOption;
import java.util.function.IntFunction;
import jdplus.math.linearfilters.CrossValidation;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TrendCrossValidation {

    public double[] process(DoubleSeq in, int period, boolean mul, int start, int end, IntFunction<SymmetricFilter> fn){
        SeasonalFilterOption[] s0=new SeasonalFilterOption[period];
        SeasonalFilterOption[] s1=new SeasonalFilterOption[period];
        for (int i=0; i<period; ++i){
            s0[i]=SeasonalFilterOption.S3X3;
            s1[i]=SeasonalFilterOption.S3X5;
        }
        X11Context context = X11Context.builder()
                .period(period)
                .mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive)
                .initialSeasonalFilter(s0)
                .finalSeasonalFilter(s1)
                .build();
        X11BStep bstep=new X11BStep();
        bstep.process(in, context);
        return CrossValidation.doCrossValidation(bstep.getB6(), start, end, fn);
    }
}
