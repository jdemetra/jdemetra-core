/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package jdplus.maths.matrices.decomposition;

import demetra.math.Complex;
import jdplus.maths.matrices.FastMatrix;


/**
 *
 * @author Jean Palate
 */
public interface IEigenSystem {

    /**
     *
     */
    public final static String EIGENINIT = "eig_init";
    /**
         *
         */
    public final static String EIGENFAILED = "eig_failed";
    /**
     * The method forces the computation of the eigenvalues and optionnaly the eigenvectors
     */
    void compute();

    /**
     * The method returns an array with all the eigenvalues.
     *
     * @return
     */
    Complex[] getEigenValues();
    /// <summary>
    /// . The eigenvalues can be complex.
    /// </summary>

    /**
     * The method returns an array with the first n eigenvalues
     *
     * @param n
     * @return
     */
    Complex[] getEigenValues(int n);

    /**
     * The method returns the eigenvector for the eigenvalue in position idx.
     *
     * @param idx
     * @return
     */
    double[] getEigenVector(int idx);

    /**
     * The method returns all the eigenvectors as an IMatrix. Each eigenvector takes a column of the
     * matrix
     *
     * @return
     */
    FastMatrix getEigenVectors();

    /**
     * The method returns the first n eigenvectors as an IMatrix. Each eigenvector takes a column of the
     * matrix
     *
     * @param n
     * @return
     */
    FastMatrix getEigenVectors(int n);
    /// <summary>
    /// The property sets/gets the treshold for small values
    /// </summary>

    double getZero();

    /**
     * @param zero
     */
    void setZero(double zero);

    /**
     * @return
     */
    boolean isComputingEigenVectors();

    /**
     * @param value
     */
    void setComputingEigenVectors(boolean value);
}
