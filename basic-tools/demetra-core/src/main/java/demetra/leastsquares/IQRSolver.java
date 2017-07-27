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
package demetra.leastsquares;

import demetra.design.Algorithm;
import demetra.leastsquares.internal.QRSolver;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 *
 * @author Jean Palate
 */
@Algorithm
public interface IQRSolver extends ILeastSquaresSolver {

      public static IQRSolver fastSolver() {
        return QR_Factory.FAST_FACTORY.get().get();
    }

    public static IQRSolver robustSolver() {
        return QR_Factory.ROBUST_FACTORY.get().get();
    }

    public static void setFastSolver(Supplier<IQRSolver> factory) {
        QR_Factory.FAST_FACTORY.set(factory);
    }

    public static void setRobustSolver(Supplier<IQRSolver> factory) {
        QR_Factory.ROBUST_FACTORY.set(factory);
    }
  
    Matrix R();
}

class QR_Factory {

    static AtomicReference<Supplier<IQRSolver>> FAST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).build());
    static AtomicReference<Supplier<IQRSolver>> ROBUST_FACTORY = new AtomicReference<>(() -> 
            QRSolver.builder(new Householder()).iterative(3).simpleIteration(true).build());
}
