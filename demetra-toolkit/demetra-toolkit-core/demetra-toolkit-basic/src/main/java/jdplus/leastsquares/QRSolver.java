/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.leastsquares;

import demetra.data.DoubleSeq;
import demetra.design.Algorithm;
import demetra.design.Development;
import jdplus.leastsquares.internal.DefaultQRSolver;
import jdplus.math.matrices.Matrix;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 * Solves a least squares problem by means of the QR algorithm.
 *
 * @author Jean Palate
 */
@Algorithm
@ServiceDefinition(quantifier = Quantifier.SINGLE,
        mutability = Mutability.CONCURRENT,
        fallback = DefaultQRSolver.class)
public interface QRSolver {
    
    static QRSolverLoader PROCESSOR=new QRSolverLoader();
    
    public static QRSolution process(DoubleSeq y, Matrix X){
        return PROCESSOR.get().solve(y, X);
    }
    
    QRSolution solve(DoubleSeq y, Matrix X);
 
}
