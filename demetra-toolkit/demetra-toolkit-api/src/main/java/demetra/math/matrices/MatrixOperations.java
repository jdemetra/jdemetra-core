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

    public MatrixType At(MatrixType A) {
        return getComputer().At(A);
    }

    public MatrixType AB(MatrixType A, MatrixType B) {
        return getComputer().AB(A, B);
    }

    public MatrixType ABt(MatrixType A, MatrixType B) {
        return getComputer().ABt(A, B);
    }

    public MatrixType AtB(MatrixType A, MatrixType B) {
        return getComputer().AtB(A, B);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Computer {

        MatrixType At(MatrixType A);

        MatrixType AB(MatrixType A, MatrixType B);

        MatrixType ABt(MatrixType A, MatrixType B);

        MatrixType AtB(MatrixType A, MatrixType B);
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

        public MatrixType t(MatrixType L) {
            return getComputer().t(L);
        }

        public MatrixType XXt(MatrixType X) {
            return getComputer().XXt(X);
        }

        public MatrixType XtX(MatrixType X) {
            return getComputer().XtX(X);
        }

        public MatrixType xSx(MatrixType S, DoubleSeq x) {
            return getComputer().xSx(S, x);
        }

        public MatrixType XSXt(MatrixType S, MatrixType X) {
            return getComputer().XSXt(S, X);
        }

        public MatrixType XtSX(MatrixType S, MatrixType X) {
            return getComputer().XtSX(S, X);
        }

        public MatrixType lcholesky(MatrixType M) {
            return getComputer().lcholesky(M);
        }

        public MatrixType ucholesky(MatrixType M) {
            return getComputer().ucholesky(M);
        }

        public MatrixType inverse(MatrixType M) {
            return getComputer().inverse(M);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface SymmetricComputer {

            MatrixType t(MatrixType M);

            MatrixType xSx(MatrixType S, DoubleSeq x);

            MatrixType XXt(MatrixType X);

            MatrixType XtX(MatrixType X);

            MatrixType XSXt(MatrixType S, MatrixType X);

            MatrixType XtSX(MatrixType S, MatrixType X);

            MatrixType lcholesky(MatrixType M);

            MatrixType ucholesky(MatrixType M);

            MatrixType inverse(MatrixType M);

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

        public MatrixType t(MatrixType L) {
            return getComputer().t(L);
        }

        public MatrixType inverse(MatrixType L) {
            return getComputer().inverse(L);
        }

        public DoubleSeq solveLx(MatrixType L, DoubleSeq b) {
            return getComputer().solveLx(L, b);
        }

        public DoubleSeq solvexL(MatrixType L, DoubleSeq b) {
            return getComputer().solvexL(L, b);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface LowerTriangularComputer {

            MatrixType t(MatrixType L);

            MatrixType inverse(MatrixType M);

            /**
             * Solve Lx=b (or x=inv(L)*b
             *
             * @param L
             * @param b
             * @return
             */
            DoubleSeq solveLx(MatrixType L, DoubleSeq b);

            /**
             * Solve xL=b (or x=b*inv(L)
             *
             * @param L
             * @param b
             * @return
             */
            DoubleSeq solvexL(MatrixType L, DoubleSeq b);
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

        public MatrixType t(MatrixType L) {
            return getComputer().t(L);
        }

        public MatrixType inverse(MatrixType U) {
            return getComputer().inverse(U);
        }

        public DoubleSeq solveUx(MatrixType U, DoubleSeq b) {
            return getComputer().solveUx(U, b);
        }

        public DoubleSeq solvexU(MatrixType U, DoubleSeq b) {
            return getComputer().solvexU(U, b);
        }

        @Algorithm
        @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
        public static interface UpperTriangularComputer {

            MatrixType inverse(MatrixType M);

            MatrixType t(MatrixType U);

            /**
             * Solve Ux=b (or x=inv(U)*b or )
             *
             * @param U
             * @param b
             * @return
             */
            DoubleSeq solveUx(MatrixType U, DoubleSeq b);

            /**
             * Solve xU=b (or x=b*inv(U)
             *
             * @param U
             * @param b
             * @return
             */
            DoubleSeq solvexU(MatrixType U, DoubleSeq b);
        }
    }
}
