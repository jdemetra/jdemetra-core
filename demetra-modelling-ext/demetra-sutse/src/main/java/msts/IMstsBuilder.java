/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DoubleSequence;
import demetra.ssf.implementations.MultivariateCompositeSsf;

/**
 *
 * @author palatej
 */
public interface IMstsBuilder {
    int decode(DoubleSequence parameters, int startpos, MultivariateCompositeSsf.Builder builder);
}
