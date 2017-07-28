/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.likelihood.ConcentratedLikelihood;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IOls {
    OlsResults compute(LinearModel model);
}
