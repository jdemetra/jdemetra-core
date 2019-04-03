/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.ssf.implementations.MultivariateCompositeSsf;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public interface IMstsBuilder {
    int decode(DoubleSeq parameters, MultivariateCompositeSsf.Builder builder);
}
