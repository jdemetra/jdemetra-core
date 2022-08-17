/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.ssf.composite.MultivariateCompositeSsf;
import demetra.data.DoubleSeq;

/**
 *
 * @author palatej
 */
public interface IMstsBuilder {
    int decode(DoubleSeq parameters, MultivariateCompositeSsf.Builder builder);
}
