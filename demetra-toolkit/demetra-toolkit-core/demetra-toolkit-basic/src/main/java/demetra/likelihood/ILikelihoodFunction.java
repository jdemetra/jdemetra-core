/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.likelihood;

import demetra.maths.functions.IFunction;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 * @param <L>
 */
public interface ILikelihoodFunction<L extends ILikelihood> extends IFunction, ISsqFunction {
    @Override
    ILikelihoodFunctionPoint<L> evaluate(DoubleSeq p);
    
    @Override
    ILikelihoodFunctionPoint<L> ssqEvaluate(DoubleSeq p);
    
}
