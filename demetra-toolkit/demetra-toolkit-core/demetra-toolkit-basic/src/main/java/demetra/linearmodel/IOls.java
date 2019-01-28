/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.design.Algorithm;
import demetra.design.Development;
import demetra.design.ServiceDefinition;
import demetra.maths.matrices.spi.UpperTriangularMatrixAlgorithms;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Algorithm
@ServiceDefinition()
@Development(status=Development.Status.Alpha)
public interface IOls {
    LeastSquaresResults compute(LinearModel model);
}

