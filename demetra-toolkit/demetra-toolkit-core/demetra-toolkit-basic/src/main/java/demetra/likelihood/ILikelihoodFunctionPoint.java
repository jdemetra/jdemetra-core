/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.likelihood;

import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */

public interface ILikelihoodFunctionPoint<L extends ILikelihood> extends IFunctionPoint, ISsqFunctionPoint{
    L getLikelihood();
}
