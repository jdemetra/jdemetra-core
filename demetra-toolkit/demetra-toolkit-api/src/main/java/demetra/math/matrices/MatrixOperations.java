/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.math.matrices;

import demetra.data.DoubleSeq;
import demetra.design.Algorithm;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class MatrixOperations {

    private final MatrixOperationsLoader.Computer COMPUTER = new MatrixOperationsLoader.Computer();

    public void setComputer(Computer computer) {
        COMPUTER.set(computer);
    }

    public Computer getComputer() {
        return COMPUTER.get();
    }

    public Matrix At(Matrix A) {
        return getComputer().At(A);
    }

    public Matrix AB(Matrix A, Matrix B) {
        return getComputer().AB(A, B);
    }

    public Matrix ABt(Matrix A, Matrix B) {
        return getComputer().ABt(A, B);
    }

    public Matrix AtB(Matrix A, Matrix B) {
        return getComputer().AtB(A, B);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Computer {

        Matrix At(Matrix A);

        Matrix AB(Matrix A, Matrix B);

        Matrix ABt(Matrix A, Matrix B);

        Matrix AtB(Matrix A, Matrix B);
    }

    @lombok.experimental.UtilityClass
    public static class Symmetric {

        private final MatrixOperationsLoader.SymmetricComputer COMPUTER = new MatrixOperationsLoader.SymmetricComputer();

        public void setComputer(SymmetricComputer computer) {
            COMPUTER.set(computer);
        }

        public SymmetricComputer getComputer() {
            return COMPUTER.get();
        }

        public Matrix t(Matrix L) {
            return getComputer().t(L);
        }

        public Matrix XXt(Matrix X) {
            return getComputer().XXt(X);
        }

        public Matrix XtX(Matrix X) {
            return getComputer().XtX(X);
        }

        public Matrix xSx(Matrix S, DoubleSeq x) {
            return getComputer().xSx(S, x);
        }

        public Matrix XSXt(Matrix S, Matrix X) {
            return getComputer().XSXt(S, X);
        }

        public Matrix XtSX(Matrix S, Matrix X) {
            return getComputer().XtSX(S, X);
        }

        public Matrix lcholesky(Matrix M) {
            return getComputer().lcholesky(M);
        }

        public Matrix ucholesky(Matrix M) {
            return getComputer().ucholesky(M);
        }

        public Matrix inverse(Matrix M) {
            return getComputer().inverse(M);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface SymmetricComputer {

            Matrix t(Matrix M);

            Matrix xSx(Matrix S, DoubleSeq x);

            Matrix XXt(Matrix X);

            Matrix XtX(Matrix X);

            Matrix XSXt(Matrix S, Matrix X);

            Matrix XtSX(Matrix S, Matrix X);

            Matrix lcholesky(Matrix M);

            Matrix ucholesky(Matrix M);

            Matrix inverse(Matrix M);

        }
    }

    @lombok.experimental.UtilityClass
    public static class LowerTriangular {

        private final MatrixOperationsLoader.LowerTriangularComputer COMPUTER = new MatrixOperationsLoader.LowerTriangularComputer();

        public void setComputer(LowerTriangularComputer computer) {
            COMPUTER.set(computer);
        }

        public LowerTriangularComputer getComputer() {
            return COMPUTER.get();
        }

        public Matrix t(Matrix L) {
            return getComputer().t(L);
        }

        public Matrix inverse(Matrix L) {
            return getComputer().inverse(L);
        }

        public DoubleSeq solveLx(Matrix L, DoubleSeq b) {
            return getComputer().solveLx(L, b);
        }

        public DoubleSeq solvexL(Matrix L, DoubleSeq b) {
            return getComputer().solvexL(L, b);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface LowerTriangularComputer {

            Matrix t(Matrix L);

            Matrix inverse(Matrix M);

            /**
             * Solve Lx=b (or x=inv(L)*b
             *
             * @param L
             * @param b
             * @return
             */
            DoubleSeq solveLx(Matrix L, DoubleSeq b);

            /**
             * Solve xL=b (or x=b*inv(L)
             *
             * @param L
             * @param b
             * @return
             */
            DoubleSeq solvexL(Matrix L, DoubleSeq b);
        }
    }

    @lombok.experimental.UtilityClass
    public static class UpperTriangular {

        private final MatrixOperationsLoader.UpperTriangularComputer COMPUTER = new MatrixOperationsLoader.UpperTriangularComputer();

        public void setComputer(UpperTriangularComputer computer) {
            COMPUTER.set(computer);
        }

        public UpperTriangularComputer getComputer() {
            return COMPUTER.get();
        }

        public Matrix t(Matrix L) {
            return getComputer().t(L);
        }

        public Matrix inverse(Matrix U) {
            return getComputer().inverse(U);
        }

        public DoubleSeq solveUx(Matrix U, DoubleSeq b) {
            return getComputer().solveUx(U, b);
        }

        public DoubleSeq solvexU(Matrix U, DoubleSeq b) {
            return getComputer().solvexU(U, b);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface UpperTriangularComputer {

            Matrix inverse(Matrix M);

            Matrix t(Matrix U);

            /**
             * Solve Ux=b (or x=inv(U)*b or )
             *
             * @param U
             * @param b
             * @return
             */
            DoubleSeq solveUx(Matrix U, DoubleSeq b);

            /**
             * Solve xU=b (or x=b*inv(U)
             *
             * @param U
             * @param b
             * @return
             */
            DoubleSeq solvexU(Matrix U, DoubleSeq b);
        }
    }
}
