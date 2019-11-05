/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.linearsystem;


import jdplus.linearsystem.internal.QRLinearSystemSolver;
import jdplus.linearsystem.internal.LUSolver;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import jdplus.data.DataBlock;
import demetra.design.Algorithm;
import demetra.design.Development;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.decomposition.CroutDoolittle;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.maths.matrices.FastMatrix;

/**
 * Defines algorithms that solve linear system
 * The system contains n equations with n unknowns
 * It is defined by Ax=b
 * A unique solution exists iff A is invertible
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Algorithm
public interface LinearSystemSolver {

    public static LinearSystemSolver fastSolver(){
        return LS_Factory.FAST_FACTORY.get().get();
    }

    public static LinearSystemSolver robustSolver(){
        return LS_Factory.ROBUST_FACTORY.get().get();
    }

    public static void setFastSolver(Supplier<LinearSystemSolver> factory){
        LS_Factory.FAST_FACTORY.set(factory);
    }
    
    public static void setRobustSolver(Supplier<LinearSystemSolver> factory){
        LS_Factory.ROBUST_FACTORY.set(factory);
    }

    /**
     * Solves Ax=b
     *
     * @param A
     * @param b On entry, the datablock contains the right terms of the system (b).
          On exit, it contains the results of the system (x).
     * @throws MatrixException
     */
    void solve(FastMatrix A, DataBlock b) throws MatrixException;

    /**
     * Solves AX=B
     *
     * @param A
     * @param B On entry, the matrix contains the right terms of the system (B).
          On exit, it contains the results of the system (X).
     * @throws MatrixException
     */
    void solve(FastMatrix A, FastMatrix B) throws MatrixException;
}

class LS_Factory{
   
    static AtomicReference<Supplier<LinearSystemSolver>> FAST_FACTORY = new AtomicReference<>(
            ()->LUSolver.builder(new CroutDoolittle()).normalize(true).build());
    static AtomicReference<Supplier<LinearSystemSolver>> ROBUST_FACTORY = new AtomicReference<>(
            ()->QRLinearSystemSolver.builder(new Householder()).normalize(true).build());
}
