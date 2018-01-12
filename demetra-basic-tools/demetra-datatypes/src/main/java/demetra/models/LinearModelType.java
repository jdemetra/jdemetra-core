/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.models;

import demetra.data.DoubleSequence;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class LinearModelType {
    DoubleSequence y;
    boolean meanCorrection;
    MatrixType X;
}
